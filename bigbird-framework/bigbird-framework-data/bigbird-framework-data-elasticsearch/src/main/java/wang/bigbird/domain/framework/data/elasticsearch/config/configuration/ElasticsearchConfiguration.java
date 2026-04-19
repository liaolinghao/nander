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
package wang.bigbird.domain.framework.data.elasticsearch.config.configuration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import wang.bigbird.domain.framework.data.elasticsearch.base.helper.PropertiesHelper;
import wang.bigbird.domain.framework.data.elasticsearch.config.property.ElasticsearchProperties;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;


/**
 * @author Bigbird
 */
@Configuration
@Slf4j
@ComponentScan(basePackages = "wang.bigbird.domain.framework.data.elasticsearch")
public class ElasticsearchConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init elasticsearch client.");
    }

    /**
     * 高级别客户端
     */
    @Bean(destroyMethod = "close")
    public RestHighLevelClient restHighLevelClient(ElasticsearchProperties elasticsearchProperties, org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties springElasticsearchProperties) throws MalformedURLException {
        PropertiesHelper.combineElasticsearchProperties(elasticsearchProperties, springElasticsearchProperties);
        return buildRestHighLevelClient(elasticsearchProperties);
    }

    /**
     * elasticsearchTemplate
     * 如果保留该方法名作为Bean名称，需要开启Bean覆盖，否则会报错
     *
     * @SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
     * @SpringBootApplication(properties = "spring.main.allow-bean-definition-overriding=true")
     * 或者在配置文件中设置
     * spring.main.allow-bean-definition-overriding=true
     * 或yml配置文件
     * spring:
     * main:
     * allow-bean-definition-overriding: true
     */
    @Bean
    @Primary
    public ElasticsearchRestTemplate elasticsearchTemplate(RestHighLevelClient restHighLevelClient) {
        return new ElasticsearchRestTemplate(restHighLevelClient);
    }

    /**
     * 批量操作处理器
     */
    @Bean(destroyMethod = "close")
    public BulkProcessor bulkProcessor(RestHighLevelClient restHighLevelClient, ElasticsearchProperties elasticsearchProperties) {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                int numberOfActions = request.numberOfActions();
                if (log.isDebugEnabled()) {
                    log.debug("Executing bulk [{}] with {} requests.", executionId, numberOfActions);
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                if (response.hasFailures()) {
                    log.warn("Bulk [{}] executed with failures, fail msg: {}.", executionId,
                            response.buildFailureMessage());
                } else {
                    log.debug("Bulk [{}] completed in {} milliseconds.",
                            executionId, response.getTook().getMillis());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {
                log.error("Failed to execute bulk[{}].", executionId, failure);
            }
        };

        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) ->
                        restHighLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener);

        builder.setBulkActions(elasticsearchProperties.getBulkActions());
        builder.setBulkSize(new ByteSizeValue(elasticsearchProperties.getBulkSize(), ByteSizeUnit.MB));
        builder.setConcurrentRequests(elasticsearchProperties.getBulkConcurrentRequests());
        builder.setFlushInterval(TimeValue.timeValueSeconds(elasticsearchProperties.getBulkFlushInterval()));
        builder.setBackoffPolicy(BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(50L), 10));

        return builder.build();
    }

    /**
     * 客户端构造器
     */
    @SneakyThrows
    private RestClientBuilder restClientBuilder(ElasticsearchProperties elasticsearchProperties) {
        // 设置主机名及端口
        List<String> addresses = Arrays.asList(elasticsearchProperties.getAddresses().split(","));
        HttpHost[] hosts = new HttpHost[addresses.size()];
        for (int i = 0; i < addresses.size(); i++) {
            String scheme = elasticsearchProperties.getScheme();
            String hostname = addresses.get(i).split(":")[0];
            String port = addresses.get(i).split(":")[1];
            hosts[i] = new HttpHost(hostname, Integer.parseInt(port), scheme);
        }
        RestClientBuilder builder = RestClient.builder(hosts);
        // 设置用户名和密码
        String username = elasticsearchProperties.getUsername();
        String password = elasticsearchProperties.getPassword();
        CredentialsProvider credentialsProvider = null;
        if (StringUtils.isNotBlank(username)) {
            credentialsProvider = new BasicCredentialsProvider();
            Credentials credentials = new UsernamePasswordCredentials(
                    username, password);
            credentialsProvider.setCredentials(AuthScope.ANY, credentials);
        }
        // ssl
        Resource truststorePath = elasticsearchProperties.getTruststorePath();
        SSLContext sslContext = null;
        if (null != truststorePath) {
            String truststorePassword = elasticsearchProperties.getTruststorePassword();
            sslContext = SSLContexts
                    .custom()
                    .loadTrustMaterial(truststorePath.getURL(), truststorePassword.toCharArray(), new TrustSelfSignedStrategy())
                    .build();
        }
        // 超时设置
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder.setConnectTimeout(elasticsearchProperties.getConnectTimeout())
                        .setSocketTimeout(elasticsearchProperties.getSocketTimeout())
                        .setConnectionRequestTimeout(elasticsearchProperties.getConnectionRequestTimeout())
        );
        CredentialsProvider finalCredentialsProvider = credentialsProvider;
        SSLContext finalSslContext = sslContext;
        builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                .setDefaultCredentialsProvider(finalCredentialsProvider)
                .setSSLContext(finalSslContext)
                .setMaxConnPerRoute(elasticsearchProperties.getMaxConnectPerRoute())
                .setMaxConnTotal(elasticsearchProperties.getMaxConnectNum()));
        return builder;
    }

    private RestHighLevelClient buildRestHighLevelClient(ElasticsearchProperties elasticsearchProperties) {
        return new RestHighLevelClient(restClientBuilder(elasticsearchProperties));
    }

}

