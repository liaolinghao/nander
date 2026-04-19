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
package wang.bigbird.domain.framework.message.sms.chinatelecom.config.configuration;

import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitScan;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.IntegratedHttpClient;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Retrofit 配置
 *
 * @author Bigbird
 */
@Configuration
@RetrofitScan("wang.bigbird.domain.framework.message.sms.chinatelecom")
@ComponentScan(basePackages = "wang.bigbird.domain.framework.message.sms.chinatelecom")
@Slf4j
public class ChinatelecomSmsConfiguration {

    @Value("${bigbird.message.sms.integrated.baseUrl:http://27.128.167.216:8097}")
    private String integratedBaseUrl;

    @PostConstruct
    public void init(){
        log.info("Init chinatelecom retrofit.");
    }

    @Bean
    public OkHttpClient chinatelecomHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
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
        return okhttpClient.build();
    }

    @Bean
    public IntegratedHttpClient integratedHttpClient(OkHttpClient chinatelecomHttpClient) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(integratedBaseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(chinatelecomHttpClient)
                .build();
        return retrofit.create(IntegratedHttpClient.class);
    }

}
