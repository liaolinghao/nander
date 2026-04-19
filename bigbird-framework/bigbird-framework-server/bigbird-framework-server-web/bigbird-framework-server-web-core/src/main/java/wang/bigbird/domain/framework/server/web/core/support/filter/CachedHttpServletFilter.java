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

import org.springframework.web.util.ContentCachingRequestWrapper;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 将请求和响应替换为带缓存能力支持重复读取和输出功能的过滤器
 *
 * @author Bigbird
 */
public class CachedHttpServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        if (request instanceof HttpServletRequest) {
            String contentType = request.getContentType();
            boolean isFormType = StringUtils.isNotBlank(contentType) && (contentType.startsWith("application/x-www-form-urlencoded") || contentType.startsWith("multipart/form-data"));
            if (isFormType) {
                requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
            } else {
                requestWrapper = new CachedHttpServletRequestWrapper((HttpServletRequest) request);
            }
        }
        if (requestWrapper == null) {
            chain.doFilter(request, response);
        } else {
            CachedHttpServletResponseWrapper responseWrapper = new CachedHttpServletResponseWrapper((HttpServletResponse) response);
            chain.doFilter(requestWrapper, responseWrapper);
        }
    }

}
