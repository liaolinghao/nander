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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.annotation;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;

import javax.cache.annotation.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * 本注解需要与下面两套注解搭配使用，以实现对被注解参数所在的方法进行批量的缓存操作。
 * <ol>
 * <li>Spring的缓存注解{@link Cacheable @Cacheable}、{@link CachePut @CachePut}、{@link CacheEvict @CacheEvict}</li>
 * <li>JSR-107的注解{@link CacheResult @CacheResult}、{@link CachePut JSR-107的@CachePut}、{@link CacheRemove @CacheRemove}、{@link CacheKey @CacheKey}</li>
 * </ol>
 *
 * <p><b>使用说明：</b></p>
 *
 * <p>
 * 假设已有获取单个对象的方法，如下：
 * <pre><code>
 *   public Foo getFoo(Integer fooId) {
 *     ...
 *   }
 * </code></pre>
 * 此时如果需要获取批量对象的方法，通常会是下面两种写法：
 * <pre><code>
 *   public Map<Integer, Foo> getMultiFoo(Collection<Integer> fooIds) {
 *     ...
 *   }
 *
 *   public List<Foo> getMultiFoo(List<Integer> fooIds) {
 *     ...
 *   }
 * </code></pre>
 * 获取批量对象的方法相对于获取单个对象的方法会有两点变化：
 * <ol>
 * <li>入参从单个对象（以下称【对象参数】）变为对象集合（以下称【对象集合参数】），例如：Integer 变为 Collection<Integer> 或 Set<Integer> 或 List<Integer>。</li>
 * <li>返回值从单个对象变为 Map<K,V> 或者 List<V>。例如：Map<Integer,Foo> 或 List<Foo>，若返回的是 List，那应与【对象集合参数】大小相同并顺序一致（v1.3以前）。</li>
 * </ol>
 * </p>
 *
 * <p>
 * <ol>
 * <li>
 * 在上面例子中，如果需要对获取单个对象的方法做缓存，会使用 {@link Cacheable @Cacheable} 或 {@link CacheResult @CacheResult} 注解：
 * （PS: 这里将 @CacheResult 和 @Cacheable 放在一起举例子，实际使用时通常只用其中的一个。）
 * <pre><code>
 *   @Cacheable(cacheNames = "foo")
 *   @CacheResult(cacheName = "foo")
 *   public Foo getFoo(Integer fooId) {
 *     // 用 fooId 生成缓存 key 和计算 condition、unless 条件，用 Foo 为缓存值
 *   }
 * </code></pre>
 * </li>
 * <li>
 * 如果对获取批量对象的方法直接加上 {@link Cacheable @Cacheable} 或 {@link CacheResult @CacheResult}，
 * 则会使用【对象集合参数】整体生成一个缓存 key，将返回的 Map 或 List 整体作为一个缓存值。
 * 但通常我们会希望它能变为多个 fooId 对应 Foo 的缓存，即：使用【对象集合参数】中每个【元素】和它对应的值分别作缓存。
 * 此时只需要在【对象集合参数】上加上 {@link CacheAsMulti @CacheAsMulti} 注解即可实现我们想要的缓存方式。
 * <pre><code>
 *   @Cacheable(cacheNames = "foo")
 *   @CacheResult(cacheName = "foo")
 *   public Map<Integer, Foo> getMultiFoo(@CacheAsMulti Collection<Integer> fooIds) {
 *     // 为 fooIds 集合中每个元素分别生成缓存 key 和计算 condition、unless 条件，用 Map 中对应的值作为缓存值
 *   }
 *
 *   @Cacheable(cacheNames = "foo")
 *   @CacheResult(cacheName = "foo")
 *   public List<Foo> getMultiFoo(@CacheAsMulti List<Integer> fooIds) {
 *     // 为 fooIds 集合中每个元素分别生成缓存 key 和计算 condition、unless 条件，用 List 中对应的值作为缓存值
 *     // 之后的例子中，返回 List 和 返回 Map 的处理方式都一样，就不再单独举例
 *   }
 * </code></pre>
 * </li>
 * </ol>
 * </p>
 *
 * <p>
 * 当方法有多个参数时，示例如下：
 * <ol>
 * <li>
 * 使用 {@link Cacheable} 时 {@link Cacheable#key()} 未配置【或】使用 {@link CacheResult} 时参数中没有 {@link CacheKey @CacheKey}
 * <pre><code>
 * @Cacheable(cacheNames = "foo", key="")
 * @CacheResult(cacheName = "foo")
 * public Foo getFoo(Integer fooId, String arg1) {
 *   // 用 fooId 和 arg1 两个参数生成缓存的 key，用返回值作为缓存值
 * }
 *
 * @Cacheable(cacheNames = "foo", key="")
 * @CacheResult(cacheName = "foo")
 * public Map<Integer, Foo> getMultiFoo(@CacheAsMulti Collection<Integer> fooIds, String arg1) {
 *   // 用 fooIds 中的每个【元素】分别和 arg1 参数生成缓存的 key，用返回 Map 中【元素】对应的值作为缓存值
 * }
 * </code></pre>
 * </li>
 * <li>
 * 使用 {@link Cacheable} 时 {@link Cacheable#key()} 只配置了【对象参数】的引用【或】使用 {@link CacheResult} 时只有【对象参数】上有 {@link CacheKey @CacheKey}
 * <pre><code>
 * @Cacheable(cacheNames = "foo", key="#fooId")
 * @CacheResult(cacheName = "foo")
 * public Foo getFoo(@CacheKey Integer fooId, String arg1) {
 *   // 用 fooId 生成缓存的 key，用返回值作为缓存值
 * }
 *
 * @Cacheable(cacheNames = "foo", key="#fooIds")
 * @CacheResult(cacheName = "foo")
 * public Map<Integer, Foo> getMultiFoo(@CacheAsMulti @CacheKey Collection<Integer> fooIds, String arg1) {
 *   // 用 fooIds 中的每个【元素】分别生成缓存的 key，用返回 Map 中【元素】对应的值作为缓存值
 * }
 * </code></pre>
 * </li>
 * </ol>
 * </p>
 *
 * <p>
 * <ol>
 * <li>
 * 使用 {@link Cacheable} 时 {@link Cacheable#key()} 配置了若干参数的引用【或】使用 {@link CacheResult} 时参数中有若干 {@link CacheKey @CacheKey}
 * <pre><code>
 * @Cacheable(cacheNames = "foo", key="#fooId+#arg1")
 * @CacheResult(cacheName = "foo")
 * public Foo getFoo(@CacheKey Integer fooId, @CacheKey String arg1,  Float arg2) {
 *   // 用 fooId 和 arg1 两个参数生成缓存的 key，用返回值作为缓存值
 * }
 *
 * @Cacheable(cacheNames = "foo", key="#fooIds+#arg1")
 * @CacheResult(cacheName = "foo")
 * public Map<Integer, Foo> getMultiFoo(@CacheAsMulti @CacheKey Collection<Integer> fooIds, @CacheKey String arg1,  Float arg2) {
 *   // 用 fooIds 中的每个【元素】分别和 arg1 参数生成缓存的 key，用返回 Map 中【元素】对应的值作为缓存值
 *   // 注意此时【对象集合参数】需要在 {@link Cacheable#key()} 中，需要有 @CacheKey 注解
 * }
 * </code></pre>
 * </li>
 * </ol>
 * </p>
 *
 * <p>
 * <b>与其他注解搭配使用时的说明：</b>
 * <ul>
 * <li>与 {@link CachePut Spring 的 CachePut} 搭配时，同样符合上面的例子。</li>
 * <li>与 {@link CacheEvict} 搭配时，若注解的 key 参数中没有 #result，对【方法】返回类型无要求；若 key 中有 #result，【方法】返回类型需要是 Map 或 List。</li>
 * <li>与 Spring 的 `@CachePut`、`@CacheEvict` 搭配时，若 key 参数中有 `#result`， 则可以没有【对象集合参数】的引用。</li>
 * <li>与 {@link CacheRemove} 搭配时，对【方法】返回类型无要求。</li>
 * <li>与 {@link CachePut JSR-107 的 CachePut} 搭配时，对【方法】返回类型无要求，可参照下面的示例：
 * <ul>
 * <li>单个参数做key，未配置 {@link CacheKey @CacheKey}：
 * <pre><code>
 * @CachePut(cacheName = "foo")
 * public void putFoo(Integer fooId, @CacheValue String value) {
 *   // 用 fooId 参数生成缓存的 key，用 value 作为缓存值
 * }
 *
 * @CachePut(cacheName = "foo")
 * public void putMultiFoo(@CacheAsMulti @CacheValue Map<Integer, String> fooIdValueMap) {
 *   // 此时方法的 @CacheValue 参数必须为 Map 类型
 *   // 用 fooIdValueMap 中的每个 Entry 的 key 分别生成缓存的 key，用 Entry 的 value 作为缓存值
 * }
 * </code></pre>
 * </li>
 * <li>多个参数做key，未配置 {@link CacheKey @CacheKey}：
 * <pre><code>
 * @CachePut(cacheName = "foo")
 * public void putFoo(Integer fooId, String arg1, @CacheValue String value) {
 *   // 用 fooId 和 arg1 两个参数生成缓存的 key，用 value 作为缓存值
 * }
 *
 * @CachePut(cacheName = "foo")
 * public void putMultiFoo(@CacheAsMulti @CacheValue Map<Integer, String> fooIdValueMap, String arg1) {
 *   // 此时方法的 @CacheValue 参数必须为 Map 类型
 *   // 用 fooIdValueMap 中的每个 Entry 的 key 分别和 arg1 参数生成缓存的 key，用 Entry 的 value 作为缓存值
 * }
 * </code></pre>
 * </li>
 * <li>只有【对象参数】上有 {@link CacheKey @CacheKey}：
 * <pre><code>
 * @CachePut(cacheName = "foo")
 * public void putFoo(@CacheKey Integer fooId, String arg1, @CacheValue String value) {
 *   // 用 fooId 参数生成缓存的 key，用 value 作为缓存值
 * }
 *
 * @CachePut(cacheName = "foo")
 * public void putMultiFoo(@CacheAsMulti @CacheKey @CacheValue Map<Integer, String> fooIdValueMap, String arg1) {
 *   // 此时方法的 @CacheValue 参数必须为 Map 类型
 *   // 用 fooIdValueMap 中的每个 Entry 的 key 分别生成缓存的 key，用 Entry 的 value 作为缓存值
 * }
 * </code></pre>
 * </li>
 * <li>若干参数上有 {@link CacheKey @CacheKey}：
 * <pre><code>
 * @CachePut(cacheName = "foo")
 * public void putFoo(@CacheKey Integer fooId, @CacheKey String arg1, String arg2, @CacheValue String value) {
 *   // 用 fooId 和 arg1 两个参数生成缓存的 key，用 value 作为缓存值
 * }
 *
 * @CachePut(cacheName = "foo")
 * public void putMultiFoo(@CacheAsMulti @CacheKey @CacheValue Map<Integer, String> fooIdValueMap, @CacheKey String arg1, String arg2) {
 *   // 此时方法的 @CacheValue 参数必须为 Map 类型
 *   // 用 fooIdValueMap 中的每个 Entry 的 key 分别和 arg1 参数生成缓存的 key，用 Entry 的 value 作为缓存值
 * }
 * </code></pre>
 * </li>
 * </ul>
 *
 * </li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>总结和补充：</b>
 * <ol>
 *     <li>@CacheAsMulti 注解不能替代 Spring 缓存注解中的 key 参数，例如：{@link Cacheable#key()}，也不能替代 {@link CacheKey @CacheKey}、{@link CacheValue @CacheValue} 注解。</li>
 *     <li>如果使用自定义的 {@link KeyGenerator}，则会用【对象集合参数】的每个【元素】和其他参数组成 Object[] 传入 {@link KeyGenerator#generate(Object, Method, Object...)} 计算缓存 key；自定义的 {@link CacheKeyGenerator CacheKeyGenerator} 也一样。</li>
 *     <li>与 @Cacheable、@CacheResult、@CachePut 注解搭配使用时，若 {@link CacheAsMulti#strictNull()} 为 true 且方法的返回类型是 Map，【元素】在 Map 中对应的值为 null 就会缓存 null，【元素】在 Map 中不存在就不缓存。</li>
 *     <li>与 {@link CachePut} 和 {@link CacheEvict} 搭配，注解的 key 参数配置了 #result 时，若方法的返回类型是 Map，对于 Map 中不存在的【元素】会使用 null 作为缺省值来计算缓存 key 和 condition、unless 条件。</li>
 *     <li>{@link Cacheable#condition()}、{@link Cacheable#unless()} 等条件表达式是用【对象集合参数】中的每个【元素】分别计算，只将不符合的【元素】排除，而不是整个集合。</li>
 * </ol>
 * </p>
 *
 * @author Bigbird
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheAsMulti {

    /**
     * 严格的缓存数据为 null 的策略
     * 为 false 时，返回值 map 中未包含的参数项都会被缓存为 null；为 ture 时，返回值 map 中包含的为 null 的参数项才会被缓存为 null
     * 例如：参数传入 List[1,2,3]，返回 Map{1:"A",3:"C"}。当为 false 时，2 会被缓存为 null；当为 true 时，2 不会被缓存。
     *
     * @return 默认为 false，如果方法返回值Map里会包含为 null 的元素，请将此值设置为 true
     */
    boolean strictNull() default false;

    /**
     * 当方法返回的是 List 时，如果顺序无法保证，可以以此来指定【对象集合参数】的元素对应返回 List 的元素的字段名（使用 . 取下层），此时 List 的大小不必与【对象集合参数】相同。
     * 例如：返回List<Foo>，此处可以是：Foo的字段名.下级字段名
     *
     * @return 字段名
     */
    String asElementField() default "";

}


