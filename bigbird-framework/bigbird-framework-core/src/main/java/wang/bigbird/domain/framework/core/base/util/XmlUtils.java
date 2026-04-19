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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import wang.bigbird.domain.framework.core.base.tool.CustomLocalDateTimeSerializer;
import wang.bigbird.domain.framework.core.exception.ProcessingRuntimeException;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Xml工具类
 *
 * @author Bigbird
 */
public class XmlUtils {

    private XmlUtils() {
    }

    private static final JacksonXmlModule JACKSON_XML_MODULE = new JacksonXmlModule();

    private static final XmlMapper MAPPER = new XmlMapper(JACKSON_XML_MODULE);

    private static final String STANDARD_PATTERN = DateUtils.STANDARD_PATTERN;

    private static final String DATE_PATTERN = DateUtils.DATE_PATTERN;

    private static final String TIME_PATTERN = DateUtils.TIME_PATTERN;

    static {
        registerJavaTimeModule(MAPPER, STANDARD_PATTERN, DATE_PATTERN, TIME_PATTERN, null);
        configurateObjectMapper(MAPPER);
    }

    /**
     * 配置ObjectMapper
     *
     * @param objectMapper
     */
    public static void configurateObjectMapper(ObjectMapper objectMapper) {
        // 包含所有字段
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // 在序列化一个空对象时不抛出异常
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 忽略反序列化时在json字符串中存在，但在java对象中不存在的属性
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 默认禁用
        objectMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        // 大数值作为文本
        objectMapper.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        // 支持字段无双引号
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    }

    /**
     * 注册时间序列化模块
     * <p>
     * 支持jsr310，即新的时间类（java.time包下的时间类）
     *
     * @param objectMapper
     * @param dateTimePattern
     * @param datePattern
     * @param timePattern
     * @param setNullDateTime
     */
    public static void registerJavaTimeModule(ObjectMapper objectMapper, String dateTimePattern, String datePattern, String timePattern, String setNullDateTime) {
        objectMapper.setDateFormat(new SimpleDateFormat(dateTimePattern));
        objectMapper.registerModule(createJavaTimeModule(dateTimePattern, datePattern, timePattern, setNullDateTime));
    }

    /**
     * 构造时间序列化模块
     *
     * @return 时间序列化模块
     */
    public static JavaTimeModule createJavaTimeModule(String dateTimePattern, String datePattern, String timePattern, String setNullDateTime) {
        // 初始化JavaTimeModule
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        //处理LocalDateTime
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        if (StringUtils.isBlank(setNullDateTime)) {
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        } else {
            javaTimeModule.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer(dateTimeFormatter, setNullDateTime));
        }
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));

        //处理LocalDate
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(datePattern);
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));

        //处理LocalTime
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(timePattern);
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

        return javaTimeModule;
    }

    /**
     * 获取Mapper
     *
     * @return XmlMapper
     */
    public static XmlMapper getMapper() {
        return MAPPER;
    }

    /**
     * 对象转XML
     *
     * @param object 对象
     * @return xml字符串
     */
    public static String object2Xml(Object object) {
        if (null == object) {
            throw new NullPointerException("object is null.");
        }
        if (object instanceof String) {
            return (String) object;
        }
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * XML转对象
     *
     * @param xml   xml
     * @param clazz 类型
     * @return 对象
     */
    public static <T> T xml2Object(String xml, Class<T> clazz) {
        if (null == xml) {
            return null;
        }
        if (clazz.equals(String.class)) {
            return clazz.cast(xml);
        }
        try {
            return MAPPER.readValue(xml, clazz);
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

}
