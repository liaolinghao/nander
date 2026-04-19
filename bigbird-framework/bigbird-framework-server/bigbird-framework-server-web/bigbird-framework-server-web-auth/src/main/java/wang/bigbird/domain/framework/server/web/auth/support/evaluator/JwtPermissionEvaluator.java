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
package wang.bigbird.domain.framework.server.web.auth.support.evaluator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.JwtAuthData;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.user.JwtPermission;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.user.JwtUser;
import wang.bigbird.domain.framework.server.web.auth.support.processor.JwtSecurityProcessor;

import java.io.Serializable;
import java.util.List;

/**
 * 自定义权限注解验证
 *
 * @author Bigbird
 */
@Component
public class JwtPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private JwtSecurityProcessor jwtSecurityProcessor;

    /**
     * hasPermission鉴权方法
     * 这里仅仅判断PreAuthorize注解中的权限表达式
     * 实际中可以根据业务需求设计数据库通过targetUrl和permission做更复杂鉴权
     *
     * @param authentication 用户身份
     * @param targetUrl      请求路径，比如，对于用户权限认证，其值为：/analysis/sales
     * @param permission     请求路径权限，比如，对于用户权限认证，其值为：admin:analysis:sales
     * @return boolean 是否通过
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetUrl, Object permission) {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return true;
        }
        // 获取用户信息
        String authority = (String) authentication.getPrincipal();
        JwtAuthData jwtAuthData = JsonUtils.json2Object(authority, JwtAuthData.class);
        switch (jwtAuthData.getType()) {
            case JwtAuthData.USER:
                return hasUserPermission(authority, permission);
            case JwtAuthData.DEVICE:
            case JwtAuthData.CLIENT:
            default:
                return false;
        }
    }

    private boolean hasUserPermission(String authority, Object permission) {
        JwtUser jwtUser = JsonUtils.json2Object(authority, JwtUser.class);
        List<JwtPermission> permissions = jwtUser.getPermissions();
        if (CollectionUtils.isNotEmpty(permissions)) {
            // 权限对比
            for (JwtPermission jwtPermission : permissions) {
                if (jwtPermission.match(permission.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
