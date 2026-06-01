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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.sememe;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.similarity.config.property.SimilarityProperties;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.AbstractWordSimilarity;
import wang.bigbird.domain.framework.common.similarity.support.calculator.word.hownet.IHownetMeta;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * 义原解析器
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractSememeParser extends AbstractWordSimilarity implements IHownetMeta {

    /**
     * 所有的义原都存放到一个MultiMap，Key为Sememe的中文定义，Value为义原的Id
     */
    protected static Multimap<String, String> SEMEMES = null;

    private static final String path = SimilarityProperties.SememeXmlPath;

    public AbstractSememeParser() throws IOException {
        if (SEMEMES != null) {
            return;
        }
        SEMEMES = HashMultimap.create();
        InputStream inputStream = new GZIPInputStream(getClass().getClassLoader().getResourceAsStream(path));
        load(inputStream);
    }

    /**
     * 文件加载义原
     */
    private void load(InputStream inputStream) {
        log.info("loading sememe dictionary...");
        long start = System.currentTimeMillis();
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlEventReader = inputFactory.createXMLEventReader(inputStream);
            while (xmlEventReader.hasNext()) {
                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    if (startElement.getName().toString().equals("sememe")) {
                        String cnWord = startElement.getAttributeByName(QName.valueOf("cn")).getValue();
                        String id = startElement.getAttributeByName(QName.valueOf("id")).getValue();
                        SEMEMES.put(cnWord, id);
                    }
                }
            }
        } catch (Exception e) {
            log.error("xml err:{}", e.getMessage(), e);
        } finally {
            StreamUtils.close(inputStream);
        }
        log.info("loading sememe dictionary complete! time spend:{}ms", System.currentTimeMillis() - start);
    }

}
