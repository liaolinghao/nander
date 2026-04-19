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
package wang.bigbird.domain.framework.data.kafka.support.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import wang.bigbird.domain.framework.core.base.util.StringUtils;


/**
 * 生产者条件
 *
 * @author Bigbird
 */
public class ProducerCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String addresses = context.getEnvironment().getProperty("bigbird.data.kafka.producer.addresses");
        if (StringUtils.isBlank(addresses)) {
            addresses = context.getEnvironment().getProperty("spring.kafka.producer.bootstrap-servers");
        }
        if (StringUtils.isBlank(addresses)) {
            addresses = context.getEnvironment().getProperty("spring.kafka.bootstrap-servers");
        }
        return StringUtils.isNotBlank(addresses);
    }
}
