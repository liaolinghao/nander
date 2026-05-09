package wang.bigbird.domain.framework.server.common.retrofit.support.handler;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import wang.bigbird.domain.framework.server.common.retrofit.support.callback.IStreamCallbacker;

/**
 * 流式返回回调处理器，
 * 仅仅适合能在几秒内返回全部数据的流式处理，对于长时间的流式返回，该处理器不适用，会导致流丢失
 *
 * @author Bigbird
 */
@Slf4j
public class StreamResponseCallbackHandler implements Callback<ResponseBody> {

    private IStreamCallbacker streamCallbacker;

    private SseEmitter emitter;

    public StreamResponseCallbackHandler(IStreamCallbacker streamCallback, SseEmitter emitter) {
        this.streamCallbacker = streamCallback;
        this.emitter = emitter;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        StreamResponseHandler streamResponseHandler = new StreamResponseHandler(call, streamCallbacker, emitter);
        streamResponseHandler.handleResponse();
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        streamCallbacker.onFailed(t);
    }

}
