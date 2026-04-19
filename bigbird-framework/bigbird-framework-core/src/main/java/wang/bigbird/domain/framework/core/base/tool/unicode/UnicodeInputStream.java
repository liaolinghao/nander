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

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * This inputstream will recognize unicode BOM marks and will skip bytes if
 * getEncoding() method is called before any of the read(...) methods.
 * <p>
 * Usage pattern: String enc = "ISO-8859-1"; // or NULL to use systemdefault
 * FileInputStream fis = new FileInputStream(file); UnicodeInputStream uin = new
 * UnicodeInputStream(fis, enc); enc = uin.getEncoding(); // check and skip
 * possible BOM bytes InputStreamReader in; if (enc == null) in = new
 * InputStreamReader(uin); else in = new InputStreamReader(uin, enc);
 *
 * @author Bigbird
 */
public class UnicodeInputStream extends InputStream {

    public static final int BOM_SIZE = 4;

    public static final byte BOM_UTF32BE_FIRST_BYTE = (byte) 0x00;
    public static final byte BOM_UTF32BE_SECOND_BYTE = (byte) 0x00;
    public static final byte BOM_UTF32BE_THIRD_BYTE = (byte) 0xFE;
    public static final byte BOM_UTF32BE_FOURTH_BYTE = (byte) 0xFF;

    public static final byte BOM_UTF32LE_FIRST_BYTE = (byte) 0xFF;
    public static final byte BOM_UTF32LE_SECOND_BYTE = (byte) 0xFE;
    public static final byte BOM_UTF32LE_THIRD_BYTE = (byte) 0x00;
    public static final byte BOM_UTF32LE_FOURTH_BYTE = (byte) 0x00;

    public static final byte BOM_UTF8_FIRST_BYTE = (byte) 0xEF;
    public static final byte BOM_UTF8_SECOND_BYTE = (byte) 0xBB;
    public static final byte BOM_UTF8_THIRD_BYTE = (byte) 0xBF;

    public static final byte BOM_UTF16BE_FIRST_BYTE = (byte) 0xFE;
    public static final byte BOM_UTF16BE_SECOND_BYTE = (byte) 0xFF;

    public static final byte BOM_UTF16LE_FIRST_BYTE = (byte) 0xFF;
    public static final byte BOM_UTF16LE_SECOND_BYTE = (byte) 0xFE;

    private PushbackInputStream pushbackInputStream;

    private boolean isInited = false;

    private String defaultEncoding;

    private String encoding;

    public UnicodeInputStream(InputStream in, String defaultEncoding) {
        pushbackInputStream = new PushbackInputStream(in, BOM_SIZE);
        this.defaultEncoding = defaultEncoding;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    public String getEncoding() {
        if (!isInited) {
            try {
                init();
            } catch (IOException ex) {
                IllegalStateException ise = new IllegalStateException(
                        "Init method failed.");
                ise.initCause(ise);
                throw ise;
            }
        }
        return encoding;
    }

    /**
     * Read-ahead four bytes and check for BOM marks. Extra bytes are unread
     * back to the stream, only BOM bytes are skipped.
     */
    protected void init() throws IOException {
        if (isInited) {
            return;
        }
        byte[] bom = new byte[BOM_SIZE];
        int n, unread;
        n = pushbackInputStream.read(bom, 0, bom.length);
        if ((bom[0] == BOM_UTF32BE_FIRST_BYTE) && (bom[1] == BOM_UTF32BE_SECOND_BYTE)
                && (bom[2] == BOM_UTF32BE_THIRD_BYTE) && (bom[3] == BOM_UTF32BE_FOURTH_BYTE)) {
            encoding = "UTF-32BE";
            unread = n - 4;
        } else if ((bom[0] == BOM_UTF32LE_FIRST_BYTE) && (bom[1] == BOM_UTF32LE_SECOND_BYTE)
                && (bom[2] == BOM_UTF32LE_THIRD_BYTE) && (bom[3] == BOM_UTF32LE_FOURTH_BYTE)) {
            encoding = "UTF-32LE";
            unread = n - 4;
        } else if ((bom[0] == BOM_UTF8_FIRST_BYTE) && (bom[1] == BOM_UTF8_SECOND_BYTE)
                && (bom[2] == BOM_UTF8_THIRD_BYTE)) {
            encoding = "UTF-8";
            unread = n - 3;
        } else if ((bom[0] == BOM_UTF16BE_FIRST_BYTE) && (bom[1] == BOM_UTF16BE_SECOND_BYTE)) {
            encoding = "UTF-16BE";
            unread = n - 2;
        } else if ((bom[0] == BOM_UTF16LE_FIRST_BYTE) && (bom[1] == BOM_UTF16LE_SECOND_BYTE)) {
            encoding = "UTF-16LE";
            unread = n - 2;
        } else {
            // Unicode BOM mark not found, unread all bytes
            encoding = defaultEncoding;
            unread = n;
        }
        if (unread > 0) {
            pushbackInputStream.unread(bom, (n - unread), unread);
        }
        isInited = true;
    }

    @Override
    public void close() throws IOException {
        init();
        pushbackInputStream.close();
    }

    @Override
    public int read() throws IOException {
        init();
        return pushbackInputStream.read();
    }
}
