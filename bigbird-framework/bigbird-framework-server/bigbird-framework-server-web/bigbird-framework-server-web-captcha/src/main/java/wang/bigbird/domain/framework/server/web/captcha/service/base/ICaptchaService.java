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
package wang.bigbird.domain.framework.server.web.captcha.service.base;

import wang.bigbird.domain.framework.server.core.exception.BusinessException;
import wang.bigbird.domain.framework.server.web.captcha.domain.bo.out.SliderBO;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 验证码服务
 *
 * @author Bigbird
 */
public interface ICaptchaService {

    /**
     * 后门场景
     */
    String BACKDOOR = "backdoor";
    /**
     * 验证码验证失败场景
     */
    String CAPTCHA_VERIFY_FAILED = "captcha:verify:failed";

    /**
     * 生成图像验证码
     *
     * @param captchaKey 验证码键
     * @return 图像验证码
     */
    BufferedImage generateImageCaptcha(String captchaKey);

    /**
     * 生成滑块验证码
     *
     * @param captchaKey 验证码键
     * @return 滑块验证码
     * @throws IOException
     */
    SliderBO generateSliderCaptcha(String captchaKey) throws IOException;

    /**
     * 验证滑块验证码
     *
     * @param captchaKey 验证码键
     * @param id         滑块验证码ID
     * @param x          水平坐标偏移值
     * @return 临时授权码
     */
    String checkSlider(String captchaKey, String id, Double x);

    /**
     * 验证验证码
     *
     * @param captchaKey   验证码键
     * @param captchaValue 验证码值
     * @param isLast       是否是最后一次验证，如果是，不管结果如何都要清空验证码
     * @param clean4ok     验证成功是否清除验证码，防止验证码被重复利用
     * @return 验证码是否正确
     * @throws BusinessException
     */
    boolean verifyCaptcha(String captchaKey, String captchaValue,
                          boolean isLast, boolean clean4ok) throws BusinessException;

    /**
     * 清除验证码
     *
     * @param captchaKey 验证码键
     */
    void cleanCaptcha(String captchaKey);

    /**
     * 保存验证码
     *
     * @param captchaKey   验证码键
     * @param captchaValue 验证码值
     */
    void saveCaptchaValue(String captchaKey, String captchaValue);

}
