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
package wang.bigbird.domain.framework.server.web.ban.service.base;

import java.util.Set;

/**
 * 禁用词变更服务
 *
 * @author Bigbird
 */
public interface IForbidWordChangeService {

    /**
     * 添加禁用词
     *
     * @param words 禁用词列表
     */
    void add(Set<String> words);

    /**
     * 删除禁用词
     *
     * @param words 禁用词列表
     */
    void remove(Set<String> words);

    /**
     * 刷新禁用词库
     */
    void refresh();

}
