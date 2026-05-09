package wang.bigbird.domain.framework.server.common.retrofit.support.handler;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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

    private SseEmitter emitter;

    public StreamResponseHandler(Call<ResponseBody> call, IStreamCallbacker streamCallback, SseEmitter emitter) {
        this.call = call;
        this.streamCallbacker = streamCallback;
        this.emitter = emitter;
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
                    ResponseBody body = response.body();
                    if (body == null) {
                        // 请求失败
                        StreamResponseException e = new StreamResponseException("The response body is null");
                        errorRef.set(e);
                        if (emitter != null) {
                            emitter.completeWithError(e);
                        }
                        streamCallbacker.onFailed(e);
                        return;
                    }
                    StringBuilder result = new StringBuilder();
                    // 重点：使用 byteStream() 实时读取流
                    try (InputStream is = body.byteStream(); BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                        String line;
                        // 一行一行读取，实时处理
                        while ((line = reader.readLine()) != null) {
                            // 处理流式消息（SSE 格式）
                            if (StringUtils.isNotBlank(line)) {
                                log.debug("Read line:{}", line);
                                if (emitter != null) {
                                    emitter.send(line);
                                }
                                streamCallbacker.onProcess(line);
                                result.append(line).append(StringUtils.getLineSeparator());
                            }
                        }
                        if (emitter != null) {
                            emitter.complete();
                        }
                        // 全部读取完成 → 回调业务
                        streamCallbacker.onSuccess(result.toString());
                    } catch (IOException e) {
                        log.error("Error occurred while executing the request:{}", e.getMessage(), e);
                        errorRef.set(e);
                        if (emitter != null) {
                            emitter.completeWithError(e);
                        }
                        streamCallbacker.onFailed(e);
                    }
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                try {
                    log.error("Error occurred while executing the request:{}", t.getMessage(), t);
                    errorRef.set(t);
                    if (emitter != null) {
                        emitter.completeWithError(t);
                    }
                    streamCallbacker.onFailed(t);
                } finally {
                    latch.countDown();
                }
            }
        });
        try {
            // 阻塞直到请求完全结束
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
