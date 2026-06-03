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

    public StreamResponseCallbackHandler(IStreamCallbacker streamCallback) {
        this.streamCallbacker = streamCallback;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        StreamResponseHandler streamResponseHandler = new StreamResponseHandler(call, streamCallbacker);
        streamResponseHandler.handleResponse();
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        streamCallbacker.onFailed(t);
    }

}
