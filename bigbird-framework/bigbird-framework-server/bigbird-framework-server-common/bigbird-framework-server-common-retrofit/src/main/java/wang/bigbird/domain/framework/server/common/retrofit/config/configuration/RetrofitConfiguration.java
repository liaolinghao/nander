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
package wang.bigbird.domain.framework.server.common.retrofit.config.configuration;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.server.common.retrofit.config.property.RetrofitProperties;

import javax.annotation.PostConstruct;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 网络框架配置
 *
 * @author Bigbird
 */
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.server.common.retrofit")
@Configuration
public class RetrofitConfiguration {

    @Autowired
    private RetrofitProperties retrofitProperties;

    @PostConstruct
    public void init() {
        log.info("Init retrofit framework.");
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
     * @return 支持https请求的客户端
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @Bean
    public OkHttpClient okHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        return createOkHttpClient(false);
    }

    /**
     * 用于支持sse请求
     * <p>
     * 对于sse接口请求需要采用以下方式构造Retrofit客户端
     * <p>
     * Retrofit retrofit = new Retrofit.Builder().baseUrl(authBaseUrl)
     * .addConverterFactory(JacksonConverterFactory.create())
     * .client(sseHttpClient)
     * .build();
     * return retrofit.create(XxxHttpClient.class);
     *
     * @return 支持sse请求的客户端
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    @Bean
    public OkHttpClient sseHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        return createOkHttpClient(true);
    }

    private OkHttpClient createOkHttpClient(boolean isStream) throws NoSuchAlgorithmException, KeyManagementException {
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
        // 打印请求过程详细信息
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        OkHttpClient.Builder okhttpClient = new OkHttpClient().newBuilder();
        okhttpClient.connectTimeout(retrofitProperties.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(retrofitProperties.getReadTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(retrofitProperties.getWriteTimeoutMs(), TimeUnit.MILLISECONDS)
                //信任所有服务器地址
                .hostnameVerifier((s, sslSession) -> true)
                // 信任所有证书
                .sslSocketFactory(ssfFactory, (X509TrustManager) trustAllCerts[0]);
        if (isStream) {
            // 不能开 BODY 级别日志，否则 OkHttp 会缓存整份响应全部接收完才抛流
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            okhttpClient.socketFactory(new SocketFactory() {

                        // 拿到默认的 SocketFactory，真正干活用它
                        private final SocketFactory delegate = SocketFactory.getDefault();

                        // 抽取公共方法：创建Socket并设置TcpNoDelay
                        private Socket createAndConfigureSocket(Socket socket) {
                            if (socket != null) {
                                try {
                                    socket.setTcpNoDelay(true);
                                } catch (Exception ignored) {
                                    // 配置失败不影响连接建立
                                }
                            }
                            return socket;
                        }

                        @Override
                        public Socket createSocket() throws IOException {
                            return createAndConfigureSocket(delegate.createSocket());
                        }

                        @Override
                        public Socket createSocket(String host, int port) throws IOException {
                            return createAndConfigureSocket(delegate.createSocket(host, port));
                        }

                        @Override
                        public Socket createSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
                            return createAndConfigureSocket(delegate.createSocket(host, port, localAddr, localPort));
                        }

                        @Override
                        public Socket createSocket(InetAddress addr, int port) throws IOException {
                            return createAndConfigureSocket(delegate.createSocket(addr, port));
                        }

                        @Override
                        public Socket createSocket(InetAddress addr, int port, InetAddress localAddr, int localPort) throws IOException {
                            return createAndConfigureSocket(delegate.createSocket(addr, port, localAddr, localPort));
                        }
                    })
                    // 禁用缓存
                    .cache(null)
                    .addInterceptor(httpLoggingInterceptor);
        } else {
            // 开了 BODY 级别日志，OkHttp 会缓存整份响应，全部接收完才抛流
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okhttpClient.addInterceptor(httpLoggingInterceptor);
        }
        return okhttpClient.build();
    }

}
