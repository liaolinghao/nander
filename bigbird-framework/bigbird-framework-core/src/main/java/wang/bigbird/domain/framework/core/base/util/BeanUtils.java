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
package wang.bigbird.domain.framework.core.base.util;

import cn.hutool.core.bean.BeanUtil;
import lombok.SneakyThrows;

import java.util.*;

/**
 * Bean 工具类
 *
 * @author Bigbird
 */
public class BeanUtils {

    /**
     * 禁止实例化
     */
    private BeanUtils() {
    }

    /**
     * 对象->Map
     *
     * @param o 对象，必须是标准 Java Bean（有 getter/setter 的实体类）
     * @return Map
     */
    public static Map<String, Object> toMap(Object o) {
        return (o == null) ? new HashMap<>(2) : BeanUtil.beanToMap(o, false, true);
    }

    /**
     * Map数据->bean对象
     *
     * @param map 数据map
     * @return bean对象
     */
    @SneakyThrows
    public static <T> T toBean(Map<String, Object> map, Class<T> beanClass) {
        if (null == map || map.isEmpty()) {
            return beanClass.getDeclaredConstructor().newInstance();
        }
        return BeanUtil.mapToBean(map, beanClass, true);
    }

    /**
     * 拷贝集合对象到新的集合中
     *
     * @param sourceList       原集合
     * @param destinationClass 类
     * @param <T>              类型
     * @return 拷贝后的集合类
     */
    public static <T> List<T> copyList(Collection<?> sourceList,
                                       Class<T> destinationClass) {
        List<T> destinationList = new ArrayList<>();
        for (Object sourceObject : sourceList) {
            T destinationObject = BeanUtil.copyProperties(sourceObject, destinationClass);
            destinationList.add(destinationObject);
        }
        return destinationList;
    }
}
