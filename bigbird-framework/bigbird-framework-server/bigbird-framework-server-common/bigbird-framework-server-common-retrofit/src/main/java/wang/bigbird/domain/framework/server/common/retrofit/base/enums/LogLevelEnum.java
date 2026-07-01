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
package wang.bigbird.domain.framework.server.common.retrofit.base.enums;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 日志级别定义
 *
 * @author Bigbird
 */
public enum LogLevelEnum {

    /**
     * 完全关闭 OkHttp 网络日志，一行都不打印（生产环境首选）
     */
    NONE,
    /**
     * 只打印请求起止、URL、HTTP 状态码、耗时，不打印请求头、响应头、请求体、响应体（极简日志，线上临时排查首选）
     */
    BASIC,
    /**
     * 在 BASIC 基础上，完整打印全部请求头、响应头，依旧完全不打印 Request Body / Response Body（测试环境常驻、生产常态化使用）
     */
    HEADERS,
    /**
     * 完整打印：请求行 + 全部请求头 + 请求入参 Body + 响应头 + 完整响应返回体 Body（仅本地开发调试使用，严禁线上常开）
     */
    BODY;

    /**
     * 转换为 OkHttp 原生日志级别
     */
    public HttpLoggingInterceptor.Level toOkHttpLevel() {
        switch (this) {
            case NONE:
                return HttpLoggingInterceptor.Level.NONE;
            case BASIC:
                return HttpLoggingInterceptor.Level.BASIC;
            case BODY:
                return HttpLoggingInterceptor.Level.BODY;
            default:
                // 兜底关闭日志
                return HttpLoggingInterceptor.Level.HEADERS;
        }
    }

}
