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
package wang.bigbird.domain.framework.server.web.defence.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 安防框架通用配置
 *
 * @author Bigbird
 */
@Data
@Component
@ConfigurationProperties(prefix = "bigbird.server.web.defence")
public class DefenceProperties {

    /**
     * 安全访问控制接口模式，以逗号分隔，拦截器使用
     * 一个*：表示匹配路径下直接资源，不包含下级目录资源
     * 两个*：表示匹配路径下任意级目录资源
     * 标准 Filter 仅支持 *（单层匹配），** 依赖 Spring Boot 的扩展
     * 拦截所有内容：/**
     * 拦截部分内容：/admin/**
     */
    private String patterns;

    /**
     * 加解密处理的关键字段名称集合
     */
    private Set<String> keyFields;

}
