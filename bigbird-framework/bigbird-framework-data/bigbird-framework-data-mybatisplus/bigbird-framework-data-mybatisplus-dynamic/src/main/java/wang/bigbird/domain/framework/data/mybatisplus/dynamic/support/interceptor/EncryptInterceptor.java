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

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.core.base.util.ObjectUtils;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.config.property.MybatisPlusProperties;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.annotation.SecurityField;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 处理插入和更新操作的加密拦截器
 * 注册成为Spring容器的Bean，拦截器就能生效
 *
 * @author Bigbird
 */
@Intercepts({
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})
})
@Component
public class EncryptInterceptor implements Interceptor {

    @Autowired
    private MybatisPlusProperties mybatisPlusProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
        // 获取参数对象
        Object parameterObject = parameterHandler.getParameterObject();
        // 对参数对象中的加密字段进行加密处理
        encryptParameter(parameterObject);
        // 执行原方法
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ParameterHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以通过properties配置一些参数
    }

    private void encryptParameter(Object parameter) throws UnsupportedEncodingException, IllegalAccessException {
        if (parameter instanceof Map) {
            Map<?, ?> paramMap = (Map<?, ?>) parameter;
            for (Object value : paramMap.values()) {
                encryptParameter(value);
            }
        } else if (parameter instanceof List) {
            List<?> list = (List<?>) parameter;
            for (Object value : list) {
                encryptObject(value);
            }
        } else {
            // 处理实体类参数
            encryptObject(parameter);
        }
    }

    /**
     * 对对象中标记了SecurityField注解的字段进行加密
     */
    private void encryptObject(Object obj) throws IllegalAccessException, UnsupportedEncodingException {
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
                if (CryptUtils.isEnc(value)) {
                    continue;
                }
                field.set(obj, CryptUtils.encrypt(value, mybatisPlusProperties.getKey()));
            }
        }
    }

}
