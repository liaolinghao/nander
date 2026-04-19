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
package wang.bigbird.domain.framework.server.web.retrofit.config.configuration;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * WEB框架配置
 *
 * @author Bigbird
 */
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.server.web.retrofit")
@Configuration
public class WebRetrofitConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init retrofit web framework.");
    }

    /**
     * 用于支持https请求
     * <p>
     * 普通的@RetrofitClient不支持https请求，
     * 对于https接口请求需要采用以下方式构造Retrofit客户端
     * <p>
     * Retrofit retrofit = new Retrofit.Builder().baseUrl(authBaseUrl)
     * .addConverterFactory(JacksonConverterFactory.create())
     * .client(okHttpClient)
     * .build();
     * return retrofit.create(XxxHttpClient.class);
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @Bean
    public OkHttpClient okHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
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
        // 打印请求过程详细信息
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return okhttpClient.addInterceptor(httpLoggingInterceptor).build();
    }

}
