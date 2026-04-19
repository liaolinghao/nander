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
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.interceptor;

import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.core.base.util.ObjectUtils;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.config.property.MybatisPlusProperties;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.annotation.SecurityField;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * 处理查询操作的解密拦截器
 * 注册成为Spring容器的Bean，拦截器就能生效
 *
 * @author Bigbird
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
@Component
public class DecryptInterceptor implements Interceptor {

    @Autowired
    private MybatisPlusProperties mybatisPlusProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 执行查询操作，获取结果
        Object result = invocation.proceed();
        if (result instanceof List<?>) {
            List<?> list = (List<?>) result;
            for (Object obj : list) {
                decryptObject(obj);
            }
        } else if (result != null) {
            decryptObject(result);
        }
        return result;
    }

    @Override
    public Object plugin(Object target) {
        // 只拦截ResultSetHandler类型的目标对象
        if (target instanceof ResultSetHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以通过properties配置一些参数
    }

    /**
     * 对对象中标记了SecurityField注解的字段进行解密
     */
    private void decryptObject(Object obj) throws IllegalAccessException, UnsupportedEncodingException {
        if (obj == null) {
            return;
        }
        Class<?> clazz = obj.getClass();
        if (ObjectUtils.isBasicType(clazz)) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(SecurityField.class) && field.getType() == String.class) {
                field.setAccessible(true);
                String value = (String) field.get(obj);
                field.set(obj, CryptUtils.decrypt(value, mybatisPlusProperties.getKey()));
            }
        }
    }

}
