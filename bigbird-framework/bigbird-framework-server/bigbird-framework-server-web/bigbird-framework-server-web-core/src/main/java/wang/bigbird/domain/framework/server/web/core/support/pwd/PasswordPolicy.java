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
package wang.bigbird.domain.framework.server.web.core.support.pwd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 密码验证策略
 *
 * @author Bigbird
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordPolicy implements Serializable {

    /**
     * 最小长度
     */
    private int minLength;
    /**
     * 最大长度
     */
    private int maxLength;
    /**
     * 所需字符类别数
     */
    private int requiredCategories;
    /**
     * 是否检查键盘序列
     */
    private boolean checkKeyboardSequence;
    /**
     * 是否检查连续字符
     */
    private boolean checkCharSequence;
    /**
     * 是否检查相同字符
     */
    private boolean checkSameChar;
    /**
     * 是否检查账号相似性
     */
    private boolean checkAccountSimilarity;
    /**
     * 是否检查历史密码
     */
    private boolean checkHistory;
    /**
     * 是否检查弱密码
     */
    private boolean checkWeakPassword;
    /**
     * 历史密码记录数
     */
    private int historySize;

    /**
     * 默认密码校验规则：
     * <p>
     * 密码长度应至少8位；
     * 至少包含数字、小写字母、大写字母、特殊符号4类；
     * 应避免键盘排序输入；
     * 不能与账号一致；
     * 不能与近三次密码一致；
     * 弱密码库校验。
     *
     * @return 默认密码校验规则
     */
    public static PasswordPolicy defaultPolicy() {
        return PasswordPolicy.builder()
                .minLength(8).maxLength(32)
                .requiredCategories(3)
                .checkKeyboardSequence(true)
                .checkAccountSimilarity(true)
                .checkHistory(true)
                .checkWeakPassword(true)
                .historySize(3)
                .build();
    }

}
