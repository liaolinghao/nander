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
package wang.bigbird.domain.framework.data.mongodb.base.helper;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.mongodb.config.property.MongoDbProperties;

/**
 * 属性设置器
 *
 * @author Bigbird
 */
public class PropertiesHelper {

    /**
     * 合并mongoDB配置
     * <p>
     * 配置加载优先级如下：
     * <p>
     * 1、自定义配置优先
     * <p>
     * 2、spring原生配置作为候补
     *
     * @param mongoDbProperties     自定义mongoDB属性
     * @param springMongoProperties spring原生的mongoDB配置
     */
    public static void combineMongoProperties(MongoDbProperties mongoDbProperties, MongoProperties springMongoProperties) {
        mongoDbProperties.setUri(loadUri(mongoDbProperties, springMongoProperties));
        mongoDbProperties.setDatabase(loadDatabase(mongoDbProperties, springMongoProperties));
        mongoDbProperties.setUsername(loadUsername(mongoDbProperties, springMongoProperties));
        mongoDbProperties.setPassword(loadPassword(mongoDbProperties, springMongoProperties));
    }

    private static char[] loadPassword(MongoDbProperties mongoDbProperties, MongoProperties springMongoProperties) {
        if (mongoDbProperties.getPassword() == null) {
            if (springMongoProperties.getPassword() != null) {
                return springMongoProperties.getPassword();
            }
        } else {
            return CryptUtils.decrypt(String.valueOf(mongoDbProperties.getPassword()), mongoDbProperties.getKey()).toCharArray();
        }
        return null;
    }

    private static String loadUsername(MongoDbProperties mongoDbProperties, MongoProperties springMongoProperties) {
        if (StringUtils.isBlank(mongoDbProperties.getUsername())) {
            if (StringUtils.isNotBlank(springMongoProperties.getUsername())) {
                return springMongoProperties.getUsername();
            }
        } else {
            return mongoDbProperties.getUsername();
        }
        return null;
    }

    private static String loadDatabase(MongoDbProperties mongoDbProperties, MongoProperties springMongoProperties) {
        if (StringUtils.isBlank(mongoDbProperties.getDatabase())) {
            if (StringUtils.isNotBlank(springMongoProperties.getDatabase())) {
                return springMongoProperties.getDatabase();
            }
        } else {
            return mongoDbProperties.getDatabase();
        }
        return null;
    }

    private static String loadUri(MongoDbProperties mongoDbProperties, MongoProperties springMongoProperties) {
        if (StringUtils.isBlank(mongoDbProperties.getUri())) {
            if (StringUtils.isNotBlank(springMongoProperties.getUri())) {
                return springMongoProperties.getUri();
            } else if (StringUtils.isNotBlank(springMongoProperties.getHost())) {
                if (springMongoProperties.getPort() != null) {
                    return StringUtils.joinStr("mongodb://", springMongoProperties.getHost(), ":", springMongoProperties.getPort());
                } else {
                    return StringUtils.joinStr("mongodb://", springMongoProperties.getHost(), ":27017");
                }
            }
        } else {
            return mongoDbProperties.getUri();
        }
        return "mongodb://127.0.0.1:27017";
    }

}
