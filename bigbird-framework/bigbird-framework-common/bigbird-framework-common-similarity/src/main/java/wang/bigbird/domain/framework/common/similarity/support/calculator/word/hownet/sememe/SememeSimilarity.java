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

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import java.io.IOException;
import java.util.Collection;

/**
 * 义原相似度计算
 *
 * @author Bigbird
 */
@Slf4j
public class SememeSimilarity extends AbstractSememeParser {

    public SememeSimilarity() throws IOException {
        super();
    }

    /**
     * 计算两个义原的相似度
     *
     * @param id1 义原ID
     * @param id2 义原ID
     * @return 义原相似度
     */
    private double getSimilarityBySememeId(final String id1, final String id2) {
        int position = 0;
        String[] array1 = id1.split("-");
        String[] array2 = id2.split("-");
        for (position = 0; position < array1.length && position < array2.length; position++) {
            if (!array1[position].equals(array2[position])) {
                break;
            }
        }
        return 2.0 * position / (array1.length + array2.length);
    }

    /**
     * 根据汉语定义计算义原之间的相似度，由于可能多个义元有相同的汉语词语，故计算结果为其中相似度最大者
     *
     * @param sememeName1 义原中文名称
     * @param sememeName2 义原中文名称
     * @return 义原相似度
     */
    public double getMaxSimilarity(String sememeName1, String sememeName2) {
        double maxValue = 0.0;
        // 如果两个字符串相等，直接返回距离为0
        if (sememeName1.equals(sememeName2)) {
            return 1.0;
        }
        Collection<String> sememeIds1 = SEMEMES.get(sememeName1);
        Collection<String> sememeIds2 = SEMEMES.get(sememeName2);
        // 如果sememe1或者sememe2不是义元，则返回0
        if (sememeIds1.size() == 0 || sememeIds1.size() == 0) {
            return 0.0;
        }
        for (String id1 : sememeIds1) {
            for (String id2 : sememeIds2) {
                double value = getSimilarityBySememeId(id1, id2);
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        return maxValue;
    }

    /**
     * 计算两个义元之间的相似度，由于义元可能相同，计算结果为其中相似度最大者
     * similarity = alpha/(distance+alpha)
     */
    @Override
    public double doCalculate(String target, String candidate) {
        // 括号 () 表示动态角色 / 关系义原
        // 规则：只有两边都带括号 / 都不带括号，才能继续计算；否则语义不同 → 相似度 0
        if ((target.startsWith(CommonConstants.PARENTHESIS_START)) && target.endsWith(CommonConstants.PARENTHESIS_END)) {
            if (candidate.startsWith(CommonConstants.PARENTHESIS_START) && candidate.endsWith(CommonConstants.PARENTHESIS_END)) {
                target = target.substring(1, target.length() - 1);
                candidate = candidate.substring(1, candidate.length() - 1);
            } else {
                return 0.0;
            }
        }
        // 处理关系义元，即x=y的情况
        // 关系名必须相同，才能比较关系值；关系名不同 → 相似度 0
        int pos = target.indexOf(CommonConstants.EQUAL);
        if (pos > 0) {
            int pos2 = candidate.indexOf(CommonConstants.EQUAL);
            // 如果是关系义元，则判断前面部分是否相同，如果相同，则转为计算后面部分的相似度，否则为0
            if ((pos == pos2) && target.substring(0, pos).equals(candidate.substring(0, pos2))) {
                target = target.substring(pos + 1);
                candidate = candidate.substring(pos2 + 1);
            } else {
                return 0.0;
            }
        }
        // 处理符号义元，即前面有特殊符号的义元
        String symbol1 = target.substring(0, 1);
        String symbol2 = candidate.substring(0, 1);
        for (int i = 0; i < Symbol_Descriptions.length; i++) {
            if (symbol1.equals(Symbol_Descriptions[i][0])) {
                if (symbol1.equals(symbol2)) {
                    target = target.substring(1);
                    candidate = candidate.substring(1);
                    break;
                } else {
                    // 如果不是同一关系符号，则相似度直接返回0
                    return 0.0;
                }
            }
        }
        if ((pos = target.indexOf(CommonConstants.VERTICAL_BAR)) >= 0) {
            target = target.substring(pos + 1);
        }
        if ((pos = candidate.indexOf(CommonConstants.VERTICAL_BAR)) >= 0) {
            candidate = candidate.substring(pos + 1);
        }
        // 如果两个字符串相等，直接返回距离为0
        if (target.equals(candidate)) {
            return 1.0;
        }
        return getMaxSimilarity(target, candidate);
    }

}
