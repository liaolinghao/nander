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
package wang.bigbird.domain.framework.server.core.support.holder;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Spring BeanFactory持有类，可以通过该类获取Spring容器中的Bean
 * 【注意】
 * 该类是通过Spring启动后回调ApplicationContextAware接口获取Spring容器
 * <p>
 * 备注：如果使用该类的时机在回调该接口之前会收到一个{@link #assertApplicationContext()}异常
 *
 * @author Bigbird
 */
@Slf4j
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        log.info("SpringContextHolder initialize success.");
        SpringContextHolder.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        assertApplicationContext();
        return applicationContext;
    }

    /**
     * 通过实例类型获取实例
     *
     * @param requiredType 实例类型
     * @param <T>          T
     * @return T
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertApplicationContext();
        return applicationContext.getBean(requiredType);
    }

    /**
     * 通过实例名称获取实例
     *
     * @param beanName 实例名称
     * @param <T>      T
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        assertApplicationContext();
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 通过实例接口类型获取所有实现类实例
     *
     * @param tClass 接口类型
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getBeansWithType(Class<T> tClass) {
        assertApplicationContext();
        return applicationContext.getBeansOfType(tClass);
    }

    /**
     * 通过类上注解类型获取所有实现类实例
     *
     * @param annotation 注解类型
     * @return map
     */
    public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotation) {
        assertApplicationContext();
        return applicationContext.getBeansWithAnnotation(annotation);
    }

    /**
     * 读取配置值（支持 Nacos 配置）
     *
     * @param key 配置键
     * @return 配置值
     */
    public static String getConfigValue(String key) {
        assertApplicationContext();
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 读取配置值并转换类型
     *
     * @param key  配置键
     * @param type 数据类型
     * @param <T>
     * @return 配置数据对象
     */
    public static <T> T getConfigValue(String key, Class<T> type) {
        assertApplicationContext();
        return applicationContext.getEnvironment().getProperty(key, type);
    }

    /**
     * 根据配置前缀，批量读取一组配置转为Map
     *
     * @param prefix 配置前缀，例：encrypt.config
     * @return key=后缀, value=配置值
     */
    public static Map<String, String> getConfigMap(String prefix) {
        assertApplicationContext();
        Environment environment = applicationContext.getEnvironment();
        Map<String, String> resultMap = Maps.newHashMapWithExpectedSize(3);
        // Spring标准Binder绑定，自动提取前缀下所有属性
        Binder binder = Binder.get(environment);
        Map<String, String> bindMap = binder.bind(prefix, Map.class).orElse(Maps.newHashMapWithExpectedSize(0));
        resultMap.putAll(bindMap);
        return resultMap;
    }

    /**
     * 发布事件
     *
     * @param event event
     */
    public static void publishEvent(ApplicationEvent event) {
        assertApplicationContext();
        applicationContext.publishEvent(event);
    }

    private static void assertApplicationContext() {
        if (SpringContextHolder.applicationContext == null) {
            throw new NullPointerException("Application Context属性为null，请检查是否注入了SpringContextHolder");
        }
    }

}
