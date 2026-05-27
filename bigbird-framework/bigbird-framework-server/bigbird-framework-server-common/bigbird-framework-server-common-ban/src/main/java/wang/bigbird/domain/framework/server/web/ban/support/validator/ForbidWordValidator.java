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
package wang.bigbird.domain.framework.server.web.ban.support.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.ban.service.base.IForbidWordValidateService;
import wang.bigbird.domain.framework.server.web.ban.support.annotation.ForbidWord;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * 禁用词校验器
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class ForbidWordValidator implements ConstraintValidator<ForbidWord, String> {

    private boolean enable;

    private boolean disableDefaultMessage;

    @Autowired(required = false)
    private IForbidWordValidateService forbidWordValidateService;

    @Override
    public void initialize(ForbidWord annotation) {
        this.enable = annotation.enable();
        this.disableDefaultMessage = annotation.disableDefaultMessage();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 关闭校验 或 内容为空，直接通过
        if (forbidWordValidateService == null || !enable || StringUtils.isEmpty(value)) {
            return true;
        }
        if (disableDefaultMessage) {
            List<String> forbidWords = forbidWordValidateService.forbidWordList(value);
            if (CollectionUtils.isNotEmpty(forbidWords)) {
                // 关闭默认错误提示
                context.disableDefaultConstraintViolation();
                // 创建自定义错误提示
                context.buildConstraintViolationWithTemplate("内容包含敏感词：" + StringUtils.collectionToCommaDelimitedString(forbidWords))
                        .addConstraintViolation();
                return false;
            }
            return true;
        } else {
            return !forbidWordValidateService.containsForbidWord(value);
        }
    }

}
