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
import wang.bigbird.domain.framework.message.sms.chinatelecom.base.util.OkHttpUtils;
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
    public IntegratedHttpClient integratedHttpClient() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(integratedBaseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .client(OkHttpUtils.getOkHttpClient())
                .build();
        return retrofit.create(IntegratedHttpClient.class);
    }

}
