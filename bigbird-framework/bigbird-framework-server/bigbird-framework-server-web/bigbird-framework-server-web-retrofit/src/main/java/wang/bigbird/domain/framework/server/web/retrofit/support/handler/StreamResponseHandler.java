package wang.bigbird.domain.framework.server.web.retrofit.support.handler;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.retrofit.exception.StreamResponseException;
import wang.bigbird.domain.framework.server.web.retrofit.support.callback.IStreamCallbacker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

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
        try (ResponseBody body = call.execute().body()) {
            if (body == null) {
                // 请求失败
                streamCallbacker.onFailed(new StreamResponseException("The response body is null"));
                return;
            }
            // 读取完整的流数据，直到全部读完
            StringBuilder result = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
                String line;
                // 逐行读取，直到流结束
                while ((line = reader.readLine()) != null) {
                    log.debug("Read line:{}", line);
                    result.append(line).append(StringUtils.getLineSeparator());
                }
                // 全部读取完成 → 才回调业务
                streamCallbacker.onSuccess(result.toString());
            } catch (IOException e) {
                streamCallbacker.onFailed(e);
            }
        } catch (Exception e) {
            log.error("Error occurred while executing the request:{}", e.getMessage(), e);
            streamCallbacker.onFailed(e);
        }
    }

}
