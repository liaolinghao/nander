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
package wang.bigbird.domain.framework.common.gis.config.configuration;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GIS配置
 *
 * @author Bigbird
 */
@Configuration
public class GisConfiguration {

    /**
     * 几何对象创建器
     * <p>
     * 主要用于创建和管理各种空间几何对象（如 Point、LineString、Polygon 等），
     * 是许多空间库（如 Java的JTS、GeoTools，或C#的NetTopologySuite等）中的关键组件。
     * 它的核心作用是封装几何对象的创建逻辑，
     * 提供统一的接口来生成符合规范的空间几何实例。
     *
     * @return
     */
    @Bean(name = "geometryFactory")
    public GeometryFactory geometryFactory() {
        return new GeometryFactory();
    }

    /**
     * WKT解析器
     *
     * @param geometryFactory 几何对象创建器
     * @return WKT解析器
     * @Qualifier 注解主要用于解决依赖注入时的歧义性问题。当容器中存在多个类型相同的 Bean 时，
     * 仅通过类型无法确定要注入哪一个，
     * 此时可以通过 @Qualifier 指定具体要注入的 Bean 的名称或标识符，明确依赖关系。
     */
    @Bean(name = "wktReader")
    public WKTReader wktReader(@Qualifier("geometryFactory") GeometryFactory geometryFactory) {
        return new WKTReader(geometryFactory);
    }

}
