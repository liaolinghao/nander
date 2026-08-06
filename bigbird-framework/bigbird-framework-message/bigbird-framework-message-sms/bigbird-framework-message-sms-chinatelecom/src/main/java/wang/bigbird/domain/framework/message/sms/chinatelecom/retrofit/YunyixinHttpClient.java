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
package wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit;

import com.github.lianjiatech.retrofit.spring.boot.annotation.OkHttpClientBuilder;
import com.github.lianjiatech.retrofit.spring.boot.annotation.RetrofitClient;
import net.dreamlu.mica.core.spring.SpringContextUtil;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.YunyixinSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.fallback.YunyixinHttpDegradeFallbackFactory;
import wang.bigbird.domain.framework.message.sms.chinatelecom.support.holder.OkHttpHolder;

import java.util.Map;

/**
 * 云翼信平台接口调用器
 *
 * @author Bigbird
 */
@RetrofitClient(baseUrl = "${bigbird.message.sms.yunyixin.baseUrl:https://yyx.saas.189.cn:8070}", fallbackFactory = YunyixinHttpDegradeFallbackFactory.class)
public interface YunyixinHttpClient {

    /**
     * @return
     * @OkHttpClientBuilder 静态方法优先级最高，
     * 该接口会放弃 starter 默认全局 OkHttp，
     * 使用该自定义支持 HTTPS 的客户端，相关原理为：
     * 启动扫描 @RetrofitClient 接口
     * 反射查找接口内部带有 @OkHttpClientBuilder、返回值为 OkHttpClient.Builder 的静态方法
     * 找到 → 执行该静态方法构建专属 OkHttpClient 实例给当前 Api
     * 找不到 → 使用 starter 全局默认 OkHttpClient
     */
    @OkHttpClientBuilder
    static OkHttpClient.Builder okHttpBuilder() {
        return SpringContextUtil.getBean(OkHttpHolder.class).getHttpsBuilder();
    }

    /**
     * 发送消息
     *
     * @param map 消息参数
     * @return 发送结果
     */
    @POST("/jt3netsmsservicehttp/httpservices/capService")
    @Headers("Content-Type: application/json;charset=UTF-8")
    Response<YunyixinSendSmsResponseDTO> doSendMessage(@Body Map<String, String> map);

}
