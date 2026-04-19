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
package wang.bigbird.domain.framework.common.logging.core.domain.pojo;

import lombok.Data;
import lombok.ToString;

import java.util.Collections;
import java.util.Set;

/**
 * 排除的类和方法包装
 *
 * @author Bigbird
 */
@Data
@ToString
public class ExcludeWrapper {

    /**
     * 排除的类名，例如：TestController
     */
    private Set<String> classNames = Collections.emptySet();

    /**
     * 排除的方法名，例如：TestController.test
     */
    private Set<String> methodNames = Collections.emptySet();

}
