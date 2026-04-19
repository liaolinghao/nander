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
package wang.bigbird.domain.framework.server.web.core.support.filter;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.tool.Coder;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 * 响应对象包装器，缓存输出流数据
 * <p>
 * 由于 HttpServletResponse 的输出流一旦被写入就无法直接修改，
 * 因此需要通过包装响应对象（如 HttpServletResponseWrapper）缓存响应数据，
 * 加密后再写入原始响应流。
 *
 * @author Bigbird
 */
@Slf4j
public class CachedHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private ByteArrayOutputStream buffer;

    private ServletOutputStream out;

    private PrintWriter writer;

    public CachedHttpServletResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        init();
    }

    private void init() throws IOException {
        buffer = new ByteArrayOutputStream();
        out = new OutputStreamWrapper(buffer);
        writer = new PrintWriter(new OutputStreamWriter(buffer));
    }

    public String getContent() throws IOException {
        flushBuffer();
        return buffer.toString();
    }

    public void write(String data) throws IOException {
        ServletResponse response = getResponse();
        response.reset();
        response.setCharacterEncoding(Coder.DEFAULT_ENCODING);
        response.getWriter().write(data);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        return writer;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (out != null) {
            out.flush();
        }
        if (writer != null) {
            writer.flush();
        }
    }

    @Override
    public void reset() {
        buffer.reset();
    }

    private class OutputStreamWrapper extends ServletOutputStream {

        private ByteArrayOutputStream bos;

        public OutputStreamWrapper(ByteArrayOutputStream byteArrayOutputStream) throws IOException {
            bos = byteArrayOutputStream;
        }

        @Override
        public void write(int b) throws IOException {
            bos.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            bos.write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            bos.write(b, off, len);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {

        }
    }

}
