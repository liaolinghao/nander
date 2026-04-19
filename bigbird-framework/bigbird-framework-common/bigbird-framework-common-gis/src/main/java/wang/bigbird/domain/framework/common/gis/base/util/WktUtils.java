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
package wang.bigbird.domain.framework.common.gis.base.util;

import wang.bigbird.domain.framework.common.gis.domain.pojo.WrapLngLat;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import java.util.List;

/**
 * WKT串生成工具
 *
 * @author Bigbird
 */
public class WktUtils {

    /**
     * 生成表示点的WKT串
     * Point（点）
     * 表示一个单一的位置。
     * 示例：POINT (10 20)
     *
     * @param point 坐标点
     * @return 点的WKT串
     */
    public static String toPointWkt(WrapLngLat point) {
        Assert.notNull(point, "The parameter point is null.");
        return "POINT(" + point.toWktString() + CommonConstants.PARENTHESIS_END;
    }

    /**
     * 生成表示线串的WKT串
     * LineString（线串）
     * 表示一系列有序的点，形成一条线。
     * 示例：LINESTRING (10 10, 20 20, 30 30)
     *
     * @param points 多个坐标点
     * @return 线串的WKT串
     */
    public static String toLineStringWkt(List<WrapLngLat> points) {
        Assert.notEmpty(points, "The parameter points is empty.");
        StringBuilder stringBuilder = new StringBuilder("LINESTRING(");
        points.forEach(point -> {
            stringBuilder.append(point.toWktString());
            stringBuilder.append(CommonConstants.COMMA);
        });
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(CommonConstants.COMMA));
        stringBuilder.append(CommonConstants.PARENTHESIS_END);
        return stringBuilder.toString();
    }

    /**
     * 生成表示多边形的WKT串
     * Polygon（多边形）
     * 表示一个封闭的区域，由一系列有序的点组成，最后一个点与第一个点相连。
     * 示例：POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))-无孔洞
     * POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10),(20 30, 35 35, 30 20, 20 30))-带孔洞，
     * 第一个（且至少一个）内层括号为外环，其他表示内环，内环必须完全位于外环内部，且彼此不相交。
     *
     * @param polygons 多个多边形坐标点
     * @return 多边形的WKT串
     */
    public static String toPolygonWkt(List<List<WrapLngLat>> polygons) {
        Assert.notEmpty(polygons, "The parameter polygons is empty.");
        StringBuilder stringBuilder = new StringBuilder("POLYGON (");
        polygons.forEach(polygon -> {
            stringBuilder.append(pointsToWkt(polygon));
            stringBuilder.append(CommonConstants.COMMA);
        });
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(CommonConstants.COMMA));
        stringBuilder.append(CommonConstants.PARENTHESIS_END);
        return stringBuilder.toString();
    }

    /**
     * 生成表示多点的WKT串
     * MultiPoint（多点）
     * 表示多个独立的点。
     * 示例：MULTIPOINT ((10 10), (20 20), (30 30))
     *
     * @param points 多个坐标点
     * @return 多点的WKT串
     */
    public static String toMultiPointWkt(List<WrapLngLat> points) {
        Assert.notEmpty(points, "The parameter points is empty.");
        StringBuilder stringBuilder = new StringBuilder("MULTIPOINT(");
        points.forEach(point -> {
            stringBuilder.append(CommonConstants.PARENTHESIS_START);
            stringBuilder.append(point.toWktString());
            stringBuilder.append(CommonConstants.PARENTHESIS_END);
            stringBuilder.append(CommonConstants.COMMA);
        });
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(CommonConstants.COMMA));
        stringBuilder.append(CommonConstants.PARENTHESIS_END);
        return stringBuilder.toString();
    }


    /**
     * 生成表示多线串的WKT串
     * MultiLineString（多线串）
     * 表示多个独立的线串。
     * 示例：MULTILINESTRING ((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10))
     *
     * @param multiLines 多个多线串坐标点
     * @return 多线串的WKT串
     */
    public static String toMultiLineStringWkt(List<List<WrapLngLat>> multiLines) {
        Assert.notEmpty(multiLines, "The parameter polygons is empty.");
        StringBuilder stringBuilder = new StringBuilder("MULTILINESTRING(");
        multiLines.forEach(multiLine -> {
            stringBuilder.append(pointsToWkt(multiLine));
            stringBuilder.append(CommonConstants.COMMA);
        });
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(CommonConstants.COMMA));
        stringBuilder.append(CommonConstants.PARENTHESIS_END);
        return stringBuilder.toString();
    }

    /**
     * 生成表示多边形集的WKT串
     * MultiPolygon（多边形集）
     * 表示多个独立的多边形。
     * 示例：MULTIPOLYGON (((30 20, 45 40, 10 40, 30 20)), ((15 5, 40 10, 10 20, 5 10, 15 5)))
     *
     * @param multiPolygons 多个多边形坐标点
     * @return 多边形集的WKT串
     */
    public static String toMultiPolygonWkt(List<List<List<WrapLngLat>>> multiPolygons) {
        Assert.notEmpty(multiPolygons, "The parameter multiPolygons is empty.");
        StringBuilder stringBuilder = new StringBuilder("MULTIPOLYGON(");
        multiPolygons.forEach(multiPolygon -> {
            stringBuilder.append(CommonConstants.PARENTHESIS_START);
            multiPolygon.forEach(polygon -> {
                stringBuilder.append(pointsToWkt(polygon));
                stringBuilder.append(CommonConstants.COMMA);
            });
            stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(CommonConstants.COMMA));
            stringBuilder.append(CommonConstants.PARENTHESIS_END);
            stringBuilder.append(CommonConstants.COMMA);
        });
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(CommonConstants.COMMA));
        stringBuilder.append(CommonConstants.PARENTHESIS_END);
        return stringBuilder.toString();
    }

    private static String pointsToWkt(List<WrapLngLat> points) {
        StringBuilder stringBuilder = new StringBuilder(CommonConstants.PARENTHESIS_START);
        points.forEach(point -> {
            stringBuilder.append(point.toWktString());
            stringBuilder.append(CommonConstants.COMMA);
        });
        stringBuilder.deleteCharAt(stringBuilder.lastIndexOf(CommonConstants.COMMA));
        stringBuilder.append(CommonConstants.PARENTHESIS_END);
        return stringBuilder.toString();
    }

}
