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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.pinyin;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.similarity.config.property.SimilarityProperties;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.event.TraverseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 查找汉字对应的拼音工具
 *
 * @author Bigbird
 */
@Slf4j
public class PinyinDictionary {

    private Map<Character, Set<String>> pinyinDict = new HashMap<>();

    private static final String path = SimilarityProperties.PinyinPath;

    private static PinyinDictionary instance;

    public static PinyinDictionary getInstance() {
        if (instance == null) {
            try {
                instance = new PinyinDictionary();
            } catch (IOException e) {
                log.error("GetInstance:{}", e.getMessage(), e);
            }
        }
        return instance;
    }

    private PinyinDictionary() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        // 解析词库，获得每个字对应的拼音
        TraverseEvent<String> event = line -> {
            char c = line.charAt(0);
            String pinyin = line.substring(2);
            Set<String> set = pinyinDict.get(c);
            if (set == null) {
                set = new HashSet<>();
                pinyinDict.put(c, set);
            }
            set.add(pinyin);
            return true;
        };
        log.info("loading dictionary...");
        long start = System.currentTimeMillis();
        FileUtils.traverseLines(inputStream, Coder.DEFAULT_ENCODING, event);
        log.info("loading dictionary complete! time spend:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * 获取汉字的拼音，由于汉字具有多音字，故返回一个集合
     *
     * @param c 字符
     * @return 拼音集
     */
    public Set<String> getPinyin(Character c) {
        Set<String> set = pinyinDict.get(c);
        if (set == null) {
            set = new HashSet<>();
            set.add(c.toString());
        }
        return set;
    }

    /**
     * 获取词语的拼音，一个词语可能对应多个拼音，把所有可能的组合放到集合中返回
     * 比如：教师
     * jiao1shi1
     * jiao4shi1
     *
     * @param word 词语
     * @return 词对应的不同声调拼音串
     */
    public Set<String> getPinyin(String word) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < word.length(); i++) {
            Set<String> pinyinSet = getPinyin(word.charAt(i));
            if (set.size() == 0) {
                set.addAll(pinyinSet);
                continue;
            }
            Set<String> tempSet = new HashSet<>();
            for (String s : set) {
                tempSet.addAll(pinyinSet.stream().map(p -> s + p).collect(Collectors.toList()));
            }
            set = tempSet;
        }
        return set;
    }

    /**
     * 获取拼音字符串，多音字只取一个
     * 比如：教师
     * jiao4shi1
     *
     * @param word 词语
     * @return 词对应的拼音串
     */
    public String getPinyinSingle(String word) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < word.length(); i++) {
            sb.append(getPinyin(word.charAt(i)).iterator().next());
        }
        return sb.toString();
    }

    /**
     * 获取拼音串，对于多音字，给出所有拼音
     * 比如：教师
     * [jiao4, jiao1][shi1]
     *
     * @param word 词语
     * @return 词对应的所有拼音串
     */
    public String getPinyinString(String word) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < word.length(); i++) {
            Set<String> pinyin = getPinyin(word.charAt(i));
            sb.append(pinyin.toString());
        }
        return sb.toString();
    }

    /**
     * 获取拼音首字母
     * 比如：教师
     * js
     *
     * @param word 词语
     * @return 词语对应的拼音首字母缩写
     */
    public String getPinyinHead(String word) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < word.length(); i++) {
            sb.append(getPinyin(word.charAt(i)).iterator().next().charAt(0));
        }
        return sb.toString();
    }

}
