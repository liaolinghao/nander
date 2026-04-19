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
 * 企业微信发送消息时的基本请求参数
 *
 * @author Bigbird
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BaseMessageParam implements Serializable {

    /**
     * 成员ID列表（消息接收者，多个接收者用‘|’分隔，最多支持1000个）。特殊情况：指定为@all，则向关注该企业应用的全部成员发送
     */
    private String touser;
    /**
     * 部门ID列表，多个接收者用‘|’分隔，最多支持100个。当touser为@all时忽略本参数
     */
    private String toparty;
    /**
     * 标签ID列表，多个接收者用‘|’分隔。当touser为@all时忽略本参数
     */
    private String totag;
    /**
     * 消息类型，text消息为：text （支持消息型应用跟主页型应用）
     */
    private String msgtype;
    /**
     * 企业应用的id，整型。可在应用的设置页面查看
     */
    private int agentid;
    /**
     * 表示是否是保密消息，0表示否，1表示是，默认0
     */
    private String safe;

}
