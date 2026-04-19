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
package wang.bigbird.domain.framework.server.web.captcha.base.enums;

import wang.bigbird.domain.framework.core.base.util.enums.ValuedEnum;

/**
 * 验证码类型
 *
 * @author Bigbird
 */
public enum CaptchaTypeEnum implements ValuedEnum<String> {

    IMAGE("图像验证码"), SLIDER("滑块验证码");

    private String type;

    CaptchaTypeEnum(String type) {
        this.type = type;
    }

    /**
     * 获取验证码类型枚举对象
     *
     * @param name 验证码类型名称
     * @return 验证码类型枚举对象
     */
    public static CaptchaTypeEnum getInstanceByName(String name) {
        for (CaptchaTypeEnum cte : CaptchaTypeEnum.values()) {
            if (cte.name().equalsIgnoreCase(name)) {
                return cte;
            }
        }
        return null;
    }

    /**
     * 获取验证码类型枚举对象
     *
     * @param type 验证码类型
     * @return 验证码类型枚举对象
     */
    public static CaptchaTypeEnum getInstanceByType(String type) {
        for (CaptchaTypeEnum cte : CaptchaTypeEnum.values()) {
            if (cte.value().equalsIgnoreCase(type)) {
                return cte;
            }
        }
        return null;
    }

    @Override
    public String value() {
        return type;
    }

}
