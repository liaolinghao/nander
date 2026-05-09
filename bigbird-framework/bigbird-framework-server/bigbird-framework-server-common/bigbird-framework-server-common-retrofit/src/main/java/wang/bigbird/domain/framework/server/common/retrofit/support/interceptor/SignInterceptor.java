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
package wang.bigbird.domain.framework.server.common.retrofit.support.interceptor;

import com.github.lianjiatech.retrofit.spring.boot.interceptor.BasePathMatchInterceptor;
import lombok.Setter;
import okhttp3.*;
import okio.Buffer;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.server.core.base.util.SignatureUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口请求添加签名
 *
 * @author Bigbird
 */
@Component
public class SignInterceptor extends BasePathMatchInterceptor {

    @Setter
    private String appKey;

    @Setter
    private String appSecret;

    @Override
    public Response doIntercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl url = request.url();
        String urlInfo = Coder.urlDecoderUtf8(url.url().toString());
        Map<String, String> params = new HashMap<>(CollectionUtils.initialMapCapacity(1));
        params.put("appKey", appKey);
        Map<String, String> headers = null;
        RequestBody requestBody = request.body();
        String jsonBody = "";
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            //编码设为UTF-8
            Charset charset = Charset.forName(Coder.DEFAULT_ENCODING);
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(Charset.forName(Coder.DEFAULT_ENCODING));
            }
            jsonBody = buffer.readString(charset);
        }
        String signature = SignatureUtils.signRequest(urlInfo, params, headers, jsonBody, appSecret);
        HttpUrl newUrl = url.newBuilder()
                .addQueryParameter("appKey", appKey)
                .addQueryParameter("signature", signature)
                .build();
        Request newRequest = request.newBuilder()
                .url(newUrl)
                .build();
        return chain.proceed(newRequest);
    }
}
