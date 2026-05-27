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
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
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
        log.debug("Load a total of {} forbidden words.", count);
        log.debug("Dfa: {}", JsonUtils.object2Json(dfaMap));
    }

    @Override
    public void removeWord(Iterator<String> words) {
        if (words == null) {
            return;
        }
        // 迭代keyWordSet
        while (words.hasNext()) {
            String word = words.next();
            if (StringUtils.isBlank(word)) {
                continue;
            }
            removeSingleWord(word);
        }
        log.debug("Dfa: {}", JsonUtils.object2Json(dfaMap));
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

    @Override
    public void clear() {
        dfaMap.clear();
    }

    private void removeSingleWord(String word) {
        List<Map> path = Lists.newArrayList();
        Map current = dfaMap;
        // 第一步：沿着字符路径走一遍，记录每一层节点
        for (char c : word.toCharArray()) {
            Map next = (Map) current.get(c);
            if (next == null) {
                // 不存在，直接返回
                return;
            }
            path.add(current);
            current = next;
        }
        // 检查是否真的是一个结束标记
        boolean isEnd = "true".equals(current.get(END_OF_WORD));
        if (!isEnd) {
            return;
        }
        // 第二步：删除结束标记（最干净、不影响其他词）
        current.remove(END_OF_WORD);
        // 第三步：从后往前清理空节点（可选，优化内存）
        for (int i = path.size() - 1; i >= 0; i--) {
            Map parent = path.get(i);
            char keyChar = word.charAt(i);
            Map child = (Map) parent.get(keyChar);
            // 如果子节点是空的，直接删掉
            if (child != null && (child.isEmpty() || (child.containsKey(END_OF_WORD) && child.size() == 1))) {
                parent.remove(keyChar);
            } else {
                // 只要有一个节点不为空，前面的都不能删
                break;
            }
        }
    }

}
