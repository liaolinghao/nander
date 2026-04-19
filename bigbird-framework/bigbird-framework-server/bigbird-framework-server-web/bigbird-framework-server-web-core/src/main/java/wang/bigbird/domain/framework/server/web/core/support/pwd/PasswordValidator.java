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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.core.base.util.WebUtils;
import wang.bigbird.domain.framework.server.web.core.service.base.IPwdValidateService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 强密码规则校验器
 * <p>
 * 默认策略 defaultPolicy 如下：
 * 密码长度应至少8位；
 * 至少包含数字,小写字母,大写字母,特殊符号4类；
 * 应避免键盘排序输入；
 * 不能与账号一致；
 * 不能与近三次密码一致；
 * 弱密码库校验。
 * 也可自定义密码策略执行校验
 *
 * @author Bigbird
 */
@Slf4j
@Component("passwordValidator")
public class PasswordValidator implements InitializingBean {

    @Data
    @AllArgsConstructor
    public static class Result {
        private boolean valid;
        private List<String> messages;
    }

    /**
     * 要检查的键盘序列长度
     */
    private static final int KEY_SEQUENCE_LENGTH = 3;

    /**
     * 要检查的字符序列长度
     */
    private static final int CHAR_SEQUENCE_LENGTH = 3;

    /**
     * 要检查的相同字符长度
     */
    private static final int SAME_CHAR_LENGTH = 3;

    /**
     * 弱密码库（实际应从文件/DB加载）
     */
    private Set<String> weakPasswords;

    /**
     * 弱密码服务
     */
    @Autowired(required = false)
    private IPwdValidateService pwdValidateService;

    @Override
    public void afterPropertiesSet() throws Exception {
        weakPasswords = loadWeakPasswords();
    }

    /**
     * 验证密码是否符合安全校验规范
     *
     * @param ownerId  密码拥有者标识
     * @param password 密码
     * @param account  账户名
     * @param policy   校验策略
     * @return 校验结果
     */
    public Result validate(String ownerId, String password, String account, PasswordPolicy policy) {
        List<String> errors = new ArrayList<>();
        // 基础校验
        checkLength(password, policy.getMinLength(), policy.getMaxLength(), errors);
        checkCharacterCategories(password, policy.getRequiredCategories(), errors);
        // 安全规则校验
        if (policy.isCheckKeyboardSequence()) {
            checkKeyboardSequences(password, errors);
        }
        if (policy.isCheckCharSequence()) {
            checkCharSequences(password, errors);
        }
        if (policy.isCheckSameChar()) {
            checkSameChar(password, errors);
        }
        if (policy.isCheckAccountSimilarity()) {
            checkAccountSimilarity(password, account, errors);
        }
        if (policy.isCheckHistory()) {
            checkPasswordHistory(ownerId, password, errors);
        }
        if (policy.isCheckWeakPassword()) {
            checkWeakPassword(password, errors);
        }
        return new Result(errors.isEmpty(), errors);
    }

    private void checkLength(String password, int minLength, int maxLength, List<String> errors) {
        if (StringUtils.isBlank(password) || password.length() < minLength) {
            errors.add("密码长度需至少" + minLength + "位。");
        }
        if (password.length() > maxLength) {
            errors.add("密码长度不能超过" + maxLength + "位。");
        }
    }

    private void checkCharacterCategories(String password, int required, List<String> errors) {
        int categories = 0;
        if (StringUtils.containsDigit(password)) {
            categories++;
        }
        if (StringUtils.containsLower(password)) {
            categories++;
        }
        if (StringUtils.containsUpper(password)) {
            categories++;
        }
        if (StringUtils.containsSpecial(password)) {
            categories++;
        }
        if (categories < required) {
            errors.add("密码需包含数字,小写字母,大写字母,特殊符号中至少" + required + "类。");
        }
    }

    private void checkKeyboardSequences(String password, List<String> errors) {
        String sub = StringUtils.parseKeyboardSequences(password, KEY_SEQUENCE_LENGTH);
        if (StringUtils.isNotBlank(sub)) {
            errors.add("密码包含不安全的键盘序列：" + sub);
        }
    }

    private void checkCharSequences(String password, List<String> errors) {
        String sub = StringUtils.parseCharSequences(password, CHAR_SEQUENCE_LENGTH, true);
        if (StringUtils.isNotBlank(sub)) {
            errors.add("密码包含不安全的字母序列：" + sub);
        }
        sub = StringUtils.parseCharSequences(password, CHAR_SEQUENCE_LENGTH, false);
        if (StringUtils.isNotBlank(sub)) {
            errors.add("密码包含不安全的字母序列：" + sub);
        }
    }

    private void checkSameChar(String password, List<String> errors) {
        String sub = StringUtils.parseSameChar(password, SAME_CHAR_LENGTH);
        if (StringUtils.isNotBlank(sub)) {
            errors.add("密码包含不安全的相同字符：" + sub);
        }
    }

    private void checkAccountSimilarity(String password, String account,
                                        List<String> errors) {
        if (StringUtils.isNotBlank(account)) {
            if (password.equalsIgnoreCase(account)) {
                errors.add("密码不能与账号相同。");
            } else if (password.toLowerCase().contains(account.toLowerCase())) {
                errors.add("密码不能包含账号信息。");
            }
        }
    }

    private void checkPasswordHistory(String ownerId, String password,
                                      List<String> errors) {
        if (pwdValidateService != null) {
            if (pwdValidateService.isHistoryPwd(ownerId, password)) {
                errors.add("不能使用最近使用过的密码。");
            }
        }
    }

    private void checkWeakPassword(String password, List<String> errors) {
        if (pwdValidateService != null) {
            if (pwdValidateService.isWeakPwd(password.toLowerCase())) {
                errors.add("密码属于常用弱密码。");
            }
        } else if (weakPasswords.contains(password.toLowerCase())) {
            errors.add("密码属于常用弱密码。");
        }
    }

    private Set<String> loadWeakPasswords() {
        Stream<String> stream = null;
        try {
            File weakPasswordFile = new File(StringUtils.joinStr(WebUtils.getAppDir(), "/static/conf/weak-passwords.txt"));
            stream = Files.lines(weakPasswordFile.toPath());
            return stream
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            // 国内常用弱密码
            String internal = "000000,111111,11111111,112233,123123,123321,123456,12345678,654321,666666,888888,abcdef,abcabc,abc123,a1b2c3,aaa111,123qwe,qwerty,qweasd,admin,password,p@ssword,passwd,iloveyou,5201314,asdfghjkl,66666666,88888888";
            // 国外常用弱密码
            String outside = "password,123456,12345678,qwerty,abc123,monkey,1234567,letmein,trustno1,dragon,baseball,111111,iloveyou,master,sunshine,ashley,bailey,passw0rd,shadow,123123,654321,superman,qazwsx,michael,football,asdfghjkl";
            Set<String> wp = CollectionUtils.unique(internal.split(","));
            wp.addAll(CollectionUtils.unique(outside.split(",")));
            return wp;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

}
