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
package wang.bigbird.domain.framework.server.web.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.server.core.exception.BusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.user.JwtOrg;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.user.JwtRole;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.user.JwtUser;
import wang.bigbird.domain.framework.server.web.auth.support.processor.JwtSecurityProcessor;
import wang.bigbird.domain.framework.server.web.core.base.enums.ChannelEnum;
import wang.bigbird.domain.framework.server.web.core.base.enums.DeviceScreenTypeEnum;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 存放控制器的公共逻辑
 *
 * @author Bigbird
 */
public class BaseController {

    @Autowired
    private JwtSecurityProcessor jwtSecurityProcessor;

    /**
     * 获取当前登录用户ID
     *
     * @return 登录用户ID
     */
    protected Long loadLoginUserId() {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw BusinessException.of(IBaseResponseStatus.USER_NOT_LOGIN);
        }
        String credentials = (String) authentication.getCredentials();
        return Long.valueOf(credentials.split(CommonConstants.SEPARATOR)[1]);
    }

    /**
     * 获取当前登录用户所属租户ID
     *
     * @return 登录用户所属租户ID
     */
    protected Long loadTenantId() {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw BusinessException.of(IBaseResponseStatus.USER_NOT_LOGIN);
        }
        String credentials = (String) authentication.getCredentials();
        return Long.valueOf(credentials.split(CommonConstants.SEPARATOR)[2]);
    }

    /**
     * 获取当前登录Access Token ID
     *
     * @return 当前登录Access Token ID
     */
    protected String loadTokenId() {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw BusinessException.of(IBaseResponseStatus.USER_NOT_LOGIN);
        }
        String credentials = (String) authentication.getCredentials();
        return credentials.split(CommonConstants.SEPARATOR)[0];
    }

    /**
     * 获取当前登录用户角色
     *
     * @return
     */
    protected List<JwtRole> loadLoginUserRole() {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw BusinessException.of(IBaseResponseStatus.USER_NOT_LOGIN);
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<JwtRole> roles = authorities.stream().filter(authority -> authority instanceof JwtRole)
                .map(authority -> (JwtRole) authority).collect(Collectors.toList());
        return roles;
    }

    /**
     * 获取当前登录用户组织
     *
     * @return
     */
    protected List<JwtOrg> loadLoginUserOrg() {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw BusinessException.of(IBaseResponseStatus.USER_NOT_LOGIN);
        }
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            String authObject = (String) authentication.getPrincipal();
            JwtUser jwtUser = JsonUtils.json2Object(authObject, JwtUser.class);
            return jwtUser.getOrgs();
        }
        throw BusinessException.of(IBaseResponseStatus.FORBIDDEN_TO_OPERATION);
    }

    /**
     * 获取登录用户基本信息
     *
     * @return
     */
    protected Map<String, Object> loadLoginUserBasicInfo() {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw BusinessException.of(IBaseResponseStatus.USER_NOT_LOGIN);
        }
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            String authObject = (String) authentication.getPrincipal();
            JwtUser jwtUser = JsonUtils.json2Object(authObject, JwtUser.class);
            return jwtUser.getInfos();
        }
        throw BusinessException.of(IBaseResponseStatus.FORBIDDEN_TO_OPERATION);
    }

    /**
     * 获取登录用户渠道
     *
     * @return
     */
    protected ChannelEnum loadLoginUserChannel() {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw BusinessException.of(IBaseResponseStatus.USER_NOT_LOGIN);
        }
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            String authObject = (String) authentication.getPrincipal();
            JwtUser jwtUser = JsonUtils.json2Object(authObject, JwtUser.class);
            return jwtUser.getChannel();
        }
        throw BusinessException.of(IBaseResponseStatus.FORBIDDEN_TO_OPERATION);
    }

    /**
     * 获取登录用户设备屏幕类型
     *
     * @return
     */
    protected DeviceScreenTypeEnum loadLoginUserDeviceScreenType() {
        if (!jwtSecurityProcessor.isEnableJwtSecurity()) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw BusinessException.of(IBaseResponseStatus.USER_NOT_LOGIN);
        }
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            String authObject = (String) authentication.getPrincipal();
            JwtUser jwtUser = JsonUtils.json2Object(authObject, JwtUser.class);
            return jwtUser.getDeviceScreenType();
        }
        throw BusinessException.of(IBaseResponseStatus.FORBIDDEN_TO_OPERATION);
    }

    /**
     * 从指定应用注销
     *
     * @param appKey 应用键
     */
    protected void logout(String appKey) {
        if (jwtSecurityProcessor.isEnableJwtSecurity()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                String authObject = (String) authentication.getPrincipal();
                JwtUser jwtUser = JsonUtils.json2Object(authObject, JwtUser.class);
                String credentials = (String) authentication.getCredentials();
                String accessTokenId = credentials.split(CommonConstants.SEPARATOR)[0];
                jwtSecurityProcessor.logout(appKey, jwtUser.getChannel(), jwtUser.getType(), jwtUser.getId(), accessTokenId);
            }
        }
        SecurityContextHolder.clearContext();
    }

}
