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
package wang.bigbird.domain.framework.common.forbidden.support.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 用Map实现DFA
 *
 * @author Bigbird
 */
@Slf4j
public class MemoryMapDfaImpl implements Dfa {

    private Map dfaMap = Maps.newHashMap();

    @Override
    public void addWord(Iterator<String> words) {
        if (words == null) {
            return;
        }
        long count = 0L;
        Map nowMap;
        Map<String, String> newWordMap;
        // 迭代keyWordSet
        while (words.hasNext()) {
            String key = words.next();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            nowMap = dfaMap;
            for (int i = 0; i < key.length(); i++) {
                // 转换成char型
                char keyChar = key.charAt(i);
                // 获取
                Object charMap = nowMap.get(keyChar);
                // 如果存在该key，直接赋值
                if (charMap != null) {
                    nowMap = (Map) charMap;
                } else {
                    // 不存在则构建一个map
                    newWordMap = Maps.newHashMapWithExpectedSize(128);
                    // 不是最后一个
                    newWordMap.put(END_OF_WORD, Boolean.toString(i == (key.length() - 1)));
                    nowMap.put(keyChar, newWordMap);
                    nowMap = newWordMap;
                }
            }
            count++;
        }
        log.info("Load a total of {} forbidden words.", count);
    }

    @Override
    public Dfa createNewEmpty() {
        return new MemoryMapDfaImpl();
    }

    @Override
    public FlagIndex getFlagIndex(String text, int begin) {
        final char[] charset = text.toCharArray();
        Map current = dfaMap;
        boolean flag = false;
        int count = 0;
        List<Integer> index = Lists.newArrayList();
        for (int i = begin; i < charset.length; i++) {
            char word = charset[i];
            Map mapTree = (Map) current.get(word);
            boolean stop = count > 0 || (i == begin && mapTree == null);
            if (stop) {
                break;
            }
            if (mapTree != null) {
                current = mapTree;
                count = 0;
                index.add(i);
            } else {
                count++;
                if (flag && count > 0) {
                    break;
                }
            }
            flag = "true".equals(current.get(Dfa.END_OF_WORD));
        }
        FlagIndex fi = new FlagIndex();
        fi.setFlag(flag);
        fi.setIndex(index);
        return fi;
    }
}
