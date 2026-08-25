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

import org.dozer.DozerBeanMapper;

import java.lang.reflect.Field;
import java.util.*;


/**
 * 对象转换，采用映射配置可以支持对象之间不同变量名的值传递，
 * 此外，该对象对于复杂对象属性的复制采用的是深度复制，不存在引用问题
 * <p>
 * 项目主页：http://dozer.sourceforge.net/dependencies.html
 * <p>
 * 映射配置，可在对应bean字段上加注解 @Mapping("字段名")，即可完成变量名的转换
 *
 * @author Bigbird
 */
public class BeanMapperUtils {

    private static DozerBeanMapper dozer = new DozerBeanMapper();

    static {
        dozer.setMappingFiles(Collections.singletonList("dozerJdk8Converters.xml"));
    }

    /**
     * 构造新的destinationClass实例对象，通过source对象中的字段内容
     * 映射到destinationClass实例对象中，并返回新的destinationClass实例对象。
     *
     * @param source           源数据对象
     * @param destinationClass 要构造新的实例对象Class
     */
    public static <T> T map(Object source, Class<T> destinationClass) {
        if (source == null) {
            return null;
        }
        return dozer.map(source, destinationClass);
    }

    /**
     * 把原对象集合映射到新的目标对象集合中
     *
     * @param sourceList       原对象集合
     * @param destinationClass 目标对象类型
     * @return 目标对象集合
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> mapList(Collection<?> sourceList,
                                      Class<T> destinationClass) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }
        List<T> destinationList = new ArrayList(sourceList.size());
        for (Iterator<?> iterator = sourceList.iterator(); iterator.hasNext(); ) {
            Object sourceObject = iterator.next();
            Object destinationObject = dozer
                    .map(sourceObject, destinationClass);
            destinationList.add((T) destinationObject);
        }
        return destinationList;
    }

    /**
     * 将对象source的所有属性值拷贝到对象destination中.
     *
     * @param source      对象source
     * @param destination 对象destination
     */
    public static void copy(Object source, Object destination) {
        dozer.map(source, destination);
    }

    /**
     * 将目标对象的所有属性转换成Map对象
     *
     * @param target
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static <T> Map<String, Object> toMap(T target)
            throws IllegalArgumentException, IllegalAccessException {
        return toMap(target, false);
    }

    /**
     * 将目标对象的所有属性转换成Map对象
     *
     * @param target       目标对象
     * @param ignoreParent 是否忽略父类的属性
     * @return Map
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static <T> Map<String, Object> toMap(T target, boolean ignoreParent)
            throws IllegalArgumentException, IllegalAccessException {
        return toMap(target, ignoreParent, false);
    }

    /**
     * 将目标对象的所有属性转换成Map对象
     *
     * @param target           目标对象
     * @param ignoreParent     是否忽略父类的属性
     * @param ignoreEmptyValue 是否不把空值添加到Map中
     * @return Map
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static <T> Map<String, Object> toMap(T target, boolean ignoreParent,
                                                boolean ignoreEmptyValue) throws IllegalArgumentException,
            IllegalAccessException {
        return toMap(target, ignoreParent, ignoreEmptyValue, new String[0]);
    }

    /**
     * 将目标对象的所有属性转换成Map对象
     *
     * @param target           目标对象
     * @param ignoreParent     是否忽略父类的属性
     * @param ignoreEmptyValue 是否不把空值添加到Map中
     * @param ignoreProperties 不需要添加到Map的属性名
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static <T> Map<String, Object> toMap(T target, boolean ignoreParent,
                                                boolean ignoreEmptyValue, String... ignoreProperties)
            throws IllegalArgumentException, IllegalAccessException {
        List<Field> fields = ObjectUtils.getAccessibleFields(target.getClass(),
                ignoreParent);
        // 根据fields的大小计算HashMap的初始容量
        int initialCapacity = CollectionUtils.initialMapCapacity(fields.size());
        Map<String, Object> map = new HashMap<>(initialCapacity);
        for (Iterator<Field> it = fields.iterator(); it.hasNext(); ) {
            Field field = it.next();
            Object value = field.get(target);
            if (ignoreEmptyValue && ObjectUtils.isEmpty(value)) {
                continue;
            }
            boolean flag = true;
            String key = field.getName();
            for (String ignoreProperty : ignoreProperties) {
                if (key.equals(ignoreProperty)) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                map.put(key, value);
            }
        }
        return map;
    }

}
