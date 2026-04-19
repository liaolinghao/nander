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
package wang.bigbird.domain.framework.data.elasticsearch.base.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.data.elasticsearch.config.property.ElasticsearchProperties;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 属性设置器
 *
 * @author Bigbird
 */
public class PropertiesHelper {

    /**
     * 合并elasticsearch配置
     * <p>
     * 配置加载优先级如下：
     * <p>
     * 1、自定义配置优先
     * <p>
     * 2、spring原生配置作为候补
     *
     * @param elasticsearchProperties       自定义elasticsearch配置
     * @param springElasticsearchProperties spring原生的elasticsearch配置
     */
    public static void combineElasticsearchProperties(ElasticsearchProperties elasticsearchProperties, ElasticsearchRestClientProperties springElasticsearchProperties) throws MalformedURLException {
        elasticsearchProperties.setScheme(loadScheme(elasticsearchProperties, springElasticsearchProperties));
        elasticsearchProperties.setAddresses(loadAddresses(elasticsearchProperties, springElasticsearchProperties));
        elasticsearchProperties.setUsername(loadUsername(elasticsearchProperties, springElasticsearchProperties));
        elasticsearchProperties.setPassword(loadPassword(elasticsearchProperties, springElasticsearchProperties));
        elasticsearchProperties.setConnectTimeout(loadConnectTimeout(elasticsearchProperties, springElasticsearchProperties));
    }

    private static Integer loadConnectTimeout(ElasticsearchProperties elasticsearchProperties, ElasticsearchRestClientProperties springElasticsearchProperties) {
        if (elasticsearchProperties.getConnectTimeout() == null) {
            if (springElasticsearchProperties.getConnectionTimeout() != null) {
                return Long.valueOf(springElasticsearchProperties.getConnectionTimeout().toMillis()).intValue();
            }
        } else {
            return elasticsearchProperties.getConnectTimeout();
        }
        return 60000;
    }

    private static String loadPassword(ElasticsearchProperties elasticsearchProperties, ElasticsearchRestClientProperties springElasticsearchProperties) {
        if (StringUtils.isBlank(elasticsearchProperties.getPassword())) {
            if (StringUtils.isNotBlank(springElasticsearchProperties.getPassword())) {
                return springElasticsearchProperties.getPassword();
            }
        } else {
            return CryptUtils.decrypt(elasticsearchProperties.getPassword(), elasticsearchProperties.getKey());
        }
        return null;
    }

    private static String loadUsername(ElasticsearchProperties elasticsearchProperties, ElasticsearchRestClientProperties springElasticsearchProperties) {
        if (StringUtils.isBlank(elasticsearchProperties.getUsername())) {
            if (StringUtils.isNotBlank(springElasticsearchProperties.getUsername())) {
                return springElasticsearchProperties.getUsername();
            }
        } else {
            return elasticsearchProperties.getUsername();
        }
        return null;
    }

    private static String loadAddresses(ElasticsearchProperties elasticsearchProperties, ElasticsearchRestClientProperties springElasticsearchProperties) throws MalformedURLException {
        if (StringUtils.isBlank(elasticsearchProperties.getAddresses())) {
            if (CollectionUtils.isNotEmpty(springElasticsearchProperties.getUris())) {
                StringBuilder sb = new StringBuilder();
                for (String uri : springElasticsearchProperties.getUris()) {
                    URL url = new URL(uri);
                    sb.append(",").append(url.getHost()).append(":").append(url.getPort());
                }
                return sb.substring(1);
            }
        } else {
            return elasticsearchProperties.getAddresses();
        }
        return "127.0.0.1:9200";
    }

    private static String loadScheme(ElasticsearchProperties elasticsearchProperties, ElasticsearchRestClientProperties springElasticsearchProperties) throws MalformedURLException {
        if (StringUtils.isBlank(elasticsearchProperties.getScheme())) {
            if (CollectionUtils.isNotEmpty(springElasticsearchProperties.getUris())) {
                for (String uri : springElasticsearchProperties.getUris()) {
                    URL url = new URL(uri);
                    return url.getProtocol();
                }
            }
        } else {
            return elasticsearchProperties.getScheme();
        }
        return "http";
    }

}
