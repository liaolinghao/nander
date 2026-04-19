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
package wang.bigbird.domain.framework.common.logging.core.support.aop;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import wang.bigbird.domain.framework.common.logging.core.base.enums.LogLevelEnum;
import wang.bigbird.domain.framework.common.logging.core.domain.pojo.ExcludeWrapper;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 日志打印 Aop
 *
 * @author Bigbird
 */
public abstract class BaseLoggingAop {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseLoggingAop.class);
    /**
     * 日志格式
     */
    private static final String LOG_MODEL = "{}--{}--{}--{}--{}";

    /**
     * pointcut 指定切入点的生效时机
     *
     * @annotation 当执行方法时拥有指定注解生效
     * @target 当代理目标拥有指定注解时生效
     */
    public abstract void pointcut();


    /**
     * 日志处理
     *
     * @param joinPoint 注入点对象
     * @return 注入点执行对象
     * @throws Throwable
     */
    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        String classSimpleName;
        Method method;
        String methodName;
        try {
            classSimpleName = getClassSimpleName(joinPoint);
            // 得到其方法签名
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            method = signature.getMethod();
            methodName = method.getName();
        } catch (Exception e) {
            return joinPoint.proceed();
        }
        if (StringUtils.isBlank(classSimpleName)) {
            classSimpleName = CommonConstants.EMPTY;
        }
        if (StringUtils.isBlank(methodName)) {
            methodName = CommonConstants.EMPTY;
        }
        // 是否排除
        if (exclude(classSimpleName, methodName)) {
            return joinPoint.proceed();
        }
        long start = System.currentTimeMillis();
        Object proceed = null;
        try {
            proceed = joinPoint.proceed();
            return proceed;
        } finally {
            long end = System.currentTimeMillis();
            try {
                // 构建日志
                // 日志类型
                String loggingType = getLoggingType();
                // 级别
                String level = getLoggingLevel();
                // 获取方法参数值数组
                Object[] args = joinPoint.getArgs();
                // 获取方法 参数名:参数值,参数名:参数值,... 字符串
                String paramAndValueString = methodParamHandle(method, args);
                // 返回值
                String resultJson = resultHandle(proceed);
                // 类和方法名
                String classAndMethod = classSimpleName + CommonConstants.DOT + methodName;
                // 时长（毫秒）
                long delta = end - start;
                logging(LOGGER, level, LOG_MODEL, loggingType, classAndMethod, paramAndValueString, resultJson, delta);
            } catch (Exception e) {
                LOGGER.error("logging print error.", e);
            }
        }
    }


    /**
     * 获取类的名称
     *
     * @param joinPoint 注入点对象
     * @return 类的名称
     * @throws Exception
     */
    public abstract String getClassSimpleName(ProceedingJoinPoint joinPoint) throws Exception;


    /**
     * 获取日志类型
     *
     * @return 日志类型
     */
    public abstract String getLoggingType();


    /**
     * 获取日志级别
     *
     * @return 日志级别
     */
    public abstract String getLoggingLevel();


    /**
     * 获取排除的类或方法
     *
     * @return 排除的类或方法
     */
    public abstract ExcludeWrapper getExclude();

    /**
     * 获取序列化长度
     *
     * @return 序列化长度
     */
    public abstract Integer getSerializeLength();

    /**
     * 获取当前的序列化工具
     *
     * @return ObjectMapper
     */
    public abstract ObjectMapper getObjectMapper();

    /**
     * 判断对象能否进行序列化
     *
     * @param o 判断对象
     * @return 能否进行序列化
     */
    protected abstract boolean canSerialize(Object o);

    /**
     * 记录日志
     *
     * @param logger   Logger
     * @param level    级别
     * @param msgModel 消息模板
     * @param objects  对象数组
     */
    private void logging(Logger logger, String level, String msgModel, Object... objects) {
        if (LogLevelEnum.TRACE.getCode().equals(level) && logger.isTraceEnabled()) {
            logger.trace(msgModel, objects);
        } else if (LogLevelEnum.DEBUG.getCode().equals(level) && logger.isDebugEnabled()) {
            logger.debug(msgModel, objects);
        } else if (LogLevelEnum.INFO.getCode().equals(level) && logger.isInfoEnabled()) {
            logger.info(msgModel, objects);
        } else if (LogLevelEnum.WARN.getCode().equals(level) && logger.isWarnEnabled()) {
            logger.warn(msgModel, objects);
        } else if (LogLevelEnum.ERROR.getCode().equals(level) && logger.isErrorEnabled()) {
            logger.error(msgModel, objects);
        }
    }

    /**
     * 是否排除
     *
     * @return 是否排除
     */
    private boolean exclude(String className, String methodName) {
        ExcludeWrapper excludeWrapper = getExclude();
        if (null == excludeWrapper) {
            return false;
        }
        Set<String> classNames = excludeWrapper.getClassNames();
        Set<String> methodNames = excludeWrapper.getMethodNames();
        if (classNames.contains(className)) {
            return true;
        }
        String method = className + CommonConstants.DOT + methodName;
        return methodNames.contains(method);
    }


    /**
     * 方法参数处理
     *
     * @return 参数字符串
     */
    private String methodParamHandle(Method method, Object[] args) {
        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
        List<String> paramAndValues = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (!canSerialize(arg)) {
                continue;
            }
            String serialize = serialize(arg);
            if (null == serialize) {
                serialize = CommonConstants.NULL;
            }
            String paramAndValue;
            if (null != parameterNames) {
                String parameterName = parameterNames[i];
                paramAndValue = parameterName + CommonConstants.COLON + serialize;
            } else {
                paramAndValue = serialize;
            }
            paramAndValues.add(paramAndValue);
        }
        String paramAndValueString;
        if (CollectionUtil.isEmpty(paramAndValues)) {
            paramAndValueString = CommonConstants.NULL;
        } else {
            paramAndValueString = String.join(CommonConstants.COMMA, paramAndValues);
        }
        return sliceStringBySerializeLength(paramAndValueString);
    }


    /**
     * 返回结果处理
     *
     * @return 返回结果字符串
     */
    private String resultHandle(Object proceed) {
        String resultJson = CommonConstants.NULL;
        if (canSerialize(proceed)) {
            String json = serialize(proceed);
            if (null != json) {
                resultJson = json;
            }
        }
        return sliceStringBySerializeLength(resultJson);
    }


    /**
     * 截断字符串
     *
     * @return 返回结果字符串
     */
    private String sliceStringBySerializeLength(String s) {
        Integer serializeLength = getSerializeLength();
        if (null != serializeLength && serializeLength > 0) {
            return StringUtils.substring(s, 0, serializeLength);
        }
        return s;
    }


    /**
     * 序列化
     */
    private String serialize(Object o) {
        if (null == o) {
            return null;
        }
        try {
            return getObjectMapper().writeValueAsString(o);
        } catch (Exception e) {
            // 捕获更大范围的异常
            // 日志打印工具尽量不打印额外内部错误日志
            return null;
        }
    }

}
