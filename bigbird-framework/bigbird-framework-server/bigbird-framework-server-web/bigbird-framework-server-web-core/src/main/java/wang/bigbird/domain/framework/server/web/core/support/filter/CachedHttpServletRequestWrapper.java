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


import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;


/**
 * 带缓存能力的请求对象包装器，
 * 用于解决在拦截器中读取了body后无法在controller中再次读取的问题，
 * 从中可直接获得请求传递的各种参数。
 *
 * @author Bigbird
 */
public class CachedHttpServletRequestWrapper extends
        HttpServletRequestWrapper {

    private String requestURI;

    private String body;

    private Map<String, String[]> parameterMap;

    public CachedHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        initUri(request);
        initParameterMap(request);
        initBody(request);
    }

    private void initUri(HttpServletRequest request) {
        this.requestURI = request.getRequestURI();
    }

    /**
     * 获取请求params
     *
     * @param request
     */
    private void initParameterMap(HttpServletRequest request) {
        this.parameterMap = request.getParameterMap();
    }

    /**
     * 获取请求Body
     *
     * @param request
     * @return
     */
    public void initBody(ServletRequest request) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            this.body = sb.toString();
        }
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    /**
     * 提供方法用于更新解密后的参数
     *
     * @param parameterMap
     */
    public void setParameterMap(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public String getBody() {
        return body;
    }

    /**
     * 提供方法用于更新解密后的 JSON 体
     *
     * @param body
     */
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public Enumeration getParameterNames() {
        Vector vector = new Vector(parameterMap.keySet());
        return vector.elements();
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameterMap.get(name);
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameterMap.get(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (StringUtils.isBlank(body)) {
            return super.getInputStream();
        }
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return new ServletInputStream() {

            @Override
            public int read() {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }

}
