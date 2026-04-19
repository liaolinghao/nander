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
package wang.bigbird.domain.framework.message.wechat.domain.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 企业微信发送文本消息时的请求参数
 *
 * @author Bigbird
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TextMessageParam extends BaseMessageParam implements Serializable {

    /**
     * 文本消息
     */
    private Text text;

    /**
     * 文本消息载体
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Text implements Serializable {

        private String content;

    }

}
