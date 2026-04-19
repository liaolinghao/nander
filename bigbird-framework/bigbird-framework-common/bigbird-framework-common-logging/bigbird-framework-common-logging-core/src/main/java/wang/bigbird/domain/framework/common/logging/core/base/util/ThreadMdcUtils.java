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

import org.slf4j.MDC;
import wang.bigbird.domain.framework.common.logging.core.base.constant.LogConstants;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.Map;

/**
 * 日志线程装饰工具
 *
 * @author Bigbird
 */
public class ThreadMdcUtils {

    /**
     * 采用MDC实现日志跟踪，为了使同一请求产生的异步任务中记录的链路ID保持一致，
     * 需要采用该异步任务封装器对异步任务进行封装后再执行
     *
     * @param task    异步任务
     * @param context 日志信息上下文
     * @return 封装后的异步任务
     */
    public static Runnable wrapAsync(Runnable task, Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            if (MDC.get(LogConstants.TRACE_ID) == null) {
                MDC.put(LogConstants.TRACE_ID, StringUtils.getUuid());
            }
            try {
                task.run();
            } finally {
                MDC.clear();
            }
        };
    }

}
