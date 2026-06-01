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
package wang.bigbird.domain.framework.common.similarity.support.calculator.word.cilin;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.similarity.config.property.SimilarityProperties;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.core.base.util.event.TraverseEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * 词林编码
 *
 * @author Bigbird
 */
@Slf4j
public class CilinDictionary {

    private final static String path = SimilarityProperties.CilinPath;

    /**
     * 以词语为索引
     */
    private final Map<String, Set<String>> wordIndex = new HashMap<>();
    /**
     * 以编码为索引
     */
    private final Map<String, Set<String>> codeIndex = new HashMap<>();

    private static CilinDictionary instance;

    public static CilinDictionary getInstance() {
        if (instance == null) {
            try {
                instance = new CilinDictionary();
            } catch (IOException e) {
                log.error("GetInstance:{}", e.getMessage(), e);
            }
        }
        return instance;
    }

    private CilinDictionary() throws IOException {
        InputStream inputStream = new GZIPInputStream(getClass().getClassLoader().getResourceAsStream(path));
        // 解析词库，获得每个词对应的编码库以及每个编码对应的词库
        TraverseEvent<String> event = line -> {
            String[] items = line.split(CommonConstants.SPACE);
            Set<String> set = new HashSet<>();
            for (int i = 2; i < items.length; i++) {
                String code = items[i].trim();
                if (StringUtils.isNotBlank(code)) {
                    set.add(code);
                    Set<String> codeWords = codeIndex.get(code);
                    if (codeWords == null) {
                        codeWords = new HashSet<>();
                        codeIndex.put(code, codeWords);
                    }
                    codeWords.add(items[0]);
                }
            }
            wordIndex.put(items[0], set);
            return true;
        };
        log.info("loading dictionary...");
        long start = System.currentTimeMillis();
        FileUtils.traverseLines(inputStream, Coder.DEFAULT_ENCODING, event);
        log.info("loading dictionary complete! time spend:{}ms", System.currentTimeMillis() - start);
    }

    /**
     * 获取某词语的词林编码，一个词语可以对应多个编码
     *
     * @param word 词语
     * @return 词林编码集
     */
    public Set<String> getCilinCodes(String word) {
        return wordIndex.get(word);
    }

    /**
     * 获取某词林编码的词语，一个词林编码可以对应多个词语
     *
     * @param code 词林编码
     * @return 词语集
     */
    public Set<String> getCilinWords(String code) {
        return codeIndex.get(code);
    }

}
