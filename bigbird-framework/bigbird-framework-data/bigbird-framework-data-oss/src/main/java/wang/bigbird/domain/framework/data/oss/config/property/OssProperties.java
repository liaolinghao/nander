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
package wang.bigbird.domain.framework.data.oss.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;

/**
 * 对象存储统一配置
 *
 * @author Bigbird
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.data.oss")
public class OssProperties {

    /**
     * 加解密密钥
     */
    private String key = "bigbird";
    /**
     * 对象存储类型
     */
    private OssTypeEnum type;

    private final Minio minio = new Minio();

    private final Aliyun aliyun = new Aliyun();

    private final Qcloud qcloud = new Qcloud();

    private final Huawei huawei = new Huawei();

    private final Ct ct = new Ct();

    private final Ftp ftp = new Ftp();

    /**
     * Minio配置
     */
    @Data
    public static class Minio {

        private String url = "http://127.0.0.1:9005";

        private String accessKey;

        private String secretKey;

        private Boolean secure = false;

    }

    /**
     * 阿里云Oss配置
     */
    @Data
    public static class Aliyun {

        private String endpoint = "oss-cn-beijing.aliyuncs.com";

        private String accessKeyId;

        private String accessKeySecret;

    }

    /**
     * 腾讯云Oss配置
     */
    @Data
    public static class Qcloud {

        private Integer appId;

        private String secretId;

        private String secretKey;

        private String referer;

    }

    /**
     * 华为云Oss配置
     */
    @Data
    public static class Huawei {

        private String endpoint;

        private String ak;

        private String sk;

        private String protocol = "http";

    }

    /**
     * 天翼云Oss配置
     */
    @Data
    public static class Ct {

        private String endpoint = "oos-cn.ctyunapi.cn";

        private String accessId;

        private String accessKey;

        private String protocol = "http";

        /**
         * 连接的超时时间，单位毫秒
         */
        private Integer connectionTimeout = 30000;

        /**
         * socket超时时间，单位毫秒
         */
        private Integer socketTimeout = 30000;

    }

    /**
     * Ftp配置
     */
    @Data
    public static class Ftp {

        private String host;

        private Integer port = 22;

        private String username;

        private String password;

    }

}
