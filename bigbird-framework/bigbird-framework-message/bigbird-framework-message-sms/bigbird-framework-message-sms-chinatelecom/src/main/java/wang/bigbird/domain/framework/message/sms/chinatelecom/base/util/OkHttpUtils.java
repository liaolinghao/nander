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
package wang.bigbird.domain.framework.message.sms.chinatelecom.base.util;

import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;

/**
 * OkHttp通用工具类
 *
 * @author Bigbird
 */
public class OkHttpUtils {

    /**
     * 全局单例OkHttpClient，共用连接池
     */
    private static final OkHttpClient OK_HTTP_CLIENT;

    static {
        try {
            OkHttpClient.Builder okhttpClient = new OkHttpClient().newBuilder();
            //信任所有服务器地址
            okhttpClient.hostnameVerifier((s, sslSession) -> true);
            //创建管理器
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] x509Certificates,
                        String s) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] x509Certificates,
                        String s) {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            SSLSocketFactory ssfFactory = sc.getSocketFactory();
            okhttpClient.sslSocketFactory(ssfFactory, (X509TrustManager) trustAllCerts[0]);
            OK_HTTP_CLIENT = okhttpClient.build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize OkHttpClient", e);
        }
    }

    /**
     * 返回OkHttpClient
     */
    public static OkHttpClient getOkHttpClient() {
        return OK_HTTP_CLIENT;
    }

    /**
     * 返回Builder副本，底层复用同一个OkHttp连接池
     */
    public static OkHttpClient.Builder getBuilder() {
        return OK_HTTP_CLIENT.newBuilder();
    }

}
