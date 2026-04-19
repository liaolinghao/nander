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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 响应结果信息构造器
 *
 * @author Bigbird
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class RespResult<T> implements IRespResult<T>, Serializable {

    private static final long serialVersionUID = -6241731956441128714L;

    private Integer code;

    private String msg;

    private T data;

    private RespResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 默认不带数据的请求成功响应
     */
    public static RespResult<Void> ok() {
        return new RespResult<>(IBaseResponseStatus.OK.getCode(), IBaseResponseStatus.OK.getMessage());
    }

    /**
     * 带数据的请求成功响应
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> RespResult<T> ok(T data) {
        return new RespResult<>(IBaseResponseStatus.OK.getCode(), IBaseResponseStatus.OK.getMessage(), data);
    }

    /**
     * 函数式填充响应
     *
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> RespResult<T> ok(Supplier<T> supplier) {
        return new RespResult<>(
                IBaseResponseStatus.OK.getCode(),
                IBaseResponseStatus.OK.getMessage(),
                supplier.get());
    }

    /**
     * 函数式填充响应
     *
     * @param function
     * @param t
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> RespResult<R> ok(Function<T, R> function, T t) {
        return new RespResult<>(
                IBaseResponseStatus.OK.getCode(),
                IBaseResponseStatus.OK.getMessage(),
                function.apply(t));
    }

    /**
     * 构造无数据信息响应
     *
     * @param code
     * @param message
     * @return
     */
    public static RespResult<Void> of(Integer code, String message) {
        return new RespResult<>(code, message);
    }

    /**
     * 构造有数据信息响应
     *
     * @param code
     * @param message
     * @param data
     * @param <T>
     * @return
     */
    public static <T> RespResult<T> of(Integer code, String message, T data) {
        return new RespResult<>(code, message, data);
    }

    /**
     * 构造无数据信息响应
     *
     * @param responseStatus
     * @return
     */
    public static RespResult<Void> of(ResponseStatus responseStatus) {
        return of(responseStatus.getCode(), responseStatus.getMessage());
    }

    /**
     * 判断响应是否成功
     *
     * @param respResult
     * @return
     */
    public static boolean isOk(RespResult<?> respResult) {
        return Optional.ofNullable(respResult)
                .map(resp -> IBaseResponseStatus.OK.getCode().equals(resp.code) || IBaseResponseStatus.OK0.getCode().equals(resp.code))
                .orElse(false);
    }

    /**
     * 如果状态码ok尝试获取响应
     *
     * @param respResult respResult
     * @param exception  状态码不正确时抛出的异常
     * @return Optional of data
     */
    public static <T> Optional<T> getDataIfOk(RespResult<T> respResult, Supplier<? extends RuntimeException> exception) {
        if (!isOk(respResult)) {
            throw exception.get();
        }
        return Optional.ofNullable(respResult.data);
    }

    /**
     * 如果状态码ok尝试获取响应
     *
     * @param respResult respResult
     * @param exception  状态码不正确时抛出的异常
     * @return Optional of data
     */
    public static <T> Optional<T> getDataIfOk(RespResult<T> respResult, Function<RespResult<T>, ? extends RuntimeException> exception) {
        if (!isOk(respResult)) {
            throw exception.apply(respResult);
        }
        return Optional.ofNullable(respResult.data);
    }
}
