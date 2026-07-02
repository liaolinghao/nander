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

import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import org.springframework.util.MimeTypeUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.common.retrofit.base.enums.LogLevelEnum;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 1:1复刻 OkHttp4.9.3 HttpLoggingInterceptor 源码，Java完整重写
 * 扩展：JSON请求/响应体日志长度自动截断，解决超长日志刷屏问题
 *
 * @author Bigbird
 */
public class CustomHttpLoggingInterceptor implements Interceptor {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    private final HttpLoggingInterceptor.Logger logger = HttpLoggingInterceptor.Logger.DEFAULT;

    private LogLevelEnum level;
    /**
     * 配置最大日志长度，<=0不截断
     */
    private final Integer maxLogLength;

    public CustomHttpLoggingInterceptor(LogLevelEnum level, int maxLogLength) {
        this.level = level;
        this.maxLogLength = maxLogLength;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (level == LogLevelEnum.NONE) {
            return chain.proceed(request);
        }
        boolean logBody = level == LogLevelEnum.BODY;
        boolean logHeaders = logBody || level == LogLevelEnum.HEADERS;
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        // 打印请求起始行
        Connection connection = chain.connection();
        String requestStart = "--> " + request.method()
                + " " + request.url()
                + (connection != null ? " " + connection.protocol() : "");
        logger.log(requestStart);
        // 打印请求头
        if (logHeaders) {
            Headers headers = request.headers();

            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. When not
                // already present, force them to be included (if available) so their values are known.
                MediaType contentType = requestBody.contentType();
                if (contentType != null && headers.get("Content-Type") == null) {
                    logger.log("Content-Type: " + contentType);
                }
                long contentLen = requestBody.contentLength();
                if (contentLen != -1L && headers.get("Content-Length") == null) {
                    logger.log("Content-Length: " + contentLen);
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                logger.log(headers.name(i) + ": " + headers.value(i));
            }
            if (!logBody || !hasRequestBody) {
                logger.log("--> END " + request.method());
            } else if (bodyEncoded(request.headers())) {
                logger.log("--> END " + request.method() + " (encoded body omitted)");
            } else {
                // ========== 请求BODY打印 + 截断扩展 ==========
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                logger.log("");
                if (isPlaintext(buffer)) {
                    String bodyStr = buffer.readString(charset);
                    // JSON执行截断
                    if (contentType != null
                            && contentType.toString().contains(MimeTypeUtils.APPLICATION_JSON_VALUE)) {
                        bodyStr = StringUtils.sliceStringBySerializeLength(bodyStr, maxLogLength);
                    }
                    logger.log(bodyStr);
                    logger.log("--> END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
                } else {
                    logger.log("--> END " + request.method() + " (binary " + requestBody.contentLength() + "-byte body omitted)");
                }
            }
        }
        // 执行请求
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            logger.log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = (System.nanoTime() - startNs) / 1_000_000;
        // ========== 复刻原生 响应日志逻辑 ==========
        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        logger.log("<-- " + response.code() + " " + response.message()
                + " " + response.request().url()
                + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ")");
        // 响应头
        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0; i < headers.size(); i++) {
                logger.log(headers.name(i) + ": " + headers.value(i));
            }
            if (!logBody || !hasBody(response)) {
                logger.log("<-- END HTTP");
            } else if (bodyEncoded(response.headers())) {
                logger.log("<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.buffer();
                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                if (!isPlaintext(buffer)) {
                    logger.log("");
                    logger.log("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                    return response;
                }
                if (contentLength != 0) {
                    String bodyStr = buffer.clone().readString(charset);
                    // ========== 响应JSON 字符串截断核心扩展 ==========
                    if (contentType != null
                            && contentType.toString().contains(MimeTypeUtils.APPLICATION_JSON_VALUE)) {
                        bodyStr = StringUtils.sliceStringBySerializeLength(bodyStr, maxLogLength);
                    }
                    logger.log("");
                    logger.log(bodyStr);
                }
                logger.log("<-- END HTTP (" + buffer.size() + "-byte body)");
            }
        }
        return response;
    }

    /**
     * 判断是否明文（非二进制）
     *
     * @param buffer
     * @return
     */
    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = Math.min(buffer.size(), 64);
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                byte b = prefix.readByte();
                int code = b & 0xFF;
                if (code < 0x20 && code != '\t' && code != '\n' && code != '\r') {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false;
        }
    }

    /**
     * 判断Content-Encoding是否压缩
     *
     * @param headers
     * @return
     */
    private static boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !"identity".equalsIgnoreCase(contentEncoding);
    }

    private static boolean hasBody(Response response) {
        if ("HEAD".equals(response.request().method())) {
            return false;
        }
        int code = response.code();
        boolean noBodyCode = (code >= 100 && code <= 199)
                || code == 204
                || code == 304;
        return !noBodyCode;
    }

}
