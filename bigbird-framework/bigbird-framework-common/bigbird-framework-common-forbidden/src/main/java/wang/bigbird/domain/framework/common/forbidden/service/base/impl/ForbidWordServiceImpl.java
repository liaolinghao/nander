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
package wang.bigbird.domain.framework.common.forbidden.service.base.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.forbidden.support.core.FlagIndex;
import wang.bigbird.domain.framework.common.forbidden.support.repository.AbstractForbidWordRepository;
import wang.bigbird.domain.framework.common.forbidden.service.base.IForbidWordService;

import java.util.List;

/**
 * 检测禁用词服务
 *
 * @author Bigbird
 */
@Slf4j
public class ForbidWordServiceImpl implements IForbidWordService {

    private final AbstractForbidWordRepository forbidWordRepository;

    public ForbidWordServiceImpl(AbstractForbidWordRepository forbidWordRepository) {
        this.forbidWordRepository = forbidWordRepository;
        forbidWordRepository.refresh(false);
    }

    @Override
    public boolean include(String text) {
        boolean flag = false;
        for (int i = 0; i < text.length(); i++) {
            flag = getFlagIndex(text, i).isFlag();
            if (flag) {
                break;
            }
        }
        return flag;
    }

    @Override
    public int forbidWordCount(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            FlagIndex fi = getFlagIndex(text, i);
            if (fi.isFlag()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public List<String> forbidWordList(String text) {
        List<String> words = Lists.newArrayList();
        for (int i = 0; i < text.length(); i++) {
            FlagIndex fi = getFlagIndex(text, i);
            if (fi.isFlag()) {
                StringBuilder builder = new StringBuilder();
                for (int j : fi.getIndex()) {
                    char word = text.charAt(j);
                    builder.append(word);
                }
                words.add(builder.toString());
            }
        }
        return words;
    }

    @Override
    public String replace(String text, char symbol) {
        char[] charset = text.toCharArray();
        for (int i = 0; i < text.length(); i++) {
            FlagIndex fi = getFlagIndex(text, i);
            if (fi.isFlag()) {
                for (int j : fi.getIndex()) {
                    charset[j] = symbol;
                }
            }
        }
        return new String(charset);
    }

    @Override
    public boolean addForbidWord(boolean refreshNow, String... words) {
        boolean result = forbidWordRepository.addForbidWord(words);
        log.info("Add forbidden words {} {}.", words, result ? "succeed" : "failed");
        if (result && refreshNow) {
            log.info("Start refreshing the forbidden word repository...");
            forbidWordRepository.refresh(true);
        }
        return result;
    }

    @Override
    public boolean removeForbidWord(boolean refreshNow, String... words) {
        boolean result = forbidWordRepository.removeForbidWord(words);
        log.info("Delete forbidden words {} {}.", words, result ? "succeed" : "failed");
        if (result && refreshNow) {
            log.info("Start refreshing the forbidden word repository...");
            forbidWordRepository.refresh(true);
        }
        return result;
    }

    @Override
    public void printDfa() {
        forbidWordRepository.getDfa().print();
    }

    private FlagIndex getFlagIndex(final String text, final int begin) {
        return forbidWordRepository.getDfa().getFlagIndex(text, begin);
    }

}
