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
package wang.bigbird.domain.framework.server.web.auth.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置
 *
 * @author Bigbird
 */
@Data
@Component
@ConfigurationProperties(prefix = "bigbird.server.web.auth.jwt")
public class JwtSecurityProperties {

    /**
     * 是否启用认证授权校验
     */
    private boolean enable;

    /**
     * Request Headers：Authorization
     */
    private String header;

    /**
     * 令牌前缀，最后留个空格 Bearer
     */
    private String tokenStartWith;

    /**
     * Base64对该令牌进行编码
     */
    private String base64Secret;

    /**
     * 令牌有效时间 此处单位：秒
     */
    private Integer tokenValidityInSeconds;

    /**
     * 刷新令牌有效时间 此处单位：天
     */
    private Integer refreshTokenValidityInDays;

    /**
     * 不需要认证的接口，以逗号分隔
     */
    private String withoutApi;

    /**
     * token签发者，比如：后端管理员操作系统~administer，后端客户操作系统~customer
     */
    private String issuer;

    /**
     * 返回令牌前缀
     */
    public String getTokenStartWith() {
        return tokenStartWith + " ";
    }
}
