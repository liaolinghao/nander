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
package wang.bigbird.domain.framework.core.base.constant;

/**
 * 通用常量
 *
 * @author Bigbird
 */
public class CommonConstants {

    private CommonConstants() {
        throw new IllegalStateException();
    }

    public static final String SPACE = " ";
    public static final String TAB = "\t";
    public static final String DOT = ".";
    public static final String DOT_REGEX = "\\.";
    public static final String PERCENT = "%";
    public static final String DOUBLE_DOT = "..";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";
    public static final String PROTOCOL_DELIMITER = "://";
    public static final String SLASH = "/";
    public static final String BACKSLASH = "\\";
    public static final String CR = "\r";
    public static final String LF = "\n";
    public static final String CRLF = "\r\n";
    public static final String UNDERLINE = "_";
    public static final String DASHED = "-";
    public static final String COMMA = ",";
    public static final String EQUAL = "=";
    public static final String POUND_SIGN = "#";
    public static final String DELIM_START = "{";
    public static final String DELIM_END = "}";
    public static final String PARENTHESIS_START = "(";
    public static final String PARENTHESIS_END = ")";
    public static final String PLACE_HOLDER = "{}";
    public static final String BRACKET_START = "[";
    public static final String BRACKET_END = "]";
    public static final String COLON = ":";
    public static final String QUESTION_MARK = "?";
    public static final String DOUBLE_QUOTES = "\"";
    public static final String SINGLE_QUOTE = "'";
    public static final String SEPARATOR = "~";
    public static final String ANY = "*";
    public static final String AMP = "&";
    public static final String DOLLAR = "$";
    public static final String EMPTY = "";
    public static final String NULL = "null";

    public static final String HTML_NBSP = "&nbsp;";
    public static final String HTML_AMP = "&amp;";
    public static final String HTML_QUOTE = "&quot;";
    public static final String HTML_APOS = "&apos;";
    public static final String HTML_LT = "&lt;";
    public static final String HTML_GT = "&gt;";

    public static final String ENC = "ENC";

    public static final String IGNORE = "ignore";

    public static final int ONE = 1;

    public static final int ONE_THOUSAND = 1000;

    public static final int ONE_HUNDRED_THOUSAND = 100000;

    public static class ActionType {

        private ActionType() {
            throw new IllegalStateException();
        }

        /**
         * 发短信
         */
        public static final String SMS = "sms";

        /**
         * 发邮件
         */
        public static final String EMAIL = "email";

    }

    /**
     * 排序方式描述
     *
     * @author Bigbird
     */
    public static class OrderType {

        private OrderType() {
            throw new IllegalStateException();
        }

        /**
         * 升序
         */
        public static final String ASC = "asc";

        /**
         * 降序
         */
        public static final String DESC = "desc";
    }


    /**
     * 删除标记
     *
     * @author Bigbird
     */
    public static class DelFlag {

        private DelFlag() {
            throw new IllegalStateException();
        }

        /**
         * 删除
         */
        public static final Integer DEL = 1;

        /**
         * 未删除
         */
        public static final Integer UN_DEL = 0;
    }

    /**
     * 开关
     *
     * @author Bigbird
     */
    public static class Switch {

        private Switch() {
            throw new IllegalStateException();
        }

        /**
         * 启用
         */
        public static final Integer ENABLE = 1;

        /**
         * 禁用
         */
        public static final Integer DISABLE = 0;
    }


    /**
     * 布尔字符串
     *
     * @author Bigbird
     */
    public static class StringBoolean {

        private StringBoolean() {
            throw new IllegalStateException();
        }

        /**
         * true
         */
        public static final String TRUE = "true";

        /**
         * false
         */
        public static final String FALSE = "false";
    }


    /**
     * 缓存
     *
     * @author Bigbird
     */
    public static class Cache {

        private Cache() {
            throw new IllegalStateException();
        }

        /**
         * 缓存名称分隔符
         */
        public static final String CACHE_NAME_SEPARATOR = "%%";
    }
}
