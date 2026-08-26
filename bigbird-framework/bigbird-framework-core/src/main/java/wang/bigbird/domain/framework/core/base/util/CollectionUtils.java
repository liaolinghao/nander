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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * 集合操作工具类
 *
 * @author Bigbird
 */
public class CollectionUtils {

    private CollectionUtils() {
        throw new IllegalStateException();
    }

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }


    /**
     * 检测是否不为空
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }


    /**
     * Return {@code true} if the supplied Map is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }


    /**
     * 检测是否不为空
     *
     * @param map the Map to check
     * @return whether the given Map is empty
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Convert the supplied array into a List. A primitive array gets converted
     * into a List of the appropriate wrapper type.
     * <p><b>NOTE:</b> Generally prefer the standard {@link Arrays#asList} method.
     * This {@code arrayToList} method is just meant to deal with an incoming Object
     * value that might be an {@code Object[]} or a primitive array at runtime.
     * <p>A {@code null} source value will be converted to an empty List.
     *
     * @param source the (potentially primitive) array
     * @return the converted List result
     * @see ObjectUtils#toObjectArray(Object)
     * @see Arrays#asList(Object[])
     */
    @SuppressWarnings("rawtypes")
    public static List arrayToList(Object source) {
        return Arrays.asList(ObjectUtils.toObjectArray(source));
    }

    /**
     * Merge the given array into the given Collection.
     *
     * @param array      the array to merge (may be {@code null})
     * @param collection the target Collection to merge the array into
     */
    @SuppressWarnings("unchecked")
    public static <E> void mergeArrayIntoCollection(Object array, Collection<E> collection) {
        Object[] arr = ObjectUtils.toObjectArray(array);
        for (Object elem : arr) {
            collection.add((E) elem);
        }
    }

    /**
     * Merge the given Properties instance into the given Map,
     * copying all properties (key-value pairs) over.
     * <p>Uses {@code Properties.propertyNames()} to even catch
     * default properties linked into the original Properties instance.
     *
     * @param props the Properties instance to merge (may be {@code null})
     * @param map   the target Map to merge the properties into
     */
    @SuppressWarnings("unchecked")
    public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
        if (props != null) {
            for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements(); ) {
                String key = (String) en.nextElement();
                Object value = props.get(key);
                if (value == null) {
                    // Allow for defaults fallback or potentially overridden accessor...
                    value = props.getProperty(key);
                }
                map.put((K) key, (V) value);
            }
        }
    }


    /**
     * Check whether the given Iterator contains the given element.
     *
     * @param iterator the Iterator to check
     * @param element  the element to look for
     * @return {@code true} if found, {@code false} otherwise
     */
    public static boolean contains(Iterator<?> iterator, Object element) {
        if (iterator != null) {
            while (iterator.hasNext()) {
                Object candidate = iterator.next();
                if (ObjectUtils.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given Enumeration contains the given element.
     *
     * @param enumeration the Enumeration to check
     * @param element     the element to look for
     * @return {@code true} if found, {@code false} otherwise
     */
    public static boolean contains(Enumeration<?> enumeration, Object element) {
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                Object candidate = enumeration.nextElement();
                if (ObjectUtils.nullSafeEquals(candidate, element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check whether the given Collection contains the given element instance.
     * <p>Enforces the given instance to be present, rather than returning
     * {@code true} for an equal element as well.
     *
     * @param collection the Collection to check
     * @param element    the element to look for
     * @return {@code true} if found, {@code false} otherwise
     */
    public static boolean containsInstance(Collection<?> collection, Object element) {
        if (collection != null) {
            for (Object candidate : collection) {
                if (candidate == element) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return {@code true} if any element in '{@code candidates}' is
     * contained in '{@code source}'; otherwise returns {@code false}.
     *
     * @param source     the source Collection
     * @param candidates the candidates to search for
     * @return whether any of the candidates has been found
     */
    public static boolean containsAny(Collection<?> source, Collection<?> candidates) {
        return findFirstMatch(source, candidates) != null;
    }

    /**
     * Return the first element in '{@code candidates}' that is contained in
     * '{@code source}'. If no element in '{@code candidates}' is present in
     * '{@code source}' returns {@code null}. Iteration order is
     * {@link Collection} implementation specific.
     *
     * @param source     the source Collection
     * @param candidates the candidates to search for
     * @return the first present object, or {@code null} if not found
     */
    public static <E> E findFirstMatch(Collection<?> source, Collection<E> candidates) {
        if (isEmpty(source) || isEmpty(candidates)) {
            return null;
        }
        for (E candidate : candidates) {
            if (source.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Find a single value of the given type in the given Collection.
     *
     * @param collection the Collection to search
     * @param type       the type to look for
     * @return a value of the given type found if there is a clear match,
     * or {@code null} if none or more than one such value found
     */
    @SuppressWarnings("unchecked")

    public static <T> T findValueOfType(Collection<?> collection, Class<T> type) {
        if (isEmpty(collection)) {
            return null;
        }
        T value = null;
        for (Object element : collection) {
            if (type == null || type.isInstance(element)) {
                if (value != null) {
                    // More than one value found... no clear single value.
                    return null;
                }
                value = (T) element;
            }
        }
        return value;
    }

    /**
     * Find a single value of one of the given types in the given Collection:
     * searching the Collection for a value of the first type, then
     * searching for a value of the second type, etc.
     *
     * @param collection the collection to search
     * @param types      the types to look for, in prioritized order
     * @return a value of one of the given types found if there is a clear match,
     * or {@code null} if none or more than one such value found
     */

    public static Object findValueOfType(Collection<?> collection, Class<?>[] types) {
        if (isEmpty(collection) || ObjectUtils.isEmpty(types)) {
            return null;
        }
        for (Class<?> type : types) {
            Object value = findValueOfType(collection, type);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Determine whether the given Collection only contains a single unique object.
     *
     * @param collection the Collection to check
     * @return {@code true} if the collection contains a single reference or
     * multiple references to the same instance, {@code false} otherwise
     */
    public static boolean hasUniqueObject(Collection<?> collection) {
        if (isEmpty(collection)) {
            return false;
        }
        boolean hasCandidate = false;
        Object candidate = null;
        for (Object elem : collection) {
            if (!hasCandidate) {
                hasCandidate = true;
                candidate = elem;
            } else if (candidate != elem) {
                return false;
            }
        }
        return true;
    }

    /**
     * Find the common element type of the given Collection, if any.
     *
     * @param collection the Collection to check
     * @return the common element type, or {@code null} if no clear
     * common type has been found (or the collection was empty)
     */

    public static Class<?> findCommonElementType(Collection<?> collection) {
        if (isEmpty(collection)) {
            return null;
        }
        Class<?> candidate = null;
        for (Object val : collection) {
            if (val != null) {
                if (candidate == null) {
                    candidate = val.getClass();
                } else if (candidate != val.getClass()) {
                    return null;
                }
            }
        }
        return candidate;
    }

    /**
     * Retrieve the first element of the given Set, using {@link SortedSet#first()}
     * or otherwise using the iterator.
     *
     * @param set the Set to check (may be {@code null} or empty)
     * @return the first element, or {@code null} if none
     * @see SortedSet
     * @see LinkedHashMap#keySet()
     * @see LinkedHashSet
     * @since 5.2.3
     */

    public static <T> T firstElement(Set<T> set) {
        if (isEmpty(set)) {
            return null;
        }
        if (set instanceof SortedSet) {
            return ((SortedSet<T>) set).first();
        }

        Iterator<T> it = set.iterator();
        T first = null;
        if (it.hasNext()) {
            first = it.next();
        }
        return first;
    }

    /**
     * Retrieve the first element of the given List, accessing the zero index.
     *
     * @param list the List to check (may be {@code null} or empty)
     * @return the first element, or {@code null} if none
     * @since 5.2.3
     */

    public static <T> T firstElement(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * Retrieve the last element of the given Set, using {@link SortedSet#last()}
     * or otherwise iterating over all elements (assuming a linked set).
     *
     * @param set the Set to check (may be {@code null} or empty)
     * @return the last element, or {@code null} if none
     * @see SortedSet
     * @see LinkedHashMap#keySet()
     * @see LinkedHashSet
     * @since 5.0.3
     */

    public static <T> T lastElement(Set<T> set) {
        if (isEmpty(set)) {
            return null;
        }
        if (set instanceof SortedSet) {
            return ((SortedSet<T>) set).last();
        }

        // Full iteration necessary...
        Iterator<T> it = set.iterator();
        T last = null;
        while (it.hasNext()) {
            last = it.next();
        }
        return last;
    }

    /**
     * Retrieve the last element of the given List, accessing the highest index.
     *
     * @param list the List to check (may be {@code null} or empty)
     * @return the last element, or {@code null} if none
     * @since 5.0.3
     */

    public static <T> T lastElement(List<T> list) {
        if (isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }

    /**
     * Marshal the elements from the given enumeration into an array of the given type.
     * Enumeration elements must be assignable to the type of the given array. The array
     * returned will be a different instance than the array given.
     */
    public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
        ArrayList<A> elements = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            elements.add(enumeration.nextElement());
        }
        return elements.toArray(array);
    }

    /**
     * Adapt an {@link Enumeration} to an {@link Iterator}.
     *
     * @param enumeration the original {@code Enumeration}
     * @return the adapted {@code Iterator}
     */
    public static <E> Iterator<E> toIterator(Enumeration<E> enumeration) {
        return (enumeration != null ? new EnumerationIterator<>(enumeration) : Collections.emptyIterator());
    }

    /**
     * Iterator wrapping an Enumeration.
     */
    private static class EnumerationIterator<E> implements Iterator<E> {

        private final Enumeration<E> enumeration;

        public EnumerationIterator(Enumeration<E> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasNext() {
            return this.enumeration.hasMoreElements();
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return this.enumeration.nextElement();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    /**
     * 判断集合是否为空
     *
     * @param c 待判断集合
     * @return 判断结果
     */
    public static boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /**
     * 判断集合是否为空
     *
     * @param map 待判断集合
     * @return 判断结果
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 判断集合是否为空
     *
     * @param objects 待判断集合
     * @return 判断结果
     */
    public static boolean isNullOrEmpty(Object[] objects) {
        return objects == null || objects.length == 0;
    }

    /**
     * 判断数组是否包含空值，对于字符串数组，空串也当作空值
     *
     * @param objects 待判断集合
     * @return 判断结果
     */
    public static boolean isContainNullOrEmpty(Object[] objects) {
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
            if (object instanceof String && StringUtils.isEmpty((String) object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 唯一化一个数组
     *
     * @param array 待处理数组
     * @param <E>
     * @return 去重后的数组集合
     */
    public static <E> Set<E> unique(E[] array) {
        if (array == null) {
            return null;
        }
        Set<E> set = new HashSet<>();
        Collections.addAll(set, array);
        return set;
    }

    /**
     * child是否是father的子集
     *
     * @param father 父集
     * @param child  子集
     * @param <E>
     * @return 判断结果
     */
    public static <E> boolean isSubSet(Set<E> father, Set<E> child) {
        Assert.notNull(father, "The parameter father is null.");
        Assert.notNull(child, "The parameter child is null.");
        for (E element : child) {
            if (!father.contains(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取两个集合的交集
     *
     * @param set1 待处理集合1
     * @param set2 待处理集合2
     * @param <E>
     * @return 交集
     */
    public static <E> Set<E> intersect(Set<E> set1, Set<E> set2) {
        Assert.notNull(set1, "The parameter set1 is null.");
        Assert.notNull(set2, "The parameter set2 is null.");
        Set<E> inter = new HashSet<>(set1);
        inter.retainAll(set2);
        return inter;
    }

    /**
     * 获取两个集合的并集
     *
     * @param set1 待处理集合1
     * @param set2 待处理集合2
     * @param <E>
     * @return 并集
     */
    public static <E> Set<E> union(Set<E> set1, Set<E> set2) {
        Assert.notNull(set1, "The parameter set1 is null.");
        Assert.notNull(set2, "The parameter set2 is null.");
        Set<E> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }

    /**
     * 求两个集合的差集，set1相对set2的差集
     *
     * @param set1 待处理集合1
     * @param set2 待处理集合2
     * @param <E>
     * @return 差集
     */
    public static <E> Set<E> minus(Set<E> set1, Set<E> set2) {
        Assert.notNull(set1, "The parameter set1 is null.");
        Assert.notNull(set2, "The parameter set2 is null.");
        Set<E> minus = new HashSet<>(set1);
        minus.removeAll(set2);
        return minus;
    }

    /**
     * 求两个集合的非公共元素构成的集合
     *
     * @param set1 待处理集合1
     * @param set2 待处理集合2
     * @param <E>
     * @return 去交集后元素集合
     */
    public static <E> Set<E> complement(Set<E> set1, Set<E> set2) {
        Assert.notNull(set1, "The parameter set1 is null.");
        Assert.notNull(set2, "The parameter set2 is null.");
        Set<E> union = union(set1, set2);
        Set<E> intersect = intersect(set1, set2);
        return minus(union, intersect);
    }

    /**
     * 判断两个集合是否相同
     *
     * @param set1 待处理集合1
     * @param set2 待处理集合2
     * @param <E>
     * @return 两个集合是否相同
     */
    public static <E> boolean isSetEqual(Set<E> set1, Set<E> set2) {
        if (set1 == set2) {
            return true;
        }
        if (set1 == null || set2 == null) {
            return false;
        }
        return set1.equals(set2);
    }

    /**
     * 判断两个集合是否不相同
     *
     * @param set1 待处理集合1
     * @param set2 待处理集合2
     * @param <E>
     * @return 两个集合是否相同
     */
    public static <E> boolean isSetNotEqual(Set<E> set1, Set<E> set2) {
        return !isSetEqual(set1, set2);
    }

    /**
     * 将一个集合按照指定量分为多个批次
     *
     * @param list      集合
     * @param batchSize 批量大小
     * @param <E>
     * @return 按批量分配后的多个批次
     */
    public static <E> List<List<E>> batchSplit(List<E> list, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("batchSize must be positive: " + batchSize);
        }
        if (isEmpty(list)) {
            return Collections.emptyList();
        }
        // 预估批次数量
        int size = list.size();
        List<List<E>> result = new ArrayList<>((size + batchSize - 1) / batchSize);
        for (int i = 0; i < size; i += batchSize) {
            int end = Math.min(size, i + batchSize);
            result.add(list.subList(i, end));
        }
        return result;
    }

    /**
     * 将一个集合按照指定量分为多个批次
     *
     * @param list      集合
     * @param batchSize 批量大小
     * @param <E>
     * @return 按批量分配后的多个批次
     */
    public static <E> List<List<E>> batchSplitByStream(List<E> list, int batchSize) {
        return new ArrayList<>(list.stream()
                .collect(Collectors.groupingBy(
                        i -> (list.indexOf(i) / batchSize + 1)))
                .values());
    }

    /**
     * 将一个集合按照指定量分为多个批次
     *
     * @param list      集合
     * @param batchSize 批量大小
     * @param <E>
     * @return 按批量分配后的多个批次
     */
    public static <E> List<List<E>> batchSplitByGuava(List<E> list, int batchSize) {
        return Lists.partition(list, batchSize);
    }

    /**
     * 获取合适的MAP初始容量
     * 初始容量指的是HashMap在创建时底层数组的大小。
     * 当元素数量超出容量与负载因子（默认是 0.75）的乘积时，HashMap就会进行扩容操作，
     * 也就是重新哈希，这会明显降低性能。
     *
     * @param size map中元素数量
     * @return 对应的map初始容量
     */
    public static int initialMapCapacity(int size) {
        return (int) (size / 0.75f + 1);
    }

    /**
     * 通用字符串转List方法
     *
     * @param str       逗号分隔的字符串
     * @param converter 类型转换函数（如：Integer::valueOf、Double::valueOf）
     * @param <E>       目标类型（Integer/Long/Double等）
     * @return 转换后的List
     */
    public static <E> List<E> convertStringToList(String str, Function<String, E> converter) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptyList();
        }
        return doConvertStringToCollection(str, converter, ArrayList::new);
    }

    /**
     * 通用字符串转Set方法
     *
     * @param str       逗号分隔的字符串
     * @param converter 类型转换函数（如：Integer::valueOf、Double::valueOf）
     * @param <E>       目标类型（Integer/Long/Double等）
     * @return 转换后的Set
     */
    public static <E> Set<E> convertStringToSet(String str, Function<String, E> converter) {
        if (StringUtils.isBlank(str)) {
            return Collections.emptySet();
        }
        return doConvertStringToCollection(str, converter, Sets::newHashSetWithExpectedSize);
    }

    /**
     * 字符串转集合的公共实现
     *
     * @param str               逗号分隔的字符串（已确保非空）
     * @param converter         类型转换函数
     * @param collectionFactory 指定初始容量的集合工厂（ArrayList::new / Sets::newHashSetWithExpectedSize）
     * @param <E>               目标元素类型
     * @param <C>               集合类型
     * @return 转换后的集合
     */
    private static <E, C extends Collection<E>> C doConvertStringToCollection(
            String str, Function<String, E> converter, IntFunction<C> collectionFactory) {
        String[] parts = str.split(CommonConstants.COMMA);
        C result = collectionFactory.apply(parts.length);
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            E element = converter.apply(trimmed);
            if (element != null) {
                result.add(element);
            }
        }
        return result;
    }

    /**
     * 合并 Map 的所有 Collection value 到一个 Set 中，自动去重。
     *
     * @param map 源 Map，value 为 Collection；可为 null 或空
     * @param <K> Map key 类型
     * @param <V> Collection 元素类型
     * @return 合并后的 Set，永远不为 null
     */
    public static <K, V> Set<V> mergeValuesToSet(Map<K, ? extends Collection<V>> map) {
        if (isEmpty(map)) {
            return Collections.emptySet();
        }
        // 预估容量：所有 collection size 之和，避免多次扩容
        int expected = 0;
        for (Collection<V> c : map.values()) {
            if (c != null) {
                expected += c.size();
            }
        }
        Set<V> result = Sets.newHashSetWithExpectedSize(expected);
        for (Collection<V> c : map.values()) {
            if (isNotEmpty(c)) {
                result.addAll(c);
            }
        }
        return result;
    }

    /**
     * 将 List 按 keyExtractor 提取的 key 转换为 Map。
     * 自动跳过 null 元素和 null key；重复 key 保留第一个。
     *
     * @param list         源列表，可为 null 或空
     * @param keyExtractor key 提取函数，如：Integer::valueOf
     * @param <K>          key 类型
     * @param <V>          元素类型
     * @return 转换后的 Map，永远不为 null
     */
    public static <K, V> Map<K, V> toMap(List<V> list, Function<V, K> keyExtractor) {
        if (isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<K, V> map = Maps.newHashMapWithExpectedSize(list.size());
        for (V item : list) {
            if (item == null) {
                continue;
            }
            K key = keyExtractor.apply(item);
            if (key == null) {
                continue;
            }
            map.putIfAbsent(key, item);
        }
        return map;
    }

    /**
     * 将 List 按 keyExtractor 分组，返回 Map<key, List<元素>>。
     * 自动跳过 null 元素和 null key；返回可变 HashMap，永远不为 null。
     *
     * @param list         源列表，可为 null 或空
     * @param keyExtractor 分组 key 提取函数，如：Integer::valueOf
     * @param <K>          key 类型
     * @param <V>          元素类型
     * @return 分组后的 Map，永远不为 null
     */
    public static <K, V> Map<K, List<V>> groupBy(List<V> list, Function<V, K> keyExtractor) {
        if (isEmpty(list)) {
            return Collections.emptyMap();
        }
        Map<K, List<V>> map = Maps.newHashMapWithExpectedSize(list.size());
        for (V item : list) {
            if (item == null) {
                continue;
            }
            K key = keyExtractor.apply(item);
            if (key == null) {
                continue;
            }
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }
        return map;
    }

    /**
     * 从 List 中提取每个元素的指定字段到 Set 中，自动去重。
     * 自动跳过 null 元素和 null 字段值；返回可变 HashSet，永远不为 null。
     *
     * @param list           源列表，可为 null 或空
     * @param fieldExtractor 字段提取函数，如：Integer::valueOf
     * @param <V>            元素类型
     * @param <F>            字段类型
     * @return 提取后的 Set，永远不为 null
     */
    public static <V, F> Set<F> extractFieldToSet(List<V> list, Function<V, F> fieldExtractor) {
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptySet();
        }
        Set<F> result = Sets.newHashSetWithExpectedSize(list.size());
        for (V item : list) {
            if (item == null) {
                continue;
            }
            F field = fieldExtractor.apply(item);
            if (field == null) {
                continue;
            }
            result.add(field);
        }
        return result;
    }

    /**
     * 将集合元素通过映射函数转换后收集到 Set。
     * <p>自动跳过 null 元素，返回可变 HashSet，永不为 null。
     *
     * @param source 源集合，可为 null
     * @param mapper 元素映射函数，不可为 null
     * @return 可变 HashSet，永不为 null
     */
    public static <V, R> Set<R> convertToSet(Collection<V> source, Function<V, R> mapper) {
        if (isEmpty(source)) {
            return Collections.emptySet();
        }
        Set<R> result = Sets.newHashSetWithExpectedSize(source.size());
        for (V item : source) {
            if (item != null) {
                result.add(mapper.apply(item));
            }
        }
        return result;
    }

    /**
     * 将集合元素通过映射函数转换后收集到 List。
     * <p>自动跳过 null 元素，返回可变 ArrayList，永不为 null。
     *
     * @param source 源集合，可为 null
     * @param mapper 元素映射函数，不可为 null
     * @return 可变 ArrayList，永不为 null
     */
    public static <V, R> List<R> convertToList(Collection<V> source, Function<V, R> mapper) {
        if (isEmpty(source)) {
            return Collections.emptyList();
        }
        List<R> result = new ArrayList<>(source.size());
        for (V item : source) {
            if (item != null) {
                result.add(mapper.apply(item));
            }
        }
        return result;
    }

}
