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
package wang.bigbird.domain.framework.common.sensitive.base.util;

import org.apache.commons.lang3.StringUtils;
import wang.bigbird.domain.framework.common.sensitive.base.enums.SensitiveTypeEnum;

import java.util.List;
import java.util.Objects;

import static wang.bigbird.domain.framework.common.sensitive.base.constant.SensitiveConstant.*;

/**
 * 脱敏工具类
 *
 * @author Bigbird
 */
public class SensitiveUtils {

    private static final int ONE = 1;

    private static final int TWO = 2;

    private static final int THREE = 3;

    private static final int FOUR = 4;

    private static final int FIFTEEN = 15;

    private static final int EIGHTEEN = 18;

    private SensitiveUtils() {
        throw new IllegalStateException();
    }

    /**
     * 脱敏
     *
     * @param sensitiveType 脱敏类型
     * @param content       内容
     * @return 脱敏后内容
     */
    public static String sensitive(SensitiveTypeEnum sensitiveType, Object content) {
        String value = "";
        if (content != null) {
            value = String.valueOf(content);
        }
        switch (Objects.requireNonNull(sensitiveType)) {
            case FRONT_AND_BACK:
                return frontAndBack(value);
            case BACK5:
                return backN(value, 5);
            case BACK3:
                return backN(value, 3);
            case HASH:
                return hash(value);
            case VAGUE:
                return vague(value);
            case NAME:
                return name(value);
            case ACCOUNT:
                return account(value);
            case MOBILE_PHONE_NUMBERS:
                return mobile(value);
            case TELEPHONE:
                return telephone(value);
            case ID_CARD:
                return idCard(value);
            case BANK_CARD_NO:
                return bankCardNo(value);
            case PASSWORD:
                return password(value);
            case MAIL:
                return mail(value);
            case ADDRESS:
                return address(value);
            default:
                return value;
        }
    }

    /**
     * 前3后3脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String frontAndBack(String value) {
        if (value == null || value.length() < FRONT_AND_BACK_FRONT_LEN + FRONT_AND_BACK_BACK_LEN) {
            return DEFAULT_SYMBOL;
        }
        String tmp = StringUtils.leftPad(value.substring(
                        FRONT_AND_BACK_FRONT_LEN),
                value.length(),
                DESENSITIZATION_STR);
        return StringUtils.rightPad(
                tmp.substring(0, tmp.length() - FRONT_AND_BACK_BACK_LEN),
                tmp.length(),
                DESENSITIZATION_STR);
    }

    /**
     * 后N位脱敏
     *
     * @param value 文本内容
     * @param len   指定位数
     * @return 脱敏后内容
     */
    public static String backN(String value, int len) {
        if (value != null && value.length() >= len) {
            return StringUtils.rightPad(value.substring(0,
                            value.length() - len),
                    value.length(),
                    DESENSITIZATION_STR);
        } else {
            return StringUtils.leftPad("",
                    len,
                    DESENSITIZATION_STR);
        }
    }

    /**
     * hash 脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String hash(String value) {
        if (value == null) {
            value = DEFAULT_SYMBOL;
        }
        return String.valueOf(value.hashCode());
    }

    /**
     * 模糊显示脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String vague(String value) {
        if (value == null) {
            return DEFAULT_SYMBOL;
        }
        return StringUtils.leftPad("",
                value.length(),
                DESENSITIZATION_STR);
    }

    /**
     * 姓名脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String name(String value) {
        if (value == null) {
            return DEFAULT_SYMBOL;
        }
        String l = StringUtils.left(value, 1);
        String r = StringUtils.right(value, 1);
        if (value.length() > TWO) {
            return StringUtils.rightPad(l, StringUtils.length(value) - 1, DESENSITIZATION_STR) + r;
        } else if (value.length() > ONE) {
            return StringUtils.rightPad(l, StringUtils.length(value), DESENSITIZATION_STR);
        }
        return DESENSITIZATION_STR;
    }

    /**
     * 账号脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    private static String account(String value) {
        if (value == null) {
            return DEFAULT_SYMBOL;
        }
        if (value.length() > FOUR) {
            String r = StringUtils.right(value, FOUR);
            return StringUtils.leftPad(r, StringUtils.length(value), DESENSITIZATION_STR);
        } else if (value.length() > THREE) {
            String r = StringUtils.right(value, THREE);
            return StringUtils.leftPad(r, StringUtils.length(value), DESENSITIZATION_STR);
        } else if (value.length() > TWO) {
            String r = StringUtils.right(value, TWO);
            return StringUtils.leftPad(r, StringUtils.length(value), DESENSITIZATION_STR);
        } else if (value.length() > ONE) {
            String r = StringUtils.right(value, ONE);
            return StringUtils.leftPad(r, StringUtils.length(value), DESENSITIZATION_STR);
        }
        return DESENSITIZATION_STR;
    }

    /**
     * 手机脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String mobile(String value) {
        if (value == null) {
            return DEFAULT_SYMBOL;
        }
        List<String> phones = wang.bigbird.domain.framework.core.base.util.StringUtils.pickUpMobilePhone(value);
        for (String phone : phones) {
            value = value.replace(phone, phone.replaceAll("(\\w{3})\\w*(\\w{4})", "$1****$2"));
        }
        return value;
    }

    /**
     * 固话脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String telephone(String value) {
        if (value == null) {
            return DEFAULT_SYMBOL;
        }
        List<String> phones = wang.bigbird.domain.framework.core.base.util.StringUtils.pickUpTelePhone(value);
        for (String phone : phones) {
            value = value.replace(phone, StringUtils.rightPad(phone.substring(0, 5), phone.length(), DESENSITIZATION_STR));
        }
        return value;
    }

    /**
     * 身份证脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String idCard(String value) {
        if (StringUtils.isNotEmpty(value)) {
            if (value.length() == FIFTEEN) {
                return value.replaceAll("(\\w{6})\\w*(\\w{3})", "$1******$2");
            }
            if (value.length() == EIGHTEEN) {
                return value.replaceAll("(\\w{6})\\w*(\\w{3})", "$1*********$2");
            }
        }
        return DEFAULT_SYMBOL;
    }

    /**
     * 银行卡号脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String bankCardNo(String value) {
        if (StringUtils.isNotEmpty(value)) {
            return value.substring(0, 4) + StringUtils.leftPad("", value.length() - 8, DESENSITIZATION_STR) + value.substring(value.length() - 4);
        }
        return DEFAULT_SYMBOL;
    }

    /**
     * 密码脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String password(String value) {
        if (StringUtils.isNotEmpty(value)) {
            return StringUtils.leftPad("", value.length(), DESENSITIZATION_STR);
        }
        return DEFAULT_SYMBOL;
    }

    /**
     * 邮箱脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String mail(String value) {
        if (StringUtils.isNotEmpty(value)) {
            List<String> emails = wang.bigbird.domain.framework.core.base.util.StringUtils.pickUpEmail(value);
            for (String email : emails) {
                value = value.replace(email, email.replaceAll("(^\\w)[^@]*(@.*$)", "$1****$2"));
            }
            return value;
        }
        return DEFAULT_SYMBOL;
    }

    /**
     * 地址脱敏
     *
     * @param value 文本内容
     * @return 脱敏后内容
     */
    public static String address(String value) {
        if (StringUtils.isNotEmpty(value)) {
            int i = value.length() / 2;
            return StringUtils.rightPad(value.substring(0, i), value.length(), DESENSITIZATION_STR);
        }
        return DEFAULT_SYMBOL;
    }
}
