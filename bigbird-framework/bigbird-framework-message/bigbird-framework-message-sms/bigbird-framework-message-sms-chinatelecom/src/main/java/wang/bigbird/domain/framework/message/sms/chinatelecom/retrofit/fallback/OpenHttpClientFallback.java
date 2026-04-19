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
package wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.fallback;

import retrofit2.Response;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.OpenSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.OpenHttpClient;

import java.util.Map;

/**
 * 当服务降级后，调用相关接口时，返回默认响应
 *
 * @author Bigbird
 * @RetrofitClient(fallback = OpenHttpClientFallback.class)
 */
public class OpenHttpClientFallback implements OpenHttpClient {

    @Override
    public Response<OpenSendSmsResponseDTO> doSendMessageByTemplate(Map<String, String> map) {
        return null;
    }

}
