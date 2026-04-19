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
package wang.bigbird.domain.framework.server.web.core.base.constant;

/**
 * 定义Web工程中要利用的一些常量
 *
 * @author Bigbird
 */
public class WebCoreConstants {

    /**
     * 设备ID
     */
    public static final String DEVICE_ID = "Device-Id";
    /**
     * 会话ID
     */
    public static final String SESSION_ID = "Session-Id";
    /**
     * 请求来源渠道
     */
    public static final String CHANNEL = "Channel";

    /**
     * 当前用户
     */
    public static final String CURRENT_USER = "currentUser";

    /**
     * 本机IP
     */
    public static final String LOCAL_HOST_IP = "127.0.0.1";

    /**
     * 重试次数
     */
    public static final int RETRY_NUM = 3;

    /**
     * 取消处理
     */
    public static final String CANCEL_PROGRESS = "cancelProgress";
    /**
     * 当前进度
     */
    public static final String CURRENT_PROGRESS = "currentProgress";
    /**
     * 总进度
     */
    public static final String TOTAL_PROGRESS = "totalProgress";

    /**
     * 开始
     */
    public static final String STARTED = "Started!";
    /**
     * 取消
     */
    public static final String CANCELED = "Canceled!";
    /**
     * 失败
     */
    public static final String FAILED = "Failed!";
    /**
     * 成功
     */
    public static final String SUCCESSED = "Successed!";

    /**
     * 图片验证码的缓存标识
     */
    public static final String CAPTCHA_IMAGE_KEY_PRE = "captcha:image:key:";

    /**
     * 短信验证码的缓存标识
     */
    public static final String CAPTCHA_SMS_KEY_PRE = "captcha:sms:key:";

    /**
     * 邮箱验证码的缓存标识
     */
    public static final String CAPTCHA_EMAIL_KEY_PRE = "captcha:email:key:";

    /**
     * 滑块验证码的缓存标识
     */
    public static final String CAPTCHA_SLIDER_KEY_PRE = "captcha:slider:key:";

}
