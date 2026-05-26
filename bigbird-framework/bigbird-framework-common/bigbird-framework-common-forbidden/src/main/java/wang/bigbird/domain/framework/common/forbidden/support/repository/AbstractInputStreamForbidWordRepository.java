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
import wang.bigbird.domain.framework.core.base.util.StreamUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 从文本文件加载禁用词，每行一个禁用词
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractInputStreamForbidWordRepository extends AbstractForbidWordRepository {

    /**
     * 传入一个空的DFA实例
     *
     * @param dfa DFA实例
     */
    protected AbstractInputStreamForbidWordRepository(Dfa dfa) {
        super(dfa);
    }

    @Override
    public final Iterator<String> loadForbidWords() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream()));
            return new BufferedReaderIterator(reader);
        } catch (Exception e) {
            log.error("LoadForbidWords:", e);
        }
        return null;
    }

    /**
     * 获取禁用词内容输入流
     *
     * @return 禁用词内容输入流
     * @throws IOException
     */
    protected abstract InputStream getInputStream() throws IOException;

    class BufferedReaderIterator implements Iterator<String> {

        private BufferedReader reader;

        private String buf;

        public BufferedReaderIterator(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = false;
            try {
                hasNext = reader != null && (buf = reader.readLine()) != null;
                if (!hasNext) {
                    StreamUtils.close(reader);
                    reader = null;
                }
            } catch (Exception e) {
                log.error("HasNext:", e);
            }
            return hasNext;
        }

        @Override
        public String next() {
            if (buf == null) {
                throw new NoSuchElementException();
            }
            return buf;
        }
    }

}
