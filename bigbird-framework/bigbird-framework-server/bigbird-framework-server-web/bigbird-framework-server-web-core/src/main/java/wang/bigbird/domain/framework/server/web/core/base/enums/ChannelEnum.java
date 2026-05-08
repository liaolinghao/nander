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
package wang.bigbird.domain.framework.server.web.core.base.enums;

import wang.bigbird.domain.framework.core.base.util.enums.ValuedEnum;

/**
 * 访问渠道
 *
 * @author Bigbird
 */
public enum ChannelEnum implements ValuedEnum<String> {

    WINDOWS("WINDOWS软件"),
    MAC("MAC软件"),
    LINUX("LINUX软件"),
    WEB("浏览器"),
    ANDROID("ANDROID"),
    IOS("IOS"),
    HarmonyOS("HarmonyOS"),
    RTOS("RTOS"),
    MP("小程序"),
    SERVER("第三方服务"),
    IGNORE("忽略渠道"),
    UNSPECIFIED("未指定渠道"),
    UNKNOW("未知渠道");

    private String channel;

    ChannelEnum(String channel) {
        this.channel = channel;
    }

    /**
     * 获取访问渠道枚举对象
     *
     * @param code 访问渠道代码
     * @return 访问渠道枚举对象
     */
    public static ChannelEnum getInstanceByCode(String code) {
        for (ChannelEnum ae : ChannelEnum.values()) {
            if (ae.name().equalsIgnoreCase(code)) {
                return ae;
            }
        }
        return UNKNOW;
    }

    /**
     * 获取访问渠道枚举对象
     *
     * @param channel 访问渠道名称
     * @return 访问渠道枚举对象
     */
    public static ChannelEnum getInstanceByChannel(String channel) {
        for (ChannelEnum ce : ChannelEnum.values()) {
            if (ce.value().equalsIgnoreCase(channel)) {
                return ce;
            }
        }
        return UNKNOW;
    }

    @Override
    public String value() {
        return name();
    }

    /**
     * 作为RPC接口参数，序列化需要利用该方法
     *
     * @return
     */
    public String getChannel() {
        return channel;
    }

}
