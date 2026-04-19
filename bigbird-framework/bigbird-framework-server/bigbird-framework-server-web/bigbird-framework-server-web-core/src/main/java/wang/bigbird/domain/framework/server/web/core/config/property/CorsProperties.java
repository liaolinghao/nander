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

import java.util.List;


/**
 * 跨域配置
 *
 * @author Bigbird
 */
@Data
@Component
@ConfigurationProperties(prefix = "bigbird.server.web.core.cors")
public class CorsProperties {
    /**
     * 是否开启跨域访问策略
     */
    private Boolean enable;
    /**
     * 默认是*
     * 允许跨域的域名
     */
    private List<String> allowedOrigins;
    /**
     * 默认是*
     * 例如：{GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE}
     */
    private List<String> allowedMethods;
    /**
     * 默认是*
     * 允许跨域携带的请求头
     */
    private List<String> allowedHeaders;
    /**
     * 允许跨域暴露的请求头
     */
    private List<String> exposedHeaders;
    /**
     * 默认是 true
     * 设置为true时，跨域请求也会带上cookie信息
     */
    private Boolean allowCredentials = true;
    /**
     * 预检请求的有效期，以秒为单位。在有效期内，避免浏览器为跨域发送多次预检请求
     * 预检请求Method为OPTIONS
     */
    private Long maxAge;

}
