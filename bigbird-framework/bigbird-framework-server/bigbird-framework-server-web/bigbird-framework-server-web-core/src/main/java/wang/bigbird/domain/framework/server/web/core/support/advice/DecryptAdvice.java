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
package wang.bigbird.domain.framework.server.web.core.support.advice;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import wang.bigbird.domain.framework.core.base.util.ObjectUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.server.web.core.config.property.WebProperties;
import wang.bigbird.domain.framework.server.web.core.support.annotation.Decrypt;
import wang.bigbird.domain.framework.server.web.core.support.annotation.DecryptField;
import wang.bigbird.domain.framework.server.web.core.support.handler.SecurityHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

/**
 * 用于对controller层为方法参数标注了@RequestBody类型的参数进行解密增强处理。
 * <p>
 *
 * @author Bigbird
 */
@Slf4j
@ControllerAdvice
public class DecryptAdvice extends RequestBodyAdviceAdapter {

    @Autowired
    private WebProperties webProperties;
    @Autowired
    private SecurityHandler securityHandler;

    @Override
    public boolean supports(MethodParameter methodParameter, Type type, Class<? extends HttpMessageConverter<?>> aClass) {
        Decrypt decrypt = methodParameter.getMethod().getAnnotation(Decrypt.class);
        return decrypt != null;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        Decrypt decrypt = methodParameter.getMethod().getAnnotation(Decrypt.class);
        String str = StreamUtils.copyToString(inputMessage.getBody(), Charset.defaultCharset());
        return new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                String decryptStr = "";
                if (StringUtils.isNotEmpty(str)) {
                    if (webProperties.getEnableDecrypt()) {
                        if (decrypt.all()) {
                            decryptStr = securityHandler.decrypt(str);
                        } else {
                            JSONObject jsonObject = new JSONObject(str);
                            // 对@RequestBody中的加密字段自动解密
                            decryptFields(jsonObject, methodParameter.getParameterType());
                            decryptStr = jsonObject.toString();
                        }
                    } else {
                        decryptStr = str;
                    }
                }
                ByteArrayInputStream inputStream = new ByteArrayInputStream(decryptStr.getBytes(Charset.defaultCharset()));
                inputMessage.getHeaders().setContentLength(inputStream.available());
                return inputStream;
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }

            private void decryptFields(JSONObject jsonObject, Class c) {
                Field[] fields = c.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(DecryptField.class)) {
                        String name = field.getName();
                        Class fieldType = field.getType();
                        if (ObjectUtils.isBasicType(fieldType)) {
                            String encryptedValue = jsonObject.getStr(name);
                            if (StringUtils.isEmpty(encryptedValue)) {
                                continue;
                            }
                            String decryptedValue = securityHandler.decrypt(encryptedValue);
                            jsonObject.put(name, decryptedValue);
                        } else if (Collection.class.isAssignableFrom(fieldType)) {
                            // 集合类型
                            JSONArray jsonArray = jsonObject.getJSONArray(name);
                            if (jsonArray == null || jsonArray.isEmpty()) {
                                continue;
                            }
                            Class collectionGenericType = ObjectUtils.getCollectionGenericType(field);
                            if (collectionGenericType == null) {
                                continue;
                            }
                            boolean isBasicType = ObjectUtils.isBasicType(collectionGenericType);
                            for (int i = jsonArray.size() - 1; i >= 0; i--) {
                                if (isBasicType) {
                                    String encryptedValue = jsonArray.getStr(i);
                                    if (StringUtils.isEmpty(encryptedValue)) {
                                        continue;
                                    }
                                    String decryptedValue = securityHandler.decrypt(encryptedValue);
                                    jsonArray.set(i, decryptedValue);
                                } else {
                                    decryptFields(jsonArray.getJSONObject(i), collectionGenericType);
                                }
                            }
                        } else if (Map.class.isAssignableFrom(fieldType)) {
                            // MAP类型
                            JSONObject jo = jsonObject.getJSONObject(name);
                            if (jo == null || jo.isEmpty()) {
                                continue;
                            }
                            Class mapValueGenericType = ObjectUtils.getMapValueGenericType(field);
                            if (mapValueGenericType == null) {
                                continue;
                            }
                            boolean isBasicType = ObjectUtils.isBasicType(mapValueGenericType);
                            for (String key : jo.keySet()) {
                                if (isBasicType) {
                                    String encryptedValue = jo.getStr(key);
                                    if (StringUtils.isEmpty(encryptedValue)) {
                                        continue;
                                    }
                                    String decryptedValue = securityHandler.decrypt(encryptedValue);
                                    jo.put(key, decryptedValue);
                                } else {
                                    decryptFields(jo.getJSONObject(key), mapValueGenericType);
                                }
                            }
                        } else {
                            // 其他类类型
                            decryptFields(jsonObject.getJSONObject(name), fieldType);
                        }
                    }
                }
            }

        };

    }

}
