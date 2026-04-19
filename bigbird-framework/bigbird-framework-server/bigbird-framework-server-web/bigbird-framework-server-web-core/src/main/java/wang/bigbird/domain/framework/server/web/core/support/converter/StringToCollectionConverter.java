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
package wang.bigbird.domain.framework.server.web.core.support.converter;

import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.Order;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Converts a comma-delimited String to a Collection.
 * If the target collection element type is declared, only matches if
 * {@code String.class} can be converted to it.
 *
 * @author Bigbird
 */
final public class StringToCollectionConverter implements ConditionalGenericConverter {

    private final ConversionService conversionService;


    public StringToCollectionConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * 告诉转换器总部，这个转换器支持什么样的转换，可以支持多个转换
     * 此处支持 String -> Collection
     *
     * @return
     */
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        TypeDescriptor targetTypeElementTypeDescriptor = targetType.getElementTypeDescriptor();
        if (null == targetTypeElementTypeDescriptor) {
            return false;
        }
        // 解决分页条件中，单 sort 字段被逗号分隔的问题
        // Order排序表示式，格式为：fieldName,asc，如果按照逗号分隔转换，
        // 会导致字段名称和排序方式被当成两个字段名称导致错误
        if (Order.class.equals(targetTypeElementTypeDescriptor.getResolvableType().resolve())) {
            return false;
        }
        return this.conversionService.canConvert(sourceType, targetTypeElementTypeDescriptor);
    }

    @Override
    @Nullable
    public Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (source == null) {
            return null;
        }
        String string = (String) source;
        String[] fields = StringUtils.commaDelimitedListToStringArray(string);
        TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
        Collection<Object> target = CollectionFactory.createCollection(targetType.getType(),
                (elementDesc != null ? elementDesc.getType() : null), fields.length);
        if (elementDesc == null) {
            for (String field : fields) {
                target.add(field.trim());
            }
        } else {
            for (String field : fields) {
                Object targetElement = this.conversionService.convert(field.trim(), sourceType, elementDesc);
                target.add(targetElement);
            }
        }
        return target;
    }

}
