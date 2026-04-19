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
package wang.bigbird.domain.framework.core.base.tool;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 编码工具类，对字符串进行编码解码处理
 *
 * @author Bigbird
 */
@Slf4j
public class Coder {

    /**
     * 默认编码
     */
    public static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * gb2312编码
     */
    public static final String GB2312_ENCODING = "gb2312";

    /**
     * UNICODE10编码的字符模式串
     */
    private static final Pattern UNICODE10_PATTERN = Pattern.compile("&#\\d+?;");

    /**
     * 字母大小写的跨度值
     */
    private static final int CASE_DIFF = ('a' - 'A');

    /**
     * 第一个小写字母
     */
    private static final char FIRST_LOWERCASE = 'a';

    /**
     * 最后一个小写字母
     */
    private static final char LAST_LOWERCASE = 'z';

    /**
     * 第一个大写字母
     */
    private static final char FIRST_UPPERCASE = 'A';

    /**
     * 最后一个大写字母
     */
    private static final char LAST_UPPERCASE = 'Z';

    /**
     * 第一个数字字符
     */
    private static final char FIRST_DIGITAL = '0';

    /**
     * 最后一个数字字符
     */
    private static final char LAST_DIGITAL = '9';

    /**
     * UNICODE编码串特征
     */
    private static final String UNICODE_FEATURE = "\\u";

    /**
     * UNICODE10编码串特征
     */
    private static final String UNICODE10_FEATURE = "&#";

    /**
     * 分号
     */
    private static final String SEMICOLON = ";";

    /**
     * 不转码的字符集合
     */
    private static final BitSet NOT_NEED_ENCODING;

    static {
        NOT_NEED_ENCODING = new BitSet(256);
        int i;
        for (i = FIRST_LOWERCASE; i <= LAST_LOWERCASE; i++) {
            NOT_NEED_ENCODING.set(i);
        }
        for (i = FIRST_UPPERCASE; i <= LAST_UPPERCASE; i++) {
            NOT_NEED_ENCODING.set(i);
        }
        for (i = FIRST_DIGITAL; i <= LAST_DIGITAL; i++) {
            NOT_NEED_ENCODING.set(i);
        }
        NOT_NEED_ENCODING.set('-');
        NOT_NEED_ENCODING.set('_');
        NOT_NEED_ENCODING.set('.');
        NOT_NEED_ENCODING.set('*');
    }

    /**
     * 获取本地文件编码，该编码是指main入口函数的java类文件编码
     *
     * @return 本地文件编码
     */
    public static String getLocalCharset() {
        return System.getProperty("file.encoding");
    }

    /**
     * 该方法相比URLEncoder.encode(s,"utf-8")的区别在于对空格，该方法使用20
     * %替换，而URLEncoder.encode(s,"utf-8")使用+号
     *
     * @param s 待编码的字符串
     * @return utf-8编码的字符串
     */
    public static String encodeUtf8(String s) {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        try {
            return encode(s, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException exp) {
            throw new RuntimeException("Unsupported UTF-8 Encoding");
        }
    }

    /**
     * 日文编码
     *
     * @param s 待编码的字符串
     * @return Shift_JIS编码的字符串
     */
    public static String encodeShiftJis(String s) {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        try {
            return encode(s, "Shift_JIS");
        } catch (UnsupportedEncodingException exp) {
            throw new RuntimeException("Unsupported Shift_JIS Encoding");
        }
    }

    /**
     * 采用指定编码格式对字符串进行编码
     *
     * @param s   待编码的字符串
     * @param enc 编码方式
     * @return 编码后的字符串
     * @throws UnsupportedEncodingException 不支持的编码类型错误
     */
    public static String encode(String s, String enc)
            throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        boolean needToChange = false;
        StringBuilder out = new StringBuilder(s.length());
        ByteArrayOutputStream buf = null;
        OutputStreamWriter writer = null;
        try {
            buf = new ByteArrayOutputStream(10);
            writer = new OutputStreamWriter(buf, enc);
            for (int i = 0; i < s.length(); i++) {
                int c = s.charAt(i);
                if (NOT_NEED_ENCODING.get(c)) {
                    if (c == ' ') {
                        c = '+';
                        needToChange = true;
                    }
                    out.append((char) c);
                } else {
                    try {
                        writer.write(c);
                        if (c >= 0xD800 && c <= 0xDBFF) {
                            if ((i + 1) < s.length()) {
                                int d = s.charAt(i + 1);
                                if (d >= 0xDC00 && d <= 0xDFFF) {
                                    writer.write(d);
                                    i++;
                                }
                            }
                        }
                        writer.flush();
                    } catch (IOException e) {
                        buf.reset();
                        continue;
                    }
                    byte[] ba = buf.toByteArray();
                    for (byte b : ba) {
                        out.append('%');
                        char ch = Character.forDigit((b >> 4) & 0xF, 16);
                        if (Character.isLetter(ch)) {
                            ch -= CASE_DIFF;
                        }
                        out.append(ch);
                        ch = Character.forDigit(b & 0xF, 16);
                        if (Character.isLetter(ch)) {
                            ch -= CASE_DIFF;
                        }
                        out.append(ch);
                    }
                    buf.reset();
                    needToChange = true;
                }
            }
        } finally {
            StreamUtils.close(writer, buf);
        }
        return (needToChange ? out.toString() : s);
    }

    /**
     * 使用URLEncoder的UTF-8编码方法
     *
     * @param s 待编码的字符串
     * @return 使用URLEncoder的UTF-8编码的字符串
     */
    public static String urlEncoderUtf8(String s) {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        try {
            return URLEncoder.encode(s, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported UTF-8 Encoding");
        }
    }

    /**
     * 使用URLDecoder的UTF-8解码方法
     *
     * @param s 待解码的字符串
     * @return 使用URLDecoder的UTF-8解码的字符串
     */
    public static String urlDecoderUtf8(String s) {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        try {
            return URLDecoder.decode(s, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported UTF-8 Decoding");
        }
    }

    /**
     * 使用URLEncoder的GB2312编码方法
     *
     * @param s 待编码的字符串
     * @return 使用URLEncoder的gb2312编码的字符串
     */
    public static String urlEncoderGb2312(String s) {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        try {
            return URLEncoder.encode(s, GB2312_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported GB2312 Encoding");
        }
    }

    /**
     * 使用URLDecoder的GB2312解码方法
     *
     * @param s 待解码的字符串
     * @return 使用URLDecoder的gb2312解码的字符串
     */
    public static String urlDecoderGb2312(String s) {
        if (StringUtils.isEmpty(s)) {
            return s;
        }
        try {
            return URLDecoder.decode(s, GB2312_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported GB2312 Decoding");
        }
    }

    /**
     * 将普通字符串转换为16位unicode编码的字符串
     *
     * @param str 普通字符串
     * @return 16位unicode编码的字符串
     */
    public static String convertUnicode(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        String tmp;
        StringBuffer sb = new StringBuffer(1000);
        char c;
        int i, j;
        sb.setLength(0);
        for (i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            sb.append(UNICODE_FEATURE);
            // 取出高8位
            j = (c >>> 8);
            tmp = Integer.toHexString(j);
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
            // 取出低8位
            j = (c & 0xFF);
            tmp = Integer.toHexString(j);
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return (new String(sb));
    }

    /**
     * 将16位unicode编码的字符串转换为普通字符串
     *
     * @param str 16位unicode编码的字符串
     * @return 普通字符串
     */
    public static String revertUnicode(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (!str.contains(UNICODE_FEATURE)) {
            // 如果不是unicode码则原样返回
            return str;
        }
        StringBuilder sb = new StringBuilder();
        int index = 0;
        int len = str.length();
        while (index < len) {
            char c = str.charAt(index);
            if (c == '\\' && index + 1 < len && str.charAt(index + 1) == 'u') {
                String unicode = str.substring(index + 2, index + 6);
                sb.append((char) Integer.parseInt(unicode, 16));
                index += 6;
            } else {
                sb.append(c);
                index++;
            }
        }
        return sb.toString();
    }

    /**
     * 将普通字符串转换为10位unicode编码的字符串
     *
     * @param str 普通字符串
     * @return 10位unicode编码的字符串
     */
    public static String convertUnicode10(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        StringBuffer sb = new StringBuffer(1000);
        char c;
        int i;
        sb.setLength(0);
        for (i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            sb.append(UNICODE10_FEATURE);
            String unicode = Integer.toHexString(c);
            // 16进制转换为10进制
            int d = Integer.parseInt(unicode, 16);
            sb.append(d);
            sb.append(SEMICOLON);
        }
        return (new String(sb));
    }

    /**
     * 将10位unicode编码的字符串转换为普通字符串
     *
     * @param str 10位unicode编码的字符串
     * @return 普通字符串
     */
    public static String revertUnicode10(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        if (!str.contains(UNICODE10_FEATURE)) {
            // 如果不是unicode码则原样返回
            return str;
        }
        StringBuilder sb = new StringBuilder(1000);
        Matcher matcher = UNICODE10_PATTERN.matcher(str);
        while (matcher.find()) {
            String s = matcher.group();
            s = s.substring(2, s.length() - 1);
            sb.append((char) Integer.parseInt(s));
        }
        return sb.toString();
    }

    /**
     * 对byte[]进行压缩
     *
     * @param data 要压缩的数据
     * @return 压缩后的数据
     */
    public static byte[] compressGzip(byte[] data) {
        if (data == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = null;
        GZIPOutputStream gzip = null;
        byte[] newData = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(byteArrayOutputStream);
            gzip.write(data);
            gzip.finish();
            gzip.flush();
            newData = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("CompressGZIP:", e);
        } finally {
            StreamUtils.close(gzip, byteArrayOutputStream);
        }
        return newData;
    }

    /**
     * 对byte[]进行解压缩
     *
     * @param data 要解压缩的数据
     * @return 解压缩后的数据
     */
    public static byte[] decompressGzip(byte[] data) {
        if (data == null) {
            return null;
        }
        ByteArrayInputStream bis = null;
        GZIPInputStream gzip = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] newData = null;
        try {
            bis = new ByteArrayInputStream(data);
            gzip = new GZIPInputStream(bis);
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int num;
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                byteArrayOutputStream.write(buf, 0, num);
            }
            byteArrayOutputStream.flush();
            newData = byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("DecompressGZIP:", e);
        } finally {
            StreamUtils.close(byteArrayOutputStream, gzip, bis);
        }
        return newData;
    }

}

