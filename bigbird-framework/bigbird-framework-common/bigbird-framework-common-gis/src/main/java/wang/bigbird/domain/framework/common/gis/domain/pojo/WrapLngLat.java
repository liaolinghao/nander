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
package wang.bigbird.domain.framework.common.gis.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Longitude（经度）和 Latitude（纬度）封装对象
 *
 * @author Bigbird
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrapLngLat {

    /**
     * 经度
     */
    private Double longitude;
    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 生成WKT串
     * WKT串，全称为Well-Known Text（知名文本），是一种用于表示地理空间几何对象的文本编码标准，
     * 由开放地理空间联盟（OGC）制定，旨在为不同地理信息系统（GIS）、空间数据库（如：PostgreSQL/PostGIS、MySQL Spatial）
     * 和空间数据处理工具提供统一的几何数据交换格式
     *
     * @return WKT串
     */
    public String toWktString() {
        return longitude + " " + latitude;
    }

}
