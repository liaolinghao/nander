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
package wang.bigbird.domain.framework.message.sms.chinatelecom.support.holder;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * OkHttp持有器
 *
 * @author Bigbird
 */
@Component
public class OkHttpHolder {

    @Autowired
    private OkHttpClient chinatelecomHttpClient;

    /**
     * 返回副本Builder，复用同一个连接池
     * 解决多Api多个OkHttp连接池隔离的性能问题
     *
     * @return
     */
    public OkHttpClient.Builder getHttpsBuilder() {
        return chinatelecomHttpClient.newBuilder();
    }

}
