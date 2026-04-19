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
package wang.bigbird.domain.framework.server.web.ws.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * WS框架通用配置
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.server.web.ws")
public class WsProperties {

    /**
     * 是否开启Spring WS机制，默认开启
     * 如果关闭，则采用Tomcat WS机制
     */
    private Boolean springEnabled = true;
    /**
     * 建立全双工透传关系的目标WS
     */
    private String target;
    /**
     * 透传ws路径模式
     */
    private String relayPath;
    /**
     * 默认是*
     * 允许跨域的域名
     */
    private List<String> allowedOrigins;

}
