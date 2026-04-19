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
package wang.bigbird.domain.framework.server.web.core.base.tool;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.DefaultCorsProcessor;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义跨域处理器，功能为在响应头中添加跨域相关的请求头
 *
 * @author Bigbird
 */
public class CustomerCorsProcessor extends DefaultCorsProcessor {

    private CorsConfiguration corsConfiguration;

    public CustomerCorsProcessor(CorsConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    @Override
    public boolean processRequest(@Nullable CorsConfiguration configuration, HttpServletRequest request,
                                  HttpServletResponse response) throws IOException {
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, StringUtils.collectionToCommaDelimitedString(corsConfiguration.getAllowedOrigins()));
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, StringUtils.collectionToCommaDelimitedString(corsConfiguration.getAllowedMethods()));
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, StringUtils.collectionToCommaDelimitedString(corsConfiguration.getAllowedHeaders()));
        response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf(corsConfiguration.getAllowCredentials()));
        return super.processRequest(configuration, request,
                response);
    }

}
