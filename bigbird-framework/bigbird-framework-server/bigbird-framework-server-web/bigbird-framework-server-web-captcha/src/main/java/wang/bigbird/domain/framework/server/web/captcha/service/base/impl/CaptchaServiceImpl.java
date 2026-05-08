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
package wang.bigbird.domain.framework.server.web.captcha.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.CaptchaImageGenerator;
import wang.bigbird.domain.framework.core.base.util.BeanMapperUtils;
import wang.bigbird.domain.framework.core.base.util.DataUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisService;
import wang.bigbird.domain.framework.server.common.frequency.exception.ExceedMaxTimesOneDayException;
import wang.bigbird.domain.framework.server.common.frequency.service.base.IFrequencyCheckerService;
import wang.bigbird.domain.framework.server.core.exception.BusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.web.captcha.base.tool.SliderGenerator;
import wang.bigbird.domain.framework.server.web.captcha.config.property.CaptchaProperties;
import wang.bigbird.domain.framework.server.web.captcha.domain.bo.out.SliderBO;
import wang.bigbird.domain.framework.server.web.captcha.domain.pojo.Slider;
import wang.bigbird.domain.framework.server.web.captcha.exception.BackGroundImageIsEmptyException;
import wang.bigbird.domain.framework.server.web.captcha.exception.BackGroundImageNotFoundException;
import wang.bigbird.domain.framework.server.web.captcha.service.base.ICaptchaService;

import javax.annotation.PostConstruct;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus.IDENTIFY_CODE_ERROR;
import static wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus.IDENTIFY_CODE_EXPIRE;

/**
 * 验证码服务
 *
 * @author Bigbird
 */
@Slf4j
@Service("captchaService")
public class CaptchaServiceImpl implements ICaptchaService {

    /**
     * 底图缓存
     */
    private URL[] bgFilePaths;

    @Autowired
    private CaptchaProperties captchaProperties;
    @Autowired
    private IRedisService redisService;
    @Autowired
    private IFrequencyCheckerService frequencyCheckerService;

    @PostConstruct
    public void init() throws MalformedURLException {
        String format = captchaProperties.getSlider().getImageFormat();
        String imageFileDir = captchaProperties.getSlider().getImageFileDir();
        if (StringUtils.isNotBlank(imageFileDir)) {
            File dir = new File(imageFileDir);
            if (!dir.exists()) {
                throw new BackGroundImageNotFoundException();
            }
            String[] fileNames = dir.list((dir1, name) -> StringUtils.isBlank(format)
                    || name.endsWith(CommonConstants.DOT + format));
            if (fileNames == null || fileNames.length == 0) {
                throw new BackGroundImageIsEmptyException();
            }
            bgFilePaths = new URL[fileNames.length];
            for (int i = 0; i < fileNames.length; i++) {
                String fileName = fileNames[i];
                File file = new File(dir.getAbsolutePath() + File.separator + fileName);
                bgFilePaths[i] = file.toURI().toURL();
            }
        } else {
            List<URL> urlList = new ArrayList<>();
            String path = "bg/";
            int i = 1;
            URL url = getClass().getClassLoader().getResource(path + i++ + CommonConstants.DOT + format);
            while (url != null) {
                urlList.add(url);
                url = getClass().getClassLoader().getResource(path + i++ + CommonConstants.DOT + format);
            }
            bgFilePaths = urlList.toArray(new URL[0]);
        }
    }

    @Override
    public BufferedImage generateImageCaptcha(String captchaKey) {
        CaptchaImageGenerator captchaImageGenerator = new CaptchaImageGenerator();
        captchaImageGenerator.createCode(captchaProperties.getLength(),
                captchaProperties.getWidth(), captchaProperties.getHeight());
        String captchaValue = captchaImageGenerator.getCode();
        saveCaptchaValue(captchaKey, captchaValue);
        return captchaImageGenerator.getBuffImg();
    }

    @Override
    public SliderBO generateSliderCaptcha(String captchaKey) throws IOException {
        int index = DataUtils.getRandomData(0, bgFilePaths.length - 1);
        Slider slider = new SliderGenerator().generate(bgFilePaths[index], captchaProperties.getSlider().getImageFormat());
        String x = String.valueOf(slider.getX());
        saveCaptchaValue(captchaKey + CommonConstants.COLON + slider.getId(), x);
        return BeanMapperUtils.map(slider, SliderBO.class);
    }

    @Override
    public String checkSlider(String captchaKey, String id, Double x) {
        String sliderKey = captchaKey + CommonConstants.COLON + id;
        String cachedValue = redisService.get(sliderKey);
        if (StringUtils.isNotBlank(cachedValue)) {
            if (DataUtils.approximate(Double.parseDouble(cachedValue), x, captchaProperties.getSlider().getDeviation().doubleValue())) {
                String code = StringUtils.getUuid();
                saveCaptchaValue(captchaKey, code);
                cleanCaptcha(sliderKey);
                return code;
            }
            throw BusinessException.of(IDENTIFY_CODE_ERROR);
        }
        throw BusinessException.of(IDENTIFY_CODE_EXPIRE);
    }

    @Override
    public boolean verifyCaptcha(String captchaKey, String captchaValue, boolean isLast, boolean clean4ok) throws BusinessException {
        if (isBackdoorCaptcha(captchaValue)) {
            return true;
        }
        String cachedValue = redisService.get(captchaKey);
        log.info("CachedValue: {}, InputValue: {}.", cachedValue,
                captchaValue);
        if (StringUtils.isNotBlank(cachedValue)) {
            boolean ok = cachedValue.equalsIgnoreCase(captchaValue);
            if (isLast) {
                // 最后一次，不管结果都清除
                cleanCaptcha(captchaKey);
            } else {
                if (!ok) {
                    // 验证失败超过一定次数，把验证码失效
                    try {
                        frequencyCheckerService.timeCheck(captchaKey,
                                CAPTCHA_VERIFY_FAILED,
                                captchaProperties.getVerifyMax(),
                                captchaProperties.getVerifyTimePeriod());
                    } catch (ExceedMaxTimesOneDayException e) {
                        cleanCaptcha(captchaKey);
                    }
                } else {
                    // 验证成功，把验证码清除，防止验证码被重复利用
                    if (clean4ok) {
                        cleanCaptcha(captchaKey);
                    }
                }
            }
            if (!ok) {
                throw BusinessException.of(IDENTIFY_CODE_ERROR);
            }
            return true;
        }
        throw BusinessException.of(IDENTIFY_CODE_EXPIRE);
    }

    @Override
    public void cleanCaptcha(String captchaKey) {
        redisService.del(captchaKey);
    }

    @Override
    public void saveCaptchaValue(String captchaKey, String captchaValue) {
        redisService.set(captchaKey, captchaValue, captchaProperties.getTtl(), TimeUnit.SECONDS);
    }

    /**
     * 是否是预留的验证码后门
     *
     * @param value
     * @return
     */
    private boolean isBackdoorCaptcha(String value) {
        if (captchaProperties.getBackdoorEnable()) {
            log.info("BackdoorCaptcha: {}.", captchaProperties.getBackdoor());
            if (value.equalsIgnoreCase(captchaProperties.getBackdoor())) {
                // 属于后门验证码，只要不超过每天最大次数就放行
                try {
                    frequencyCheckerService.dayCheck(captchaProperties.getBackdoor(),
                            BACKDOOR,
                            captchaProperties.getBackdoorLimit());
                } catch (ExceedMaxTimesOneDayException e) {
                    throw BusinessException.of(IBaseResponseStatus.EXCEED_MAX_TIMES_ONE_DAY);
                }
                return true;
            }
        }
        return false;
    }
}
