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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache;

import org.springframework.cache.Cache;

import java.util.Collection;
import java.util.Map;

/**
 * 对Spring的{@link Cache}的增强接口，增加了批量的方法
 * 注意与javax的{@link javax.cache.Cache}的区别
 *
 * @author Bigbird
 */
public interface EnhancedCache extends Cache {

    /**
     * 批量查询
     *
     * @param keys 缓存keys
     * @return key-value对
     */
    Map<Object, ValueWrapper> multiGet(Collection<?> keys);

    /**
     * 批量更新
     *
     * @param map key-value对
     */
    void multiPut(Map<?, ?> map);

    /**
     * 批量删除
     *
     * @param keys 缓存keys
     */
    void multiEvict(Collection<?> keys);

}
