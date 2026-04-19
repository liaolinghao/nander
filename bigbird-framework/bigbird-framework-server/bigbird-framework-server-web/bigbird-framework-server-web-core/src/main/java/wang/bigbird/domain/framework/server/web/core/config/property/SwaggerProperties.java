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
package wang.bigbird.domain.framework.server.web.core.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Swagger配置，用于决定是否采用Swagger生成API文档
 *
 * @author Bigbird
 */
@Data
@Component
@ConfigurationProperties(prefix = "bigbird.server.web.core.swagger")
public class SwaggerProperties {
    /**
     * 是否swagger2启用，默认不启用
     */
    private Boolean enable = false;
    /**
     * 扫描包路径，可以不指定，系统会通过自动扫描{@link io.swagger.annotations.ApiOperation}
     */
    private String basePackage;
    /**
     * 标题
     */
    private String title;
    /**
     * 应用描述
     */
    private String description;
    /**
     * 服务地址，API接口的网址域名前缀
     */
    private String serviceUrl = "bigbird.wang";
    /**
     * 版本，默认V1.0.0
     */
    private String version = "V1.0.0";
    /**
     * 许可协议名称，默认bigbird
     */
    private String license = "bigbird";
    /**
     * 许可协议访问地址
     */
    private String licenseUrl = "www.bigbird.wang";
    /**
     * 主机，swagger接口文档服务访问地址
     */
    private String host;
}
