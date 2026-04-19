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
package wang.bigbird.domain.framework.server.web.ws.base.constant;

/**
 * 定义WS要利用的一些常量
 *
 * @author Bigbird
 */
public class WsConstants {

    /**
     * 自定义请求头，浏览器不支持
     */
    public static final String APPKEY_HEADER_CODE = "AppKey";
    /**
     * 自定义请求头，浏览器不支持
     */
    public static final String TOKEN_HEADER_CODE = "Token";
    /**
     * 请求参数，用于浏览器传递appKey
     */
    public static final String APPKEY_PARAM_CODE = "appKey";
    /**
     * 请求参数，用于浏览器传递token
     */
    public static final String TOKEN_PARAM_CODE = "token";
    /**
     * 请求路径
     */
    public static final String PATH_PARAM_CODE = "requestPath";

}
