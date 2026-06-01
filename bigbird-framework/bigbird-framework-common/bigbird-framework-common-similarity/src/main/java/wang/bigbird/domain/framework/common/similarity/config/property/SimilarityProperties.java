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
package wang.bigbird.domain.framework.common.similarity.config.property;

import lombok.Data;

/**
 * 相似度配置
 *
 * @author Bigbird
 */
@Data
public class SimilarityProperties {

    /**
     * 词林编码路径
     */
    public static String CilinPath = "cilin.db.gz";
    /**
     * 拼音词典路径
     */
    public static String PinyinPath = "F02-GB2312-to-PuTongHua-PinYin.txt";
    /**
     * concept路径
     */
    public static String ConceptPath = "concept.dat";
    /**
     * concept.xml.gz路径
     */
    public static String ConceptXmlPath = "concept.xml.gz";
    /**
     * 义原关系的路径
     */
    public static String SememePath = "sememe.dat";
    /**
     * 义原数据路径
     */
    public static String SememeXmlPath = "sememe.xml.gz";
    /**
     * 词频统计输出路径
     */
    public static String StatisticsResultPath = "data/WordFrequencyStatistics-Result.txt";

}
