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
package wang.bigbird.domain.framework.server.web.defence.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 对外暴露的接口安全控制配置项
 *
 * @author Bigbird
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiSecurityItem implements Serializable {

    private String apiCode;

    private String apiName;

    private String apiUrl;

    private String apiType;

    private Boolean ipCheckEnable;

    private Boolean signEnable;

    private Boolean antiReplayEnable;

    private Boolean requestDecryptEnable;

    private Boolean responseEncryptEnable;

    private String ipWhiteList;

    private String ipBlackList;

    private String appWhiteList;

    private String appBlackList;

}
