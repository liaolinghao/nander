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
package wang.bigbird.domain.framework.server.web.captcha.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.BeanMapperUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.common.frequency.service.base.IFrequencyCheckerService;
import wang.bigbird.domain.framework.server.core.support.response.RespResult;
import wang.bigbird.domain.framework.server.web.captcha.base.enums.CaptchaTypeEnum;
import wang.bigbird.domain.framework.server.web.captcha.config.property.CaptchaProperties;
import wang.bigbird.domain.framework.server.web.captcha.domain.bo.out.SliderBO;
import wang.bigbird.domain.framework.server.web.captcha.domain.param.EmailCaptchaParam;
import wang.bigbird.domain.framework.server.web.captcha.domain.param.SliderCaptchaParam;
import wang.bigbird.domain.framework.server.web.captcha.domain.param.SmsCaptchaParam;
import wang.bigbird.domain.framework.server.web.captcha.domain.vo.SliderVO;
import wang.bigbird.domain.framework.server.web.captcha.service.base.ICaptchaService;
import wang.bigbird.domain.framework.server.web.captcha.service.base.IEmailService;
import wang.bigbird.domain.framework.server.web.captcha.service.base.ISmsService;
import wang.bigbird.domain.framework.server.web.core.base.constant.WebCoreConstants;
import wang.bigbird.domain.framework.server.web.core.support.annotation.Decrypt;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

/**
 * 验证码业务接口
 *
 * @author Bigbird
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaProperties captchaProperties;

    @Autowired
    private IFrequencyCheckerService frequencyCheckerService;
    @Autowired
    private ICaptchaService captchaService;
    @Autowired
    private IEmailService emailService;
    @Autowired(required = false)
    private ISmsService smsService;

    /**
     * 网站自定义图像验证码
     *
     * @param scene    验证码使用场景
     * @param request  请求对象
     * @param response 响应对象
     * @throws IOException 可能抛出该异常
     */
    @GetMapping(value = "/image/{scene}")
    public void image(@PathVariable(value = "scene") String scene, HttpServletRequest request,
                      HttpServletResponse response) throws IOException {
        String sessionId = request.getHeader(WebCoreConstants.SESSION_ID);
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getSession().getId();
        }
        response.setContentType("image/JPEG");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        ImageIO.write(captchaService.generateImageCaptcha(WebCoreConstants.CAPTCHA_IMAGE_KEY_PRE + sessionId + CommonConstants.COLON + scene), "JPEG", response.getOutputStream());
    }

    /**
     * 网站自定义滑块验证码
     *
     * @param scene   验证码使用场景
     * @param request 请求对象
     * @throws IOException
     */
    @GetMapping(value = "/slider/{scene}")
    public RespResult<SliderVO> slider(@PathVariable(value = "scene") String scene, HttpServletRequest request) throws IOException {
        String sessionId = request.getHeader(WebCoreConstants.SESSION_ID);
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getSession().getId();
        }
        SliderBO sliderBO = captchaService.generateSliderCaptcha(WebCoreConstants.CAPTCHA_SLIDER_KEY_PRE + sessionId + CommonConstants.COLON + scene);
        return RespResult.ok(BeanMapperUtils.map(sliderBO, SliderVO.class));
    }

    /**
     * 验证滑块验证码
     *
     * @param scene              验证码使用场景
     * @param sliderCaptchaParam 滑块验证码请求参数
     * @param request            请求对象
     */
    @PostMapping(value = "/slider/{scene}/check")
    public RespResult<String> checkSlider(@PathVariable(value = "scene") String scene, @Valid @RequestBody SliderCaptchaParam sliderCaptchaParam, HttpServletRequest request) {
        String sessionId = request.getHeader(WebCoreConstants.SESSION_ID);
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getSession().getId();
        }
        String code = captchaService.checkSlider(WebCoreConstants.CAPTCHA_SLIDER_KEY_PRE + sessionId + CommonConstants.COLON + scene, sliderCaptchaParam.getId(), sliderCaptchaParam.getX());
        return RespResult.ok(code);
    }

    /**
     * 请求短信验证码
     *
     * @param smsCaptchaParam 短信验证码请求参数
     * @param request         request对象
     */
    @PostMapping(value = "/sms")
    @Decrypt(all = false)
    public RespResult<Void> sms(@Valid @RequestBody SmsCaptchaParam smsCaptchaParam,
                                HttpServletRequest request) throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("SMS request data: {}.", smsCaptchaParam);
        }
        String sessionId = request.getHeader(WebCoreConstants.SESSION_ID);
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getSession().getId();
        }
        String templateId = captchaProperties.getSmsSceneTemplateIdMap().get(smsCaptchaParam.getScene());
        Assert.hasText(templateId, StringUtils.joinStr("无效的场景值：", smsCaptchaParam.getScene()));
        CaptchaTypeEnum cte = CaptchaTypeEnum.getInstanceByName(smsCaptchaParam.getCaptchaType());
        switch (cte) {
            case IMAGE:
                captchaService.verifyCaptcha(WebCoreConstants.CAPTCHA_IMAGE_KEY_PRE + sessionId + CommonConstants.COLON + smsCaptchaParam.getScene(), smsCaptchaParam.getCaptchaValue(), false, true);
                break;
            case SLIDER:
                captchaService.verifyCaptcha(WebCoreConstants.CAPTCHA_SLIDER_KEY_PRE + sessionId + CommonConstants.COLON + smsCaptchaParam.getScene(), smsCaptchaParam.getCaptchaValue(), false, true);
                break;
        }
        if (smsCaptchaParam.getIgnoreScene()) {
            // 不考虑场景执行短信发送频率限制检查
            frequencyCheckerService.frequencyChecker(smsCaptchaParam.getMobilephone(), CommonConstants.IGNORE, captchaProperties.getMaxDayCounts(), captchaProperties.getMaxHourCounts(), captchaProperties.getMaxMinuteCounts());
        } else {
            // 考虑场景执行短信发送频率限制检查
            frequencyCheckerService.frequencyChecker(smsCaptchaParam.getMobilephone(), smsCaptchaParam.getScene(), captchaProperties.getMaxDayCounts(), captchaProperties.getMaxHourCounts(), captchaProperties.getMaxMinuteCounts());
        }
        // 发送短信总量控制
        frequencyCheckerService.timeCheck(CommonConstants.ActionType.SMS, CommonConstants.IGNORE, captchaProperties.getSmsFrequencyMaxCount(), captchaProperties.getSmsFrequencyDurationTime());
        // 6位短信验证码
        String number = RandomStringUtils.randomNumeric(6);
        String cacheKey = WebCoreConstants.CAPTCHA_SMS_KEY_PRE + smsCaptchaParam.getMobilephone() + CommonConstants.COLON + smsCaptchaParam.getScene();
        captchaService.saveCaptchaValue(cacheKey, number);
        if (smsService != null) {
            smsService.sendSmsByTemplate(smsCaptchaParam.getMobilephone(), number, templateId);
        }
        return RespResult.ok();
    }

    /**
     * 请求邮箱验证码
     *
     * @param emailCaptchaParam 邮箱验证码请求参数
     * @param request           request对象
     */
    @PostMapping(value = "/email")
    @Decrypt(all = false)
    public RespResult<Void> email(@Valid @RequestBody EmailCaptchaParam emailCaptchaParam,
                                  HttpServletRequest request) throws InterruptedException {
        if (log.isDebugEnabled()) {
            log.debug("Email request data: {}.", emailCaptchaParam);
        }
        String sessionId = request.getHeader(WebCoreConstants.SESSION_ID);
        if (StringUtils.isBlank(sessionId)) {
            sessionId = request.getSession().getId();
        }
        String template = captchaProperties.getEmailSceneTemplateMap().get(emailCaptchaParam.getScene());
        Assert.hasText(template, StringUtils.joinStr("无效的场景值：", emailCaptchaParam.getScene()));
        CaptchaTypeEnum cte = CaptchaTypeEnum.getInstanceByName(emailCaptchaParam.getCaptchaType());
        switch (cte) {
            case IMAGE:
                captchaService.verifyCaptcha(WebCoreConstants.CAPTCHA_IMAGE_KEY_PRE + sessionId + CommonConstants.COLON + emailCaptchaParam.getScene(), emailCaptchaParam.getCaptchaValue(), false, true);
                break;
            case SLIDER:
                captchaService.verifyCaptcha(WebCoreConstants.CAPTCHA_SLIDER_KEY_PRE + sessionId + CommonConstants.COLON + emailCaptchaParam.getScene(), emailCaptchaParam.getCaptchaValue(), false, true);
                break;
        }
        if (emailCaptchaParam.getIgnoreScene()) {
            // 不考虑场景执行短信发送频率限制检查
            frequencyCheckerService.frequencyChecker(emailCaptchaParam.getEmail(), CommonConstants.IGNORE, captchaProperties.getMaxDayCounts(), captchaProperties.getMaxHourCounts(), captchaProperties.getMaxMinuteCounts());
        } else {
            // 考虑场景执行短信发送频率限制检查
            frequencyCheckerService.frequencyChecker(emailCaptchaParam.getEmail(), emailCaptchaParam.getScene(), captchaProperties.getMaxDayCounts(), captchaProperties.getMaxHourCounts(), captchaProperties.getMaxMinuteCounts());
        }
        // 发送短信总量控制
        frequencyCheckerService.timeCheck(CommonConstants.ActionType.EMAIL, CommonConstants.IGNORE, captchaProperties.getSmsFrequencyMaxCount(), captchaProperties.getSmsFrequencyDurationTime());
        // 6位邮箱验证码
        String number = RandomStringUtils.randomNumeric(6);
        String cacheKey = WebCoreConstants.CAPTCHA_EMAIL_KEY_PRE + emailCaptchaParam.getEmail() + CommonConstants.COLON + emailCaptchaParam.getScene();
        captchaService.saveCaptchaValue(cacheKey, number);
        emailService.send(emailCaptchaParam.getEmail(), number, template);
        return RespResult.ok();
    }


}
