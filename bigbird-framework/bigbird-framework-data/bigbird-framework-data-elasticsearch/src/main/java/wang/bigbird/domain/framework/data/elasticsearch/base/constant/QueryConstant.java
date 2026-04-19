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
package wang.bigbird.domain.framework.data.elasticsearch.base.constant;

/**
 * 查询相关的常量定义
 *
 * @author Bigbird
 */
public class QueryConstant {

    /**
     * ES支持的普通查询最大结果集
     */
    public static int MAX_RESULT_SIZE = 10000;

    /**
     * 为减小网络开销，限制查询请求的size最大为2000
     */
    public static int MAX_SEARCH_SIZE = 2000;

}
