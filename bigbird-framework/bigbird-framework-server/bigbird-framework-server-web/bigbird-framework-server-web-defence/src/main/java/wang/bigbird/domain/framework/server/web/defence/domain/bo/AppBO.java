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
package wang.bigbird.domain.framework.server.web.defence.domain.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用信息
 *
 * @author Bigbird
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppBO implements Serializable {

    private Long id;

    private String appName;

    private String appCode;

    private String appKey;

    private String appSecret;

    private Byte appType;

    private String contacts;

    private String mobilephone;

    private String limitIps;

    private Boolean trusted;

    private Boolean packaged;

    private String publicKey;

    private String privateKey;

}
