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
import wang.bigbird.domain.framework.server.web.core.base.enums.CryptoEnum;

import java.util.Map;

import static wang.bigbird.domain.framework.server.web.core.base.enums.CryptoEnum.SIMPLE;


/**
 * WEB框架通用配置
 *
 * @author Bigbird
 */
@Data
@Component
@ConfigurationProperties(prefix = "bigbird.server.web.core")
public class WebProperties {

    /**
     * 加解密类型
     */
    private CryptoEnum crypto = SIMPLE;

    /**
     * 是否启动接口响应加密
     */
    private Boolean enableEncrypt = true;

    /**
     * 加密密钥，对于非对称密钥算法，可配置公钥
     */
    private String encryptKey;

    /**
     * 是否启动接口请求解密
     */
    private Boolean enableDecrypt = true;

    /**
     * 解密密钥，对于非对称密钥算法，可配置私钥
     */
    private String decryptKey;

    /**
     * 业务数据目录，默认采用static，但是实际部署时建议区分目录，将static作为配置文件目录
     * 业务数据存储改用其他目录
     */
    private String dir = "static";

    /**
     * 静态目录映射关系配置
     */
    private Map<String, String> pattern;

    /**
     * 线程池配置
     */
    private ThreadPoolProperties pool;

    /**
     * 跨域配置
     */
    private CorsProperties cors;

    /**
     * swagger配置类
     */
    private SwaggerProperties swagger;

    /**
     * jackson 配置类
     */
    private JacksonProperties jackson;

}
