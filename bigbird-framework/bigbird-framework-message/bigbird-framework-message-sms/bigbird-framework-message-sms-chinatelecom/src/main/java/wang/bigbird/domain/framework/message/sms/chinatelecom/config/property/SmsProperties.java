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
package wang.bigbird.domain.framework.message.sms.chinatelecom.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import wang.bigbird.domain.framework.message.sms.chinatelecom.base.enums.SmsTypeEnum;

/**
 * Redis properties
 *
 * @author Bigbird
 */
@Validated
@Data
@Configuration
@ConfigurationProperties(prefix = "bigbird.message.sms")
public class SmsProperties {

    /**
     * Sms 类型
     */
    private SmsTypeEnum type = SmsTypeEnum.OPEN;

    private final Open open = new Open();

    private final Integrated integrated = new Integrated();

    private final Yunyixin yunyixin = new Yunyixin();

    /**
     * 能力开放平台配置
     */
    @Data
    public static class Open {
        /**
         * 访问token
         */
        private String token;
        /**
         * 在短信平台注册的应用ID
         */
        private String appId;
        /**
         * 短信服务API域名地址
         */
        private String baseUrl = "http://api.189.cn";
    }

    /**
     * 一体化服务平台配置
     */
    @Data
    public static class Integrated {
        /**
         * 渠道在一体化消息服务平台申请的cpCode
         */
        private String cpCode;
        /**
         * 一体化消息服务平台分配的私钥
         */
        private String accessKey;
        /**
         * 短信服务API域名地址
         */
        private String baseUrl;
    }

    /**
     * 云翼信平台配置
     */
    @Data
    public static class Yunyixin {
        /**
         * 客户编号，SI的唯一标识
         */
        private String siid;
        /**
         * SI发送短信时使用的HTTP帐号
         */
        private String user;
        /**
         * 云翼信平台为SI分配的接口密钥
         */
        private String secret;
        /**
         * 短信服务API域名地址
         */
        private String baseUrl = "https://yyx.saas.189.cn:8070";
    }

}
