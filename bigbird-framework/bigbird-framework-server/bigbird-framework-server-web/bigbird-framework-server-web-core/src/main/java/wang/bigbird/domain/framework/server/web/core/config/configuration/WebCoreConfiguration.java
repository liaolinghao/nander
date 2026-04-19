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

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import wang.bigbird.domain.framework.common.logging.core.base.util.ThreadMdcUtils;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.server.core.exception.BusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.web.core.base.tool.CustomerCorsProcessor;
import wang.bigbird.domain.framework.server.web.core.config.property.CorsProperties;
import wang.bigbird.domain.framework.server.web.core.config.property.JacksonProperties;
import wang.bigbird.domain.framework.server.web.core.config.property.ThreadPoolProperties;
import wang.bigbird.domain.framework.server.web.core.support.converter.StringToCollectionConverter;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * WEB框架配置
 *
 * @author Bigbird
 */
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.server.web.core")
@Configuration
public class WebCoreConfiguration {

    @Value("${spring.jackson.date-format:yyyy-MM-dd HH:mm:ss}")
    private String pattern;

    @Autowired
    private ThreadPoolProperties threadPoolProperties;

    @Autowired
    private JacksonProperties jacksonProperties;

    @Autowired
    private GenericConversionService conversionService;

    @PostConstruct
    public void init() {
        log.info("Init core web framework.");
        // 解决分页条件中，单 sort 字段被逗号分隔的问题
        // Order排序表示式，格式为：fieldName,asc，如果按照逗号分隔转换，
        // 会导致字段名称和排序方式被当成两个字段名称导致错误，所以去掉原始的转换器采用自定义的转换器
        conversionService.removeConvertible(String.class, Collection.class);
        conversionService.addConverter(new StringToCollectionConverter(conversionService));
    }

    @Bean("asyncTaskExecutor")
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor asyncTaskExecutor = new ThreadPoolTaskExecutor();
        asyncTaskExecutor.setMaxPoolSize(threadPoolProperties.getMaxPoolSize());
        asyncTaskExecutor.setCorePoolSize(threadPoolProperties.getCorePoolSize());
        asyncTaskExecutor.setQueueCapacity(threadPoolProperties.getQueueCapacity());
        asyncTaskExecutor.setKeepAliveSeconds(threadPoolProperties.getKeepAliveSeconds());
        asyncTaskExecutor.setThreadNamePrefix(threadPoolProperties.getThreadNamePrefix());
        asyncTaskExecutor.setTaskDecorator(runnable -> ThreadMdcUtils.wrapAsync(runnable, MDC.getCopyOfContextMap()));
        switch (threadPoolProperties.getPolicy()) {
            case ABORT:
                asyncTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
                break;
            case CALLER_RUNS:
                asyncTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
                break;
            case DISCARD:
                asyncTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
                break;
            case DISCARD_OLDEST:
                asyncTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
                break;
            default:
                asyncTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        }
        asyncTaskExecutor.initialize();
        return asyncTaskExecutor;
    }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 设置java.util.Date时间类的序列化以及反序列化的格式
            builder.dateFormat(new SimpleDateFormat(pattern));
            // 初始化JavaTimeModule
            String setNullDateTime = jacksonProperties.getSetNullDateTime();
            JavaTimeModule javaTimeModule = JsonUtils.createJavaTimeModule(pattern, DateUtils.DATE_PATTERN, DateUtils.TIME_PATTERN, setNullDateTime);
            // 注册时间模块, 支持支持jsr310, 即新的时间类(java.time包下的时间类)
            builder.modules(javaTimeModule);
            if (jacksonProperties.isLongToString()) {
                // 全局配置响应数据中的Long转String，解决Long型被JS读取产生的精度丢失问题
                builder.serializerByType(Long.class, ToStringSerializer.instance)
                        .serializerByType(Long.TYPE, ToStringSerializer.instance)
                        .serializerByType(BigInteger.class, ToStringSerializer.instance);
            }
        };
    }

    /**
     * LocalDate转换器，用于转换RequestParam和PathVariable参数
     * 千万不要试图转为 lambda 表达式！请忽略 IDE 的提示！
     * lambda表达式会让产生的转换器无法解析什么是传入类型，什么是目标类型
     */
    @Bean
    public Converter<String, LocalDate> localDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                return LocalDate.parse(source, DateTimeFormatter.ofPattern(DateUtils.DATE_PATTERN));
            }
        };
    }

    /**
     * LocalDateTime转换器，用于转换RequestParam和PathVariable参数
     * 千万不要试图转为 lambda 表达式！请忽略 IDE 的提示！
     * lambda表达式会让产生的转换器无法解析什么是传入类型，什么是目标类型
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(pattern));
            }
        };
    }

    /**
     * LocalTime转换器，用于转换RequestParam和PathVariable参数
     * 千万不要试图转为 lambda 表达式！请忽略 IDE 的提示！
     * lambda表达式会让产生的转换器无法解析什么是传入类型，什么是目标类型
     */
    @Bean
    public Converter<String, LocalTime> localTimeConverter() {
        return new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String source) {
                return LocalTime.parse(source, DateTimeFormatter.ofPattern(DateUtils.TIME_PATTERN));
            }
        };
    }

    /**
     * Date转换器，用于转换RequestParam和PathVariable参数
     * 千万不要试图转为 lambda 表达式！请忽略 IDE 的提示！
     * lambda表达式会让产生的转换器无法解析什么是传入类型，什么是目标类型
     */
    @Bean
    public Converter<String, Date> dateConverter() {
        return new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                try {
                    return format.parse(source);
                } catch (ParseException e) {
                    throw new BusinessException(IBaseResponseStatus.INVALID_REQUEST);
                }
            }
        };
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "bigbird.server.web.core.cors",
            name = {"enable"},
            havingValue = "true"
    )
    @ConditionalOnMissingBean(value = CorsFilter.class)
    public CorsFilter corsFilter(CorsProperties corsProperties) {
        log.debug("Cors enabled.");
        CorsConfiguration corsConfiguration = generatorCorsConfiguration(corsProperties);
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        CorsFilter corsFilter = new CorsFilter(urlBasedCorsConfigurationSource);
        corsFilter.setCorsProcessor(new CustomerCorsProcessor(corsConfiguration));
        return corsFilter;
    }

    private CorsConfiguration generatorCorsConfiguration(CorsProperties corsProperties) {
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        List<String> allowedOrigins = corsProperties.getAllowedOrigins();
        if (Objects.isNull(allowedOrigins)) {
            corsConfiguration.addAllowedOrigin(CorsConfiguration.ALL);
        } else {
            corsConfiguration.setAllowedOrigins(allowedOrigins);
        }
        List<String> allowedHeaders = corsProperties.getAllowedHeaders();
        if (Objects.isNull(allowedHeaders)) {
            corsConfiguration.addAllowedHeader(CorsConfiguration.ALL);
        } else {
            corsConfiguration.setAllowedHeaders(allowedHeaders);
        }
        List<String> exposedHeaders = corsProperties.getExposedHeaders();
        if (CollectionUtils.isNotEmpty(exposedHeaders)) {
            corsConfiguration.setExposedHeaders(exposedHeaders);
        }
        List<String> allowedMethods = corsProperties.getAllowedMethods();
        if (Objects.isNull(allowedMethods)) {
            corsConfiguration.addAllowedMethod(CorsConfiguration.ALL);
        } else {
            corsConfiguration.setAllowedMethods(allowedMethods);
        }
        Boolean allowCredentials = corsProperties.getAllowCredentials();
        corsConfiguration.setAllowCredentials(allowCredentials);
        corsConfiguration.setMaxAge(corsProperties.getMaxAge());
        return corsConfiguration;
    }


}
