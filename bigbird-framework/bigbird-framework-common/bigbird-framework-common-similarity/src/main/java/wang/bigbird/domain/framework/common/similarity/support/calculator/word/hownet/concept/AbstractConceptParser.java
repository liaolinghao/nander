/*
 * Copyright (c) 2026 廖凌浩 / 鸟域
 *
 * Licensed under the Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.concept;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import wang.bigbird.domain.framework.common.similarity.config.property.SimilarityProperties;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.AbstractWordSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.IHownetMeta;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.sememe.AbstractSememeParser;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.DataUtils;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 * 概念解析器
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractConceptParser extends AbstractWordSimilarity implements IHownetMeta {

    private static Multimap<String, Concept> CONCEPTS = null;

    private final static String path = SimilarityProperties.ConceptXmlPath;

    protected AbstractSememeParser sememeParser;

    public AbstractConceptParser(AbstractSememeParser sememeParser) throws IOException {
        this.sememeParser = sememeParser;
        synchronized (this) {
            if (CONCEPTS == null) {
                loadFile();
            }
        }
    }

    private void loadFile() throws IOException {
        CONCEPTS = HashMultimap.create();
        InputStream inputStream = new GZIPInputStream(getClass().getClassLoader().getResourceAsStream(path));
        load(inputStream);
    }

    private static void load(InputStream inputStream) {
        log.info("loading concept dictionary...");
        long start = System.currentTimeMillis();
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(inputStream);
            while (xmlEventReader.hasNext()) {
                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (startElement.getName().toString().equals("c")) {
                        String word = startElement.getAttributeByName(QName.valueOf("w")).getValue();
                        String define = startElement.getAttributeByName(QName.valueOf("d")).getValue();
                        String pos = startElement.getAttributeByName(QName.valueOf("p")).getValue();
                        CONCEPTS.put(word, new Concept(word, pos, define));
                    }
                }
            }
        } catch (Exception e) {
            log.error("xml err:{}", e.getMessage(), e);
        } finally {
            StreamUtils.close(inputStream);
        }
        log.info("loading concept dictionary complete! time spend:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * 计算四个组成部分的相似度方式，不同的算法对这四个部分的处理或者说权重分配不同
     *
     * @param sim_v1 主义原的相似度
     * @param sim_v2 其他基本义原的相似度
     * @param sim_v3 关系义原的相似度
     * @param sim_v4 符号义原的相似度
     * @return
     */
    protected abstract double calculate(double sim_v1, double sim_v2, double sim_v3, double sim_v4);

    /**
     * 判断一个词语是否是一个概念
     *
     * @param word 词语
     * @return 是否属于概念
     */
    public boolean isConcept(String word) {
        return CollectionUtils.isNotEmpty(CONCEPTS.get(word));
    }

    /**
     * 根据名称获取知网本身对应的概念定义信息，由于一个词语可能对应多个概念，因此返回一个集合
     *
     * @param word 词语
     * @return 词语对应概念集合
     */
    public Collection<Concept> getConcepts(String word) {
        return CONCEPTS.get(word);
    }

    /**
     * 计算两个概念的相似度
     *
     * @param concept1 概念1
     * @param concept2 概念2
     * @return 概念之间的相似度
     */
    public double calculateSimilarity(Concept concept1, Concept concept2) {
        if (concept1 == null || concept2 == null || !concept1.getPos().equals(concept2.getPos())) {
            return 0.0;
        }
        if (concept1.equals(concept2)) {
            return 1.0;
        }
        // 虚词和实词概念的相似度是0
        if (concept1.isSubstantive() != concept2.isSubstantive()) {
            return 0.0;
        }
        double similarity = 0.0;
        if (concept1.isSubstantive()) {
            // 实词
            double sim1 = sememeParser.calculate(concept1.getMainSememe(), concept2.getMainSememe());
            double sim2 = calculateSimilarity(concept1.getSecondSememes(), concept2.getSecondSememes());
            double sim3 = calculateSimilarity(concept1.getRelationSememes(), concept2.getRelationSememes());
            double sim4 = calculateSimilarity(concept1.getSymbolSememes(), concept2.getSymbolSememes());
            similarity = calculate(sim1, sim2, sim3, sim4);
        } else {
            // 虚词
            similarity = sememeParser.calculate(concept1.getMainSememe(), concept2.getMainSememe());
        }
        return similarity;
    }

    /**
     * 计算两个义原集合的相似度
     * 每一个集合都是一个概念的某一类义原集合，如第二基本义原、符号义原、关系义原等
     * 1、构造两个义原集合的相似度矩阵
     * 2、每次找最大相似度值
     * 3、去掉该行列，继续找下一个最大
     * 4、全部匹配完 → 总和 ÷ 最大集合长度 = 集合相似度
     *
     * @param sememes1 义原集合1
     * @param sememes2 义原集合2
     * @return 义原集合的相似度
     */
    private double calculateSimilarity(String[] sememes1, String[] sememes2) {
        if (ArrayUtils.isEmpty(sememes1) || ArrayUtils.isEmpty(sememes2)) {
            if (ArrayUtils.isEmpty(sememes1) && ArrayUtils.isEmpty(sememes2)) {
                return 1.0;
            } else {
                return delta;
            }
        }
        double score = 0.0;
        int arrayLen = DataUtils.max(sememes1.length, sememes2.length).intValue();
        double scoreArray[][] = new double[arrayLen][arrayLen];
        // calculate similarity of two set
        for (int i = 0; i < sememes1.length; i++) {
            for (int j = 0; j < sememes2.length; j++) {
                scoreArray[i][j] = sememeParser.calculate(sememes1[i], sememes2[j]);
            }
        }
        // get max similarity score
        while (scoreArray.length > 0) {
            double[][] temp;
            int row = 0;
            int column = 0;
            double max = scoreArray[row][column];
            for (int i = 0; i < scoreArray.length; i++) {
                for (int j = 0; j < scoreArray[i].length; j++) {
                    if (scoreArray[i][j] > max) {
                        row = i;
                        column = j;
                        max = scoreArray[i][j];
                    }
                }
            }
            score += max;
            // 过滤掉该行该列，继续计算
            temp = new double[scoreArray.length - 1][scoreArray.length - 1];
            for (int i = 0; i < scoreArray.length; i++) {
                if (i == row) {
                    continue;
                }
                for (int j = 0; j < scoreArray[i].length; j++) {
                    if (j == column) {
                        continue;
                    }
                    int tempRow = i;
                    int tempColumn = j;
                    if (i > row) {
                        tempRow--;
                    }
                    if (j > column) {
                        tempColumn--;
                    }
                    temp[tempRow][tempColumn] = scoreArray[i][j];
                }
            }
            scoreArray = temp;
        }
        return score / arrayLen;
    }

}
