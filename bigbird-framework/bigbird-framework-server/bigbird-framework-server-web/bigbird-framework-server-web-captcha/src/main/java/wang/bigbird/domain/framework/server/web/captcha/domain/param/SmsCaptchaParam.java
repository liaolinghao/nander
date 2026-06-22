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
package wang.bigbird.domain.framework.server.web.captcha.domain.param;

import lombok.Data;
import wang.bigbird.domain.framework.server.web.core.support.annotation.DecryptField;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 短信验证码请求信息
 *
 * @author Bigbird
 */
@Data
public class SmsCaptchaParam implements Serializable {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "((13[0-9])|(14[^23])|(15[^4])|(16[2567])|(17[0-9])|(18[0-9])|(19[^4]))\\d{8}", message = "无效的手机号")
    @DecryptField
    private String mobilephone;

    @NotBlank(message = "验证码类型不能为空")
    @Pattern(regexp = "^(IMAGE|SLIDER)$", message = "无效的验证码类型")
    private String captchaType;

    @NotBlank(message = "验证码值不能为空")
    private String captchaValue;

    @NotBlank(message = "验证码用途不能为空")
    private String scene;

    /**
     * 频率限制是否忽略场景
     */
    private Boolean ignoreScene = true;

}
