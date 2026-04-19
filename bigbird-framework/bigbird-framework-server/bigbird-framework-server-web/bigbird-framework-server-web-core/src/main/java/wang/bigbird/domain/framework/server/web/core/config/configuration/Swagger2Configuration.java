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

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.oas.configuration.OpenApiDocumentationConfiguration;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.core.config.property.SwaggerProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.function.Predicate;

import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.builders.RequestHandlerSelectors.withMethodAnnotation;


/**
 * Swagger配置
 *
 * @author Bigbird
 */
@ConditionalOnMissingBean(Docket.class)
@Slf4j
@Configuration
@Import(OpenApiDocumentationConfiguration.class)
@ConditionalOnWebApplication
public class Swagger2Configuration {

    @Resource
    private SwaggerProperties swaggerProperties;

    @PostConstruct
    public void init() {
        log.info("Init swagger config.");
    }

    @Bean
    public Docket createRestApi() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo());
        String host = swaggerProperties.getHost();
        if (StringUtils.isNotBlank(host)) {
            docket.host(host);
        }
        return docket
                .select()
                // 指定controller存放的目录路径
                .apis(apis())
                .paths(PathSelectors.any())
                .build()
                .enable(isEnable());
    }

    private Boolean isEnable() {
        Boolean enable = swaggerProperties.getEnable();
        if (enable) {
            log.debug("Enable swagger2.");
        }
        return enable;
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 文档标题
                .title(swaggerProperties.getTitle())
                // 文档描述
                .description(swaggerProperties.getDescription())
                .termsOfServiceUrl(swaggerProperties.getServiceUrl())
                .version(swaggerProperties.getVersion())
                .license(swaggerProperties.getLicense())
                .licenseUrl(swaggerProperties.getLicenseUrl())
                .build();
    }

    private Predicate<RequestHandler> apis() {
        String basePackage = swaggerProperties.getBasePackage();
        return StringUtils.isBlank(basePackage) ? withMethodAnnotation(ApiOperation.class) : basePackage(basePackage);
    }

}
