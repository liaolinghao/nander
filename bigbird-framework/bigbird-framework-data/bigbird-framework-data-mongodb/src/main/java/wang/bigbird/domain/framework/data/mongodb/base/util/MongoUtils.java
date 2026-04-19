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
package wang.bigbird.domain.framework.data.mongodb.base.util;

import org.apache.commons.collections4.MapUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.util.Map;

/**
 * MongoDB工具类
 *
 * @author Bigbird
 */
public class MongoUtils {

    private MongoUtils() {
        throw new IllegalStateException();
    }

    /**
     * 构建where条件
     *
     * @param map Map<查询条件key,查询条件value> map
     * @return
     */
    public static Criteria getCriteria(Map<String, Object> map) {
        Criteria criteria = null;
        if (MapUtils.isEmpty(map)) {
            return null;
        }
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            if (first) {
                criteria = Criteria.where(key).is(value);
                first = false;
            } else {
                criteria.and(key).is(value);
            }
        }
        return criteria;
    }

}
