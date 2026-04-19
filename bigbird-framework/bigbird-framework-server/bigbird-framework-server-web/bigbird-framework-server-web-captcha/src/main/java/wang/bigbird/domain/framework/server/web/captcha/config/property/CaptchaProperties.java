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
package wang.bigbird.domain.framework.server.web.captcha.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 验证码配置
 *
 * @author Bigbird
 */
@Data
@Component
@ConfigurationProperties(prefix = "bigbird.server.web.captcha")
public class CaptchaProperties {

    /**
     * 是否异步发送
     */
    private Boolean async = false;

    /**
     * 是否启用后门
     */
    private Boolean backdoorEnable = false;

    /**
     * 后门验证码
     */
    private String backdoor = "bigbird";

    /**
     * 后门验证码每日可用次数
     */
    private Integer backdoorLimit = 10;

    /**
     * 最大重复验证次数
     */
    private Integer verifyMax = 3;

    /**
     * 验证时间周期，秒为单位，在该周期内验证失败次数超过最大次数，就让验证码失效
     */
    private Integer verifyTimePeriod = 30;

    /**
     * 验证码有效期，秒为单位
     */
    private Long ttl = 30L;

    /**
     * 验证码长度
     */
    private Integer length = 4;

    /**
     * 验证码图片宽度
     */
    private Integer width = 110;

    /**
     * 验证码图片高度
     */
    private Integer height = 40;

    /**
     * 同一个标识一天最大操作次数
     */
    private Integer maxDayCounts = 30;

    /**
     * 同一个标识一小时最大操作次数
     */
    private Integer maxHourCounts = 10;

    /**
     * 同一个标识一分钟最大操作次数
     */
    private Integer maxMinuteCounts = 1;

    /**
     * 短信频率控制：最大条数
     */
    private Integer smsFrequencyMaxCount = 10000;

    /**
     * 短信频率控制：时长（秒）
     */
    private Integer smsFrequencyDurationTime = 600;

    /**
     * 短信使用场景与模板ID对应关系
     */
    private Map<String, String> smsSceneTemplateIdMap;

    /**
     * 邮箱使用场景与模板消息对应关系
     */
    private Map<String, String> emailSceneTemplateMap;

    /**
     * 滑块上下文环境配置
     */
    private SliderContext slider;

    /**
     * 滑块上下文环境配置
     */
    @Data
    public static class SliderContext {

        /**
         * 滑块校验允许误差（默认是10）
         */
        private Integer deviation = 5;

        /**
         * 背景图路径（默认是jar中图片）
         */
        private String imageFileDir;

        /**
         * 背景图文件后缀（默认是png）
         */
        private String imageFormat = "png";

    }

}
