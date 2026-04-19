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

import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.OpenSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.OpenHttpClient;

import java.util.Map;

/**
 * 当服务降级后，调用相关接口时，返回默认响应，相比fallback，
 * 通过fallbackFactory可以获取发生降级导致的异常信息
 *
 * @author Bigbird
 * @RetrofitClient(fallbackFactory = OpenHttpDegradeFallbackFactory.class)
 */
@Component
@Slf4j
public class OpenHttpDegradeFallbackFactory implements FallbackFactory<OpenHttpClient> {

    @Override
    public OpenHttpClient create(Throwable cause) {

        log.error("Fallback exception: {0}.", cause.getMessage(), cause);

        return new OpenHttpClient() {

            @Override
            public Response<OpenSendSmsResponseDTO> doSendMessageByTemplate(Map<String, String> map) {
                return null;
            }

        };
    }
}
