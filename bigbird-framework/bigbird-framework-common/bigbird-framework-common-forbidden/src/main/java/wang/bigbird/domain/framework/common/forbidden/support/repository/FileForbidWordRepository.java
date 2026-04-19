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
package wang.bigbird.domain.framework.common.forbidden.support.repository;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.forbidden.support.core.Dfa;

import java.io.InputStream;

/**
 * 从文本文件加载禁用词，每行一个禁用词
 *
 * @author Bigbird
 */
@Slf4j
public class FileForbidWordRepository extends AbstractInputStreamForbidWordRepository {

    private final String filePath;

    /**
     * Instantiates a new Forbid word repository.
     *
     * @param dfa the dfa
     */
    public FileForbidWordRepository(Dfa dfa, String filePath) {
        super(dfa);
        this.filePath = filePath;
        refresh(false);
    }

    @Override
    protected InputStream getInputStream() {
        log.info("Load the forbidden words file: {}.", filePath);
        return getClass().getClassLoader().getResourceAsStream(filePath);
    }

}
