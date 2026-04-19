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
package wang.bigbird.domain.framework.core.base.util;

import cn.hutool.http.HTMLFilter;
import net.dreamlu.mica.xss.utils.XssUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 关于Xss的通用操作
 *
 * @author Bigbird
 */
public class XssUtils {

    /**
     * 给html字符串进行xss过滤处理
     *
     * @param html
     * @return 过滤后字符串
     */
    public static String xssFilterByJsoup(String html, String baseUri) {
        return XssUtil.clean(html);
    }

    /**
     * 给html字符串进行xss过滤处理，该方法过于严格，会造成一些样式丢掉
     *
     * @param html
     * @return 过滤后字符串
     */
    public static String xssFilter(String html) {
        if (StringUtils.isBlank(html)) {
            return "";
        }
        return new HTMLFilter().filter(html);
    }

}
