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
package wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert.converter;

import org.springframework.cache.Cache;
import org.springframework.core.convert.converter.Converter;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.EnhancedCache;
import wang.bigbird.domain.framework.cache.support.cacheasmulti.cache.convert.EnhancedCacheConversionService;

/**
 * {@link Cache} 到 {@link EnhancedCache} 的转换器
 * 会被自动加载到 {@link EnhancedCacheConversionService} 中
 *
 * @author Bigbird
 */
public interface EnhancedCacheConverter<T> extends Converter<T, EnhancedCache> {

}
