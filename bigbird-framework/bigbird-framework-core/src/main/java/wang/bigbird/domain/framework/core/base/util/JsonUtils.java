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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import wang.bigbird.domain.framework.core.base.tool.CustomLocalDateTimeSerializer;
import wang.bigbird.domain.framework.core.exception.ProcessingRuntimeException;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Json工具类
 *
 * @author Bigbird
 */
public class JsonUtils {

    private JsonUtils() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
     * 注册大数值转换为字符串序列化模块
     *
     * @param objectMapper
     */
    public static void registerJavaLong2StringModule(ObjectMapper objectMapper) {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
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
     * 获取该Json工具类的默认转换器，一般获取后仅供使用，
     * 不建议对其注册转换模块，否则会影响该Json工具类的全局转换方式
     *
     * @return 转换器
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * 获取与该Json工具类的默认转换器处理方式一样的转换器，该转换器获取后可以再
     * 次对其注册转换模块以完成特定业务
     *
     * @return 转换器
     */
    public static ObjectMapper getRegisterMapper() {
        ObjectMapper mapper = new ObjectMapper();
        registerJavaTimeModule(mapper, STANDARD_PATTERN, DATE_PATTERN, TIME_PATTERN, null);
        configurateObjectMapper(mapper);
        return mapper;
    }

    /**
     * 对象转JSON
     *
     * @param object 对象
     * @return json字符串
     */
    public static String object2Json(Object object) {
        return object2Json(object, null);
    }

    /**
     * 对象转JSON
     *
     * @param object 对象
     * @param mapper 转换器，为空使用默认转换器
     * @return json字符串
     */
    public static String object2Json(Object object, ObjectMapper mapper) {
        return object2Json(object, mapper, false);
    }

    /**
     * 对象转JSON
     *
     * @param object 对象
     * @return 格式化的json字符串
     */
    public static String object2JsonPretty(Object object) {
        return object2JsonPretty(object, null);
    }

    /**
     * 对象转JSON
     *
     * @param object 对象
     * @param mapper 转换器，为空使用默认转换器
     * @return 格式化的json字符串
     */
    public static String object2JsonPretty(Object object, ObjectMapper mapper) {
        return object2Json(object, mapper, true);
    }

    private static String object2Json(Object object, ObjectMapper mapper, boolean isPretty) {
        if (null == object) {
            throw new NullPointerException("object is null.");
        }
        if (object instanceof String) {
            return (String) object;
        }
        try {
            if (mapper == null) {
                if (isPretty) {
                    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
                } else {
                    return MAPPER.writeValueAsString(object);
                }
            } else {
                if (isPretty) {
                    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
                } else {
                    return mapper.writeValueAsString(object);
                }
            }
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * JSON转对象
     *
     * @param json  json
     * @param clazz 类型
     * @return 对象
     */
    public static <T> T json2Object(String json, Class<T> clazz) {
        return json2Object(json, clazz, null);
    }

    /**
     * JSON转对象
     *
     * @param json   json
     * @param clazz  类型
     * @param mapper 转换器，为空使用默认转换器
     * @return 对象
     */
    public static <T> T json2Object(String json, Class<T> clazz, ObjectMapper mapper) {
        if (null == json) {
            return null;
        }
        if (clazz.equals(String.class)) {
            return clazz.cast(json);
        }
        try {
            if (mapper == null) {
                return MAPPER.readValue(json, clazz);
            } else {
                return mapper.readValue(json, clazz);
            }
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * JSON转对象
     *
     * @param json          json
     * @param typeReference typeReference
     * @return 对象
     */
    public static <T> T json2Object(String json, TypeReference<T> typeReference) {
        return json2Object(json, typeReference, null);
    }

    /**
     * JSON转对象
     *
     * @param json          json
     * @param typeReference typeReference
     * @param mapper        转换器，为空使用默认转换器
     * @return 对象
     */
    public static <T> T json2Object(String json, TypeReference<T> typeReference, ObjectMapper mapper) {
        if (null == json) {
            return null;
        }
        try {
            if (mapper == null) {
                return MAPPER.readValue(json, typeReference);
            } else {
                return mapper.readValue(json, typeReference);
            }
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * JSON转Node
     *
     * @param json json
     * @return JsonNode
     */
    public static JsonNode json2Node(String json) {
        return json2Node(json, null);
    }

    /**
     * JSON转Node
     *
     * @param json   json
     * @param mapper 转换器，为空使用默认转换器
     * @return JsonNode
     */
    public static JsonNode json2Node(String json, ObjectMapper mapper) {
        if (null == json) {
            return null;
        }
        try {
            if (mapper == null) {
                return MAPPER.readTree(json);
            } else {
                return mapper.readTree(json);
            }
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * JSON转List
     *
     * @param json  json
     * @param clazz 类型
     * @return list
     */
    public static <T> List<T> json2List(String json, Class<T> clazz) {
        return json2List(json, clazz, null);
    }

    /**
     * JSON转List
     *
     * @param json   json
     * @param clazz  类型
     * @param mapper 转换器，为空使用默认转换器
     * @return list
     */
    public static <T> List<T> json2List(String json, Class<T> clazz, ObjectMapper mapper) {
        if (null == json) {
            return null;
        }
        try {
            if (mapper == null) {
                CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
                return MAPPER.readValue(json, collectionType);
            } else {
                CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
                return mapper.readValue(json, collectionType);
            }
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * JSON转Set
     *
     * @param json  json
     * @param clazz 类型
     * @return set
     */
    public static <T> Set<T> json2Set(String json, Class<T> clazz) {
        return json2Set(json, clazz, null);
    }

    /**
     * JSON转Set
     *
     * @param json   json
     * @param clazz  类型
     * @param mapper 转换器，为空使用默认转换器
     * @return set
     */
    public static <T> Set<T> json2Set(String json, Class<T> clazz, ObjectMapper mapper) {
        if (null == json) {
            return null;
        }
        try {
            if (mapper == null) {
                CollectionType collectionType = MAPPER.getTypeFactory().constructCollectionType(Set.class, clazz);
                return MAPPER.readValue(json, collectionType);
            } else {
                CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(Set.class, clazz);
                return mapper.readValue(json, collectionType);
            }
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * JSON转Map
     *
     * @param json json
     * @return map
     */
    public static Map json2Map(String json) {
        return json2Map(json, null);
    }

    /**
     * JSON转Map
     *
     * @param json   json
     * @param mapper 转换器，为空使用默认转换器
     * @return map
     */
    public static Map json2Map(String json, ObjectMapper mapper) {
        if (null == json) {
            return null;
        }
        try {
            if (mapper == null) {
                return MAPPER.readValue(json, Map.class);
            } else {
                return mapper.readValue(json, Map.class);
            }
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * 递归遍历JSON节点，将所有Long类型值转为String
     *
     * @param node 原始JSON节点
     * @return 转换后的JSON节点
     */
    public static JsonNode convertLongToString(JsonNode node) {
        if (node == null) {
            return null;
        }
        // 如果是数组节点，遍历每个元素递归处理
        if (node.isArray()) {
            ArrayNode arrayNode = new ArrayNode(MAPPER.getNodeFactory());
            for (JsonNode item : node) {
                arrayNode.add(convertLongToString(item));
            }
            return arrayNode;
        }
        // 如果是对象节点，遍历每个字段递归处理
        else if (node.isObject()) {
            ObjectNode objectNode = new ObjectNode(MAPPER.getNodeFactory());
            node.fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();
                objectNode.set(fieldName, convertLongToString(fieldValue));
            });
            return objectNode;
        }
        // 如果是Long类型值，转为String节点
        else if (node.isLong()) {
            return new TextNode(node.asText());
        }
        // 其他类型（字符串、布尔、null等）直接返回
        else {
            return node;
        }
    }

    /**
     * 把JSON字符串中的Long类型转换为String
     *
     * @param json json
     * @return 转换后的字符串
     */
    public static String convertLongToString(String json) {
        if (StringUtils.isBlank(json)) {
            return json;
        }
        try {
            // 解析原始JSON为JsonNode
            JsonNode originalNode = MAPPER.readTree(json);
            // 转换Long为String
            JsonNode convertedNode = convertLongToString(originalNode);
            // 转为格式化后的JSON字符串输出
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(convertedNode);
        } catch (JsonProcessingException e) {
            throw new ProcessingRuntimeException(e);
        }
    }

    /**
     * 根据json key排序
     *
     * @param json 待排序的JSON字符串
     * @return 排序后的JSON字符串
     */
    public static String sortJson(String json) {
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        com.google.gson.JsonParser p = new com.google.gson.JsonParser();
        JsonElement e = p.parse(json);
        sort(e);
        return g.toJson(e);
    }

    /**
     * 排序
     *
     * @param e
     */
    private static void sort(JsonElement e) {
        if (e.isJsonNull() || e.isJsonPrimitive()) {
            return;
        }
        if (e.isJsonArray()) {
            JsonArray a = e.getAsJsonArray();
            Iterator<JsonElement> it = a.iterator();
            it.forEachRemaining(i -> sort(i));
            return;
        }
        if (e.isJsonObject()) {
            Map<String, JsonElement> tm = new TreeMap<>(Comparator.naturalOrder());
            for (Map.Entry<String, JsonElement> en : e.getAsJsonObject().entrySet()) {
                tm.put(en.getKey(), en.getValue());
            }
            String key;
            JsonElement val;
            for (Map.Entry<String, JsonElement> en : tm.entrySet()) {
                key = en.getKey();
                val = en.getValue();
                e.getAsJsonObject().remove(key);
                e.getAsJsonObject().add(key, val);
                sort(val);
            }
        }
    }

}
