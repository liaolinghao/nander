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
package wang.bigbird.domain.framework.server.web.defence.support.security;

import lombok.Data;

import java.util.Map;

/**
 * 访问数据封装类
 *
 * @author Bigbird
 */
@Data
public class AccessData {

    public static final String SIGNATURE_PARAM_CODE = "signature";
    public static final String APPKEY_PARAM_CODE = "appKey";
    // 在HTTP协议中，请求头（Headers）的名称是大小写不敏感的。也就是说，无论请求头是以大写、小写还是大小写混合的形式发送，服务器都会将其视为相同的请求头。
    // 但是，在实践中，为了保证HTTP请求的清晰和一致性，大多数HTTP客户端（如浏览器、Postman等）和服务器在处理HTTP请求时，会将请求头自动转换为小写。
    public static final String APPKEY_HEADER_CODE = "appkey";
    public static final String ACCESSTOKEN_PARAM_CODE = "authorization";
    public static final String NONCE_PARAM_CODE = "nonce";

    /**
     * 客户端调用地址
     */
    private String remoteAddr;
    /**
     * 请求接口具体地址
     */
    private String requestUri;
    /**
     * 请求接口模式串，配合requestAction用于定位对外暴露服务API信息
     */
    private String requestApi;
    /**
     * 请求动作，配合requestApi用于定位对外暴露服务API信息
     */
    private String requestAction;
    /**
     * 请求体
     */
    private String requestBody;
    /**
     * 请求参数
     */
    private Map<String, String> requestParam;
    /**
     * 请求头
     */
    private Map<String, String> requestHeader;

}
