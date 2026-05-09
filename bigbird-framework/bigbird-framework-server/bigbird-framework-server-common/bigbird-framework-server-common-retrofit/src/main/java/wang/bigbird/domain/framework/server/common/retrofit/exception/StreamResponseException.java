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
package wang.bigbird.domain.framework.server.common.retrofit.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式响应异常
 * <p>
 *
 * @author Bigbird
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamResponseException extends RuntimeException {

    private Integer code;

    public StreamResponseException(int code, String message) {
        super(message);
        this.code = code;
    }

    public StreamResponseException(String message) {
        super(message);
    }

}
