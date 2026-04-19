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
package wang.bigbird.domain.framework.common.sensitive.base.enums;

import lombok.Getter;

/**
 * 脱敏类型
 *
 * @author Bigbird
 */
@Getter
public enum SensitiveTypeEnum {

    /*
    通用
     */
    FRONT_AND_BACK(1, "前3后3脱敏"),
    BACK5(2, "后5位脱敏"),
    BACK3(3, "后3位脱敏"),
    HASH(4, "哈希脱敏"),
    VAGUE(5, "模糊显示脱敏"),

    /*
    特定
     */
    NAME(101, "姓名"),
    MOBILE_PHONE_NUMBERS(102, "手机号"),
    TELEPHONE(103, "固定电话"),
    ID_CARD(104, "身份证"),
    BANK_CARD_NO(105, "银行卡号"),
    PASSWORD(106, "密码"),
    MAIL(107, "邮箱"),
    ADDRESS(108, "地址"),
    ACCOUNT(109, "账号");

    private final int type;
    private final String desc;

    SensitiveTypeEnum(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static SensitiveTypeEnum get(int type) {
        for (SensitiveTypeEnum item : SensitiveTypeEnum.values()) {
            if (item.getType() == type) {
                return item;
            }
        }
        return null;
    }
}
