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
package wang.bigbird.domain.framework.server.web.auth.domain.pojo;

import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import wang.bigbird.domain.framework.server.web.core.base.enums.ChannelEnum;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * JWT认证权限基础数据
 *
 * @author Bigbird
 */
@Data
public class JwtAuthData implements Serializable {

    /**
     * 认证对象类型是用户
     */
    public static final String USER = "user";

    /**
     * 认证对象类型是设备
     */
    public static final String DEVICE = "device";

    /**
     * 认证对象类型是应用
     */
    public static final String CLIENT = "client";

    /**
     * 是否是有状态token
     * 有状态：后端存储Token状态，支持主动失效
     * 无状态：后端无存储，仅验签，仅依赖过期时间，无法主动失效
     */
    private Boolean isStateful = true;

    /**
     * 认证对象标识，比如：
     * 认证给用户该值为用户ID，
     * 认证给设备该值为设备ID，
     * 认证给应用该值为应用ID
     */
    private Long id;

    /**
     * 租户ID，应用于多租户场景，默认为0，表示无租户场景
     */
    private Long tenantId = 0L;

    /**
     * 认证对象访问渠道
     */
    private ChannelEnum channel;

    /**
     * 认证对象类型，比如：
     * 用户user、设备device、应用client
     */
    private String type;

    /**
     * 授予的权限列表
     */
    private List<SimpleGrantedAuthority> grantedAuthorityList;

    /**
     * 基本信息
     */
    private Map<String, Object> infos;

    /**
     * 权限数据
     */
    private String authorities;

}
