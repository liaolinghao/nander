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
package wang.bigbird.domain.framework.server.web.ban.domain.pojo.msg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wang.bigbird.domain.framework.server.web.ban.base.enums.RefreshTypeEnum;

import java.io.Serializable;
import java.util.Set;

/**
 * 禁用词刷新事件
 *
 * @author Bigbird
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForbidWordRefreshEvent implements Serializable {

    /**
     * 禁用词刷新事件类型
     */
    private RefreshTypeEnum refreshType;

    /**
     * 禁用词集合
     */
    private Set<String> words;

}
