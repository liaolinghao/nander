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
package wang.bigbird.domain.framework.core.base.tool.unicode;

import java.io.*;

/**
 * Generic unicode textreader, which will use BOM mark to identify the encoding
 * to be used. If BOM is not found then use a given default or system encoding.
 *
 * @author Bigbird
 */
public class UnicodeReader extends Reader {

    private UnicodeInputStream unicodeInputStream;

    private InputStreamReader inputStreamReader = null;

    private String defaultEncoding;

    /**
     * @param in              inputstream to be read
     * @param defaultEncoding default encoding if stream does not have BOM marker. Give NULL
     *                        to use system-level default.
     */
    public UnicodeReader(InputStream in, String defaultEncoding) {
        unicodeInputStream = new UnicodeInputStream(in, defaultEncoding);
        this.defaultEncoding = defaultEncoding;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Get stream encoding or NULL if stream is uninitialized. Call init() or
     * read() method to initialize it.
     */
    public String getEncoding() {
        if (inputStreamReader == null) {
            return null;
        }
        return inputStreamReader.getEncoding();
    }

    /**
     * Read-ahead four bytes and check for BOM marks. Extra bytes are unread
     * back to the stream, only BOM bytes are skipped.
     */
    protected void init() throws IOException {
        if (inputStreamReader != null) {
            return;
        }
        unicodeInputStream.init();
        inputStreamReader = new InputStreamReader(unicodeInputStream, unicodeInputStream.getEncoding());
    }

    @Override
    public void close() throws IOException {
        init();
        inputStreamReader.close();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        init();
        return inputStreamReader.read(cbuf, off, len);
    }

}
