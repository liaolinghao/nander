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
package wang.bigbird.domain.framework.server.web.defence.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.domain.entity.BaseEntity;

/**
 * 对外暴露服务API信息表实体
 *
 * @author Bigbird
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@TableName(value = "td_exposed_api")
public class ExposedApi extends BaseEntity {

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
