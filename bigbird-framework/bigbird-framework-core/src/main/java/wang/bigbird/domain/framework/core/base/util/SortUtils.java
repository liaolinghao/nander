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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import wang.bigbird.domain.framework.core.base.tool.Assert;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 排序操作工具类
 *
 * @author Bigbird
 */
@Slf4j
public class SortUtils {

    /**
     * boolean类型修饰符
     */
    private static final String BOOLEAN_TYPE = "boolean";
    /**
     * 小写a
     */
    private static final char LOWER_A = 'a';
    /**
     * 小写z
     */
    private static final char LOWER_Z = 'z';

    /**
     * 按照键对map进行排序，其中map存放的key可以是一个复杂对象
     *
     * @param map       待排序的map对象
     * @param fieldName key对象包含的属性名，其类型必须实现Comparable接口
     * @param isDesc    是否降序
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 排序后的map对象
     */
    public static <K, V> Map<K, V> sortMapByKey(Map<K, V> map,
                                                String fieldName, boolean isDesc) {
        Assert.notNull(fieldName, "The parameter fieldName is null.");
        if (map == null || map.isEmpty()) {
            return map;
        }
        Set<K> ks = map.keySet();
        List<K> list = new ArrayList<>(ks);
        sortByField(list, fieldName, isDesc);
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (K k : list) {
            sortedMap.put(k, map.get(k));
        }
        return sortedMap;
    }

    /**
     * 按照值对map进行排序，其中map存放的value可以是一个复杂对象
     *
     * @param map       待排序的map对象
     * @param fieldName value对象包含的属性名，其类型必须实现Comparable接口
     * @param isDesc    是否降序
     * @param <K>       键类型
     * @param <V>       值类型
     * @return 排序后的map对象
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map,
                                                  String fieldName, boolean isDesc) {
        Assert.notNull(fieldName, "The parameter fieldName is null.");
        if (map == null || map.isEmpty()) {
            return map;
        }
        List<Map.Entry<K, V>> entryList = new ArrayList<>(
                map.entrySet());
        sortByField(entryList, fieldName, isDesc);
        Map<K, V> sortedMap = new LinkedHashMap<>();
        Iterator<Map.Entry<K, V>> iterator = entryList.iterator();
        Map.Entry<K, V> tmpEntry;
        while (iterator.hasNext()) {
            tmpEntry = iterator.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    /**
     * 指定属性名进行排序，其中list存放的item可以是一个复杂对象
     *
     * @param list      待排序列表
     * @param fieldName item包含的属性名，其类型必须实现Comparable接口
     * @param isDesc    是否降序
     * @param <E>       元素类型
     */
    public static <E> void sortByField(List<E> list, String fieldName,
                                       boolean isDesc) {
        Assert.notNull(fieldName, "The parameter fieldName is null.");
        if (CollectionUtils.isNullOrEmpty(list) || list.size() == 1) {
            // 不需要排序
            return;
        }
        Class<?> eClazz = getClazzOfElement(list);
        if (eClazz == null) {
            // List中全是null，不排序
            return;
        }
        CommonComparator<E> cmp = getCommonComparator(list, fieldName, eClazz);
        if (cmp == null) {
            // 获取不到合适的排序器，意味着传入对象不符合排序规则
            return;
        }
        if (isDesc) {
            list.sort(Collections.reverseOrder(cmp));
            return;
        }
        list.sort(cmp);
    }

    /**
     * 根据集合中元素类包含的指定属性构造排序器
     *
     * @param list      待排序列表
     * @param fieldName item包含的属性名，其类型必须实现Comparable接口
     * @param eClazz    item对应的Class
     * @param <E>       元素类型
     * @return 构造好的排序器
     */
    private static <E> CommonComparator<E> getCommonComparator(List<E> list,
                                                               String fieldName, Class<?> eClazz) {
        Class<?> sortClazz;
        if (Map.Entry.class.isAssignableFrom(eClazz)) {
            // 集合中如果是键值条目，那么需要特殊处理，取出来值的类型
            sortClazz = ((Map.Entry<?, ?>) list.get(0)).getValue().getClass();
        } else {
            sortClazz = eClazz;
        }
        CommonComparator<E> cmp;
        if (StringUtils.isBlank(fieldName)) {
            if (!Comparable.class.isAssignableFrom(sortClazz)
                    && !sortClazz.isPrimitive()) {
                // 如果未指定排序的属性名，并且元素也不是Comparable类型或者基本类型
                return null;
            }
            cmp = new CommonComparator<>();
        } else {
            Field keyField = getKeyField(sortClazz, fieldName);
            if (keyField == null) {
                // 对象中不存在待排序字段
                return null;
            }
            // 判断待排序属性的类型是否实现了Comparable接口
            Class<?> fieldClazz = keyField.getType();
            if (!Comparable.class.isAssignableFrom(fieldClazz)
                    && !fieldClazz.isPrimitive()) {
                // 如果待排序属性的类型没有实现Comparable接口，并且也不是基本类型，不进行排序
                return null;
            }
            Method getterMethod = getGetterMethod(sortClazz,
                    keyField);
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType type = MethodType.methodType(int.class, Object.class);
            MethodHandle mh = null;
            try {
                mh = lookup.findVirtual(Comparable.class, "compareTo", type);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // Comparable类一定存在compareTo方法，所以这里不可能出现，如果出现就意味着严重问题
                assert (false);
            }
            if (getterMethod != null) {
                // 使用get方法比较
                getterMethod.setAccessible(true);
                cmp = new CommonComparator<>(mh, getterMethod);
            } else {
                // 使用field比较
                keyField.setAccessible(true);
                cmp = new CommonComparator<>(mh, keyField);
            }
        }
        return cmp;
    }

    /**
     * 获取集合中的元素类类型
     *
     * @param list 待排序列表
     * @param <E>  元素类型
     * @return 元素类类型
     */
    private static <E> Class<?> getClazzOfElement(List<E> list) {
        // List中元素的实际类型
        Class<?> eClazz;
        for (E e : list) {
            if (e != null) {
                eClazz = e.getClass();
                return eClazz;
            }
        }
        return null;
    }

    /**
     * 获取待排序属性的Field
     *
     * @param eClazz    类类型
     * @param fieldName 类字段名
     * @return 类字段对象
     */
    private static Field getKeyField(Class<?> eClazz, String fieldName) {
        Field keyField = null;
        try {
            keyField = eClazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            log.error("The List<E> doesn't contain fieldName. That is "
                    + String.format("%s has no field %s.", eClazz, fieldName));
        } catch (SecurityException e) {
            log.error("Deny access to class or field.");
        }
        return keyField;
    }

    /**
     * 获取待排序属性对应的get方法
     *
     * @param eClazz   类类型
     * @param keyField 类字段对象
     * @return 类字段对应的get方法对象
     */
    private static Method getGetterMethod(Class<?> eClazz, Field keyField) {
        Method getterMethod = null;
        StringBuilder getterName;
        if (BOOLEAN_TYPE.equals(keyField.getType().getSimpleName())) {
            getterName = new StringBuilder("is");
        } else {
            getterName = new StringBuilder("get");
        }
        // 首字母转化为大写
        char[] cs = keyField.getName().toCharArray();
        if (cs[0] >= LOWER_A && cs[0] <= LOWER_Z) {
            cs[0] -= 32;
        }
        getterName.append(cs);
        try {
            getterMethod = eClazz.getDeclaredMethod(getterName.toString());
        } catch (NoSuchMethodException e) {
            log.error("The List<E> doesn't contain fieldName's getter method. That is "
                    + String.format("%s has no field %s's getter method.",
                    eClazz, keyField.getName()));
        } catch (SecurityException e) {
            log.error("Deny access to class or field.");
        }
        return getterMethod;
    }

    /**
     * 基于反射机制，针对复杂对象按照某一属性进行排序实现的公共比较器
     *
     * @param <E>
     */
    private static class CommonComparator<E> implements Comparator<E> {
        /**
         * 待比较属性的get方法
         */
        Method getMethod = null;
        /**
         * 待比较属性
         */
        Field fieldToGet = null;
        /**
         * 比较方法
         */
        MethodHandle cmpMethodHandle;

        /**
         * 默认按照元素比较
         */
        public CommonComparator() {

        }

        /**
         * 通过get方法取待比较属性值，利用反射机制对复杂对象实现按某一属性值进行比较
         *
         * @param cmpMethodHandle 比较方法
         * @param getMethod       取值方法
         */
        public CommonComparator(MethodHandle cmpMethodHandle, Method getMethod) {
            this.getMethod = getMethod;
            this.cmpMethodHandle = cmpMethodHandle;
        }

        /**
         * 通过Field取待比较属性值
         *
         * @param cmpMethodHandle 比较方法
         * @param fieldToGet      比较字段
         */
        public CommonComparator(MethodHandle cmpMethodHandle, Field fieldToGet) {
            this.cmpMethodHandle = cmpMethodHandle;
            this.fieldToGet = fieldToGet;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public int compare(E e1, E e2) {
            if (e2 == null) {
                return -1;
            } else if (e1 == null) {
                return 1;
            }
            try {
                Object o1;
                Object o2;
                if (Map.Entry.class.isAssignableFrom(e1.getClass())) {
                    o1 = ((Map.Entry<?, ?>) e1).getValue();
                    o2 = ((Map.Entry<?, ?>) e2).getValue();
                } else {
                    o1 = e1;
                    o2 = e2;
                }
                // 优先以get方法比较，null往后放置
                if (getMethod != null) {
                    if (getMethod.invoke(o2) == null) {
                        return -1;
                    } else if (getMethod.invoke(o1) == null) {
                        return 1;
                    }
                    return (int) cmpMethodHandle.invokeExact(
                            (Comparable) getMethod.invoke(o1),
                            getMethod.invoke(o2));
                }
                if (fieldToGet != null) {
                    if (fieldToGet.get(o2) == null) {
                        return -1;
                    } else if (fieldToGet.get(o1) == null) {
                        return 1;
                    }
                    return (int) cmpMethodHandle
                            .invokeExact((Comparable) fieldToGet.get(o1),
                                    fieldToGet.get(o2));
                }
                // 都为空意味着没有传递字段名，那么元素类型一定是可比较类型或者基本类型
                return ((Comparable) o1).compareTo(o2);
            } catch (Exception e) {
                log.error("Compare:", e);
                System.err
                        .println("If sortByField() isn't modified, it won't print errorStackTrace. Default return 0");
            } catch (Throwable e) {
                log.error("Compare:", e);
            }
            return 0;
        }
    }

}
