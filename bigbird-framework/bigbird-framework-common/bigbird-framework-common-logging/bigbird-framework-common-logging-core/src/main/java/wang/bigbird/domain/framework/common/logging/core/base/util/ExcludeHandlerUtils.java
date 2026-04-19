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
package wang.bigbird.domain.framework.common.logging.core.base.util;

import cn.hutool.core.collection.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import wang.bigbird.domain.framework.common.logging.core.domain.pojo.ExcludeWrapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 排除处理
 *
 * @author Bigbird
 */
public class ExcludeHandlerUtils {

    private ExcludeHandlerUtils() {
    }


    /**
     * 构建 ExcludeWrapper
     *
     * @author Bigbird
     */
    public static ExcludeWrapper buildExcludeWrapper(List<String> excludes) {
        if (CollectionUtil.isEmpty(excludes)) {
            return null;
        }
        ExcludeWrapper excludeWrapper = new ExcludeWrapper();
        Set<String> classNames = new HashSet<>();
        Set<String> methodNames = new HashSet<>();
        for (String exclude : excludes) {
            if (StringUtils.isBlank(exclude)) {
                continue;
            }
            if (exclude.contains(".")) {
                methodNames.add(exclude);
            } else {
                classNames.add(exclude);
            }
        }
        excludeWrapper.setClassNames(classNames);
        excludeWrapper.setMethodNames(methodNames);
        return excludeWrapper;
    }

}
