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
package wang.bigbird.domain.framework.server.common.retrofit.support.handler;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.common.retrofit.exception.StreamResponseException;
import wang.bigbird.domain.framework.server.common.retrofit.support.callback.IStreamCallbacker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 流式返回处理器
 * 通过在异步线程中执行请求获得响应体，再用该处理器包装响应体执行处理，可以完美处理流式响应
 *
 * @author Bigbird
 */
@Slf4j
public class StreamResponseHandler {

    private Call<ResponseBody> call;

    private IStreamCallbacker streamCallbacker;

    public StreamResponseHandler(Call<ResponseBody> call, IStreamCallbacker streamCallback) {
        this.call = call;
        this.streamCallbacker = streamCallback;
    }

    public void handleResponse() {
        // 创建计数器，等待流式请求完成
        CountDownLatch latch = new CountDownLatch(1);
        // 用来存异常
        AtomicReference<Throwable> errorRef = new AtomicReference<>();
        // Retrofit 异步 + 流式处理
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    streamCallbacker.onStart();
                    ResponseBody body = response.body();
                    if (body == null) {
                        // 请求失败
                        StreamResponseException e = new StreamResponseException("The response body is null");
                        onFailure(call, e);
                        return;
                    }
                    StringBuilder result = new StringBuilder();
                    // 重点：使用 byteStream() 实时读取流
                    try (InputStream is = body.byteStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(is), 128)) {
                        String line;
                        // 一行一行读取，实时处理，不会缓存
                        while ((line = reader.readLine()) != null) {
                            // 处理流式消息（SSE 格式）
                            if (StringUtils.isNotBlank(line)) {
                                log.debug("Read line:{}", line);
                                streamCallbacker.onProcess(line);
                                result.append(line).append(StringUtils.getLineSeparator());
                            }
                        }
                        // 全部读取完成 → 才回调业务
                        streamCallbacker.onSuccess(result.toString());
                    } catch (IOException e) {
                        onFailure(call, e);
                    }
                } catch (IOException e) {
                    onFailure(call, e);
                } finally {
                    // 无论如何，最后释放阻塞
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    log.error("Error occurred while executing the request:{}", t.getMessage(), t);
                    errorRef.set(t);
                    streamCallbacker.onFailed(t);
                } finally {
                    // 失败也要释放阻塞
                    latch.countDown();
                }
            }
        });
        try {
            // 阻塞在这里，直到请求完全结束
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorRef.set(e);
            streamCallbacker.onFailed(e);
        }
        // 如果有异常，直接抛出
        if (errorRef.get() != null) {
            throw new RuntimeException(errorRef.get());
        }
    }

}
