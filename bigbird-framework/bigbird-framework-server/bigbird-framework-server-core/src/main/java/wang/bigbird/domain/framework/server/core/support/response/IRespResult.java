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
package wang.bigbird.domain.framework.server.core.support.response;

/**
 * 响应实体信息接口
 *
 * @author Bigbird
 */
public interface IRespResult<T> {

    /**
     * 获取响应码
     *
     * @return 响应码
     */
    Integer getCode();

    /**
     * 获取响应信息
     *
     * @return 响应信息
     */
    String getMsg();

    /**
     * 获取响应数据
     *
     * @return 响应数据
     */
    T getData();

}
