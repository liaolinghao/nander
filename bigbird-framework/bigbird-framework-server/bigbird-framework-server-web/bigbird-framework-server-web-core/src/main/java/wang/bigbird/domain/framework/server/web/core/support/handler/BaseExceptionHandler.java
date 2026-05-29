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
package wang.bigbird.domain.framework.server.web.core.support.handler;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.core.exception.InvalidArgumentException;
import wang.bigbird.domain.framework.server.core.exception.BaseBusinessException;
import wang.bigbird.domain.framework.server.core.exception.BusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.core.support.response.RespResult;
import wang.bigbird.domain.framework.server.web.core.domain.vo.ValidExceptionDetailVO;

import javax.validation.ConstraintViolationException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 异常统一处理，该异常处理返回值必须为Void
 * 即异常信息必须封装到msg中返回，不能放到data中，否则会干扰外部接口调用时的异常返回
 *
 * @author Bigbird
 * @ControllerAdvice给Controller控制器添加统一的操作或处理
 * @ExceptionHandler捕获异常 以上两个注解组合完成统一异常处理，当存在多个@RestControllerAdvice时，
 * 配置@Order()用于设置自身服务的拦截顺序为最低优先级
 */
@Slf4j
@RestControllerAdvice
@Order()
public class BaseExceptionHandler implements ApplicationContextAware {

    /**
     * 自定义异常是否捕获处理
     */
    protected boolean baseBusinessExceptionHandleEnable;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // 解决 conditional 注入属性问题
        String baseBusinessExceptionHandleEnableString = applicationContext.getEnvironment()
                .getProperty("bigbird.server.web.core.base-business-exception-handle.enable");
        if (StringUtils.isBlank(baseBusinessExceptionHandleEnableString)) {
            baseBusinessExceptionHandleEnable = true;
        } else {
            baseBusinessExceptionHandleEnable = CommonConstants.StringBoolean.TRUE.equals(baseBusinessExceptionHandleEnableString);
        }
    }


    /**
     * spring自动绑定的参数校验异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public RespResult<Void> handleBindException(BindException e) {
        Map<String, List<String>> fieldNameMsgMap = new LinkedHashMap<>();
        String defaultMsg = e.getBindingResult().getAllErrors()
                .stream()
                .map(o -> {
                    String fieldName;
                    if (o instanceof FieldError) {
                        fieldName = ((FieldError) o).getField();
                    } else {
                        fieldName = "";
                        String[] codes = o.getCodes();
                        if (null != codes && codes.length != 0) {
                            fieldName = codes[0];
                            int i = fieldName.lastIndexOf('.');
                            if (i > -1) {
                                fieldName = fieldName.substring(i + 1);
                            }
                        }
                    }
                    String defaultMessage = o.getDefaultMessage();
                    fieldNameMsgMap.computeIfAbsent(fieldName, v -> new ArrayList<>()).add(defaultMessage);
                    return defaultMessage;
                })
                .collect(Collectors.joining("; "));
        List<ValidExceptionDetailVO> vos = convertErrorMsgMap(fieldNameMsgMap);
        log.warn("HandleBindException: {} , detail: {}", defaultMsg, vos);
        return RespResult.of(IBaseResponseStatus.PARAMETERS_ANOMALIES.getCode(), defaultMsg);
    }

    /**
     * JAVA参数校验异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public RespResult<Void> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, List<String>> fieldNameMsgMap = new LinkedHashMap<>();
        String defaultMsg = ex.getConstraintViolations()
                .stream()
                .map(o -> {
                    String fieldName = Optional.ofNullable(o.getPropertyPath()).map(Objects::toString).orElse("");
                    int i = fieldName.lastIndexOf('.');
                    if (i > -1) {
                        fieldName = fieldName.substring(i + 1);
                    }
                    String message = o.getMessage();
                    fieldNameMsgMap.computeIfAbsent(fieldName, v -> new ArrayList<>()).add(message);
                    return message;
                })
                .collect(Collectors.joining("; "));
        List<ValidExceptionDetailVO> vos = convertErrorMsgMap(fieldNameMsgMap);
        log.warn("HandleConstraintViolationException: {} , detail: {}", defaultMsg, vos);
        return RespResult.of(IBaseResponseStatus.PARAMETERS_ANOMALIES.getCode(), defaultMsg);
    }

    /**
     * 参数校验异常
     *
     * @param ex
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RespResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        Map<String, List<String>> fieldNameMsgMap = new LinkedHashMap<>();
        String defaultMsg = fieldErrors.stream()
                .map(o -> {
                    String field = o.getField();
                    String defaultMessage = o.getDefaultMessage();
                    fieldNameMsgMap.computeIfAbsent(field, v -> new ArrayList<>()).add(defaultMessage);
                    return defaultMessage;
                })
                .collect(Collectors.joining("; "));
        List<ValidExceptionDetailVO> vos = convertErrorMsgMap(fieldNameMsgMap);
        log.warn("HandleMethodArgumentNotValidException: {} , detail: {}", defaultMsg, vos);
        return RespResult.of(IBaseResponseStatus.PARAMETERS_ANOMALIES.getCode(), defaultMsg);
    }

    /**
     * 参数异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public RespResult<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.PARAMETERS_ANOMALIES);
    }

    /**
     * 状态异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalStateException.class)
    public RespResult<Void> handleIllegalStateException(IllegalStateException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.STATE_ANOMALIES.getCode(), e.getMessage());
    }

    /**
     * 上传附件大小超限异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public RespResult<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.MULTIPART_FILE_SIZE_EXCEEDED);
    }

    /**
     * 处理HttpMessageNotReadableException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public RespResult<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.error(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.INVALID_REQUEST);
    }

    /**
     * 处理HttpRequestMethodNotSupportedException
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RespResult<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 参数无效，区别于IllegalArgumentException
     * 用于特指符合目标格式，但是在业务上无效的参数传递场景，此时http状态码采用200返回
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(InvalidArgumentException.class)
    public RespResult<Void> handleInvalidArgumentException(InvalidArgumentException e) {
        log.warn(e.getMessage(), e);
        return RespResult.of(IBaseResponseStatus.PARAMETERS_INVALID.getCode(), e.getMessage());
    }

    /**
     * WEB业务异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BusinessException.class)
    public RespResult<Void> handleBusinessException(BusinessException e) {
        String message = e.getMessage();
        Integer code = e.getCode();
        if (null == code) {
            code = IBaseResponseStatus.NOT_DEFINED_ERROR.getCode();
        }
        // 可设置空字符串
        if (null == message) {
            message = IBaseResponseStatus.NOT_DEFINED_ERROR.getMessage();
        }
        log.info(message, e);
        return RespResult.of(code, message);
    }

    /**
     * 自定义业务异常
     *
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BaseBusinessException.class)
    public RespResult<Void> handleBaseBusinessException(BaseBusinessException e) {
        if (baseBusinessExceptionHandleEnable) {
            String message = e.getMessage();
            Integer code = e.getCode();
            if (null == code) {
                code = IBaseResponseStatus.NOT_DEFINED_ERROR.getCode();
            }
            // 可设置空字符串
            if (null == message) {
                message = IBaseResponseStatus.NOT_DEFINED_ERROR.getMessage();
            }
            log.info(message, e);
            return RespResult.of(code, message);
        } else {
            return handleThrowable(e);
        }
    }

    /**
     * 未知异常，屏蔽具体错误信息给前端，
     * 日志打印异常堆栈
     *
     * @param e
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {Exception.class, Throwable.class})
    public RespResult<Void> handleThrowable(Throwable e) {
        log.error(Throwables.getStackTraceAsString(e));
        return RespResult.of(IBaseResponseStatus.NOT_DEFINED_ERROR);
    }

    /**
     * 转换错误信息 map 为 校验异常详情集合
     *
     * @param fieldNameMsgMap
     * @return
     */
    private List<ValidExceptionDetailVO> convertErrorMsgMap(Map<String, List<String>> fieldNameMsgMap) {
        if (CollectionUtils.isEmpty(fieldNameMsgMap)) {
            return Collections.emptyList();
        }
        return fieldNameMsgMap.entrySet().stream().map(entry -> {
            ValidExceptionDetailVO vo = new ValidExceptionDetailVO();
            vo.setFieldName(entry.getKey());
            vo.setMessages(entry.getValue());
            return vo;
        }).collect(Collectors.toList());
    }
}
