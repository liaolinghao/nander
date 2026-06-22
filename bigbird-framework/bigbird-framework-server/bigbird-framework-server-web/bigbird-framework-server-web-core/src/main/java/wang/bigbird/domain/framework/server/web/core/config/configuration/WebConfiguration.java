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
package wang.bigbird.domain.framework.server.web.core.config.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.core.config.property.WebProperties;
import wang.bigbird.domain.framework.server.web.core.support.interceptor.TraceInterceptor;
import wang.bigbird.domain.framework.server.web.core.support.resolver.DecryptPathVariableResolver;
import wang.bigbird.domain.framework.server.web.core.support.resolver.DecryptRequestParamResolver;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Web配置
 *
 * @author Bigbird
 */
@Slf4j
@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {

    @Resource
    private WebProperties webProperties;

    @Autowired
    private TraceInterceptor traceInterceptor;
    @Autowired
    private DecryptRequestParamResolver decryptRequestParamResolver;
    @Autowired
    private DecryptPathVariableResolver decryptPathVariableResolver;

    /**
     * 全局配置转换器，支持Controller的@RequestParam LocalDateTime 参数，
     * 都能自动解析 yyyy-MM-dd HH:mm:ss
     *
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateTimeFormatter(DateUtils.FORMATTER_24H_STANDARD);
        registrar.registerFormatters(registry);
    }

    /**
     * trace拦截器放置在最开始执行，以便从请求处理开始就添加请求链路ID
     *
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceInterceptor).order(0);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(decryptRequestParamResolver);
        resolvers.add(decryptPathVariableResolver);
    }

    /**
     * 增加自定义静态目录访问路径
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        // 预定义static目录用于保存小的固定名称的配置文件，不设置缓存对性能影响不大
        // 而自定义静态目录一般是保存业务上传的文件（可能较大），每一次上传文件（即便同一份文件）采用uuid命名，
        // 以保证文件访问路径不重复，因此，不需要采用协商缓存，采用强制缓存，并且缓存7天
        Map<String, String> pattern = webProperties.getPattern();
        if (MapUtils.isNotEmpty(pattern)) {
            pattern.forEach((key, value) -> registry.addResourceHandler(StringUtils.joinStr("/", key, "/**")).addResourceLocations(value).setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic()));
        }
    }

    /**
     * 将自定义MappingJackson2HttpMessageConverter放置在消息转换器首位，
     * 可解决@RequestBody将字段值前后空格自动去除的问题
     *
     * @param converters
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter() {
            @Override
            protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException {
                if (object instanceof String) {
                    Charset charset = this.getDefaultCharset();
                    StreamUtils.copy((String) object, charset, outputMessage.getBody());
                } else {
                    super.writeInternal(object, type, outputMessage);
                }
            }
        };
        ObjectMapper objectMapper = JsonUtils.getRegisterMapper();
        JsonUtils.registerJavaLong2StringModule(objectMapper);
        converter.setObjectMapper(objectMapper);
        converter.setDefaultCharset(Charset.defaultCharset());
        converters.add(0, converter);
    }

}
