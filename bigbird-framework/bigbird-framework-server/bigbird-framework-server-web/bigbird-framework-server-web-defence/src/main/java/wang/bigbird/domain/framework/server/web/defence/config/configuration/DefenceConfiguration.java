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
package wang.bigbird.domain.framework.server.web.defence.config.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.core.support.filter.CachedHttpServletFilter;
import wang.bigbird.domain.framework.server.web.defence.config.property.DefenceProperties;
import wang.bigbird.domain.framework.server.web.defence.support.interceptor.DecryptAndEncryptInterceptor;
import wang.bigbird.domain.framework.server.web.defence.support.interceptor.DefenceInterceptor;

import javax.annotation.Resource;
import java.util.Arrays;

/**
 * Defence配置
 *
 * @author Bigbird
 */
@Configuration
public class DefenceConfiguration implements WebMvcConfigurer {

    @Resource
    private DefenceProperties defenceProperties;

    @Autowired
    private DecryptAndEncryptInterceptor decryptAndEncryptInterceptor;
    @Autowired
    private DefenceInterceptor defenceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (StringUtils.isBlank(defenceProperties.getPatterns())) {
            return;
        }
        String[] patterns = defenceProperties.getPatterns().split(CommonConstants.COMMA);
        registry.addInterceptor(decryptAndEncryptInterceptor).addPathPatterns(patterns);
        registry.addInterceptor(defenceInterceptor).addPathPatterns(patterns);
    }

    @Bean
    public FilterRegistrationBean<CachedHttpServletFilter> cachedHttpServletFilter() {
        FilterRegistrationBean<CachedHttpServletFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CachedHttpServletFilter());
        bean.setName("cachedHttpServletFilter");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        // 在 Spring Boot 中，若通过 FilterRegistrationBean 注册过滤器并使用 /*，
        // 框架会特殊处理使其等效于 /**（匹配所有路径），这是 Spring Boot 对 Filter 的扩展，方便开发者使用。
        String[] patterns = defenceProperties.getPatterns().split(CommonConstants.COMMA);
        if (patterns.length > 0) {
            bean.addUrlPatterns(patterns);
        } else {
            bean.setUrlPatterns(Arrays.asList("/*"));
        }
        return bean;
    }

}
