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
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.base.tools.support;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.Slf4JLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.Date;

/**
 * 自定义SQL日志打印格式
 *
 * @author Bigbird
 */
public class MyFormattedLogger extends Slf4JLogger {

    private static final String JDBC_P6SPY_PREFIX = "jdbc:p6spy";

    private Logger log = LoggerFactory.getLogger("p6spy");

    @Override
    public void logSQL(int connectionId, String now, long elapsed, Category category, String prepared, String sql, String url) {
        if (!url.startsWith(JDBC_P6SPY_PREFIX)) {
            return;
        }
        String msg = StringUtils.joinStr(url, StringUtils.getLineSeparator(),
                "SQL: ", sql, StringUtils.getLineSeparator(),
                "执行时刻：", DateUtils.format(new Date(Long.valueOf(now)), DateUtils.STANDARD_PATTERN), StringUtils.getLineSeparator(),
                "耗时：", elapsed, "毫秒");
        if (Category.ERROR.equals(category)) {
            this.log.error(msg);
        } else if (Category.WARN.equals(category)) {
            this.log.warn(msg);
        } else if (Category.DEBUG.equals(category)) {
            this.log.debug(msg);
        } else {
            this.log.info(msg);
        }
    }

}
