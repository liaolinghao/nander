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

import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import wang.bigbird.domain.framework.common.gis.domain.pojo.WrapLngLat;
import wang.bigbird.domain.framework.core.base.tool.Assert;

import java.util.List;

/**
 * 几何体构造工具
 *
 * @author Bigbird
 */
public class GeometryGenerateUtils {

    /**
     * 构造点
     * <p>
     * Point（点）是GIS（地理信息系统）中最基础、最简单的空间要素类型，
     * 由OGC（开放地理空间联盟）标准化定义。它用于描述地理空间中的单个、零维度位置，
     * 是构成 LineString（线）、Polygon（面）等复杂空间要素的“基本单元”，常见应用如：
     * 标记POI（兴趣点）、采样点、设备定位坐标等。
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param wktReader WKT解析器
     * @return 点
     * @throws ParseException
     */
    public static Point generatePoint(Double longitude, Double latitude, WKTReader wktReader)
            throws ParseException {
        WrapLngLat wrapLngLat = new WrapLngLat(longitude, latitude);
        String pointWKTStr = WktUtils.toPointWkt(wrapLngLat);
        return (Point) wktReader.read(pointWKTStr);
    }

    /**
     * 构造点
     * <p>
     * Point（点）是GIS（地理信息系统）中最基础、最简单的空间要素类型，
     * 由OGC（开放地理空间联盟）标准化定义。它用于描述地理空间中的单个、零维度位置，
     * 是构成 LineString（线）、Polygon（面）等复杂空间要素的“基本单元”，常见应用如：
     * 标记POI（兴趣点）、采样点、设备定位坐标等。
     *
     * @param pointWKTStr 点的WKT串
     * @param wktReader   WKT解析器
     * @return 点
     * @throws ParseException
     */
    public static Point generatePoint(String pointWKTStr, WKTReader wktReader) throws ParseException {
        return (Point) wktReader.read(pointWKTStr);
    }

    /**
     * 构造线串
     * <p>
     * LineString是GIS中最基础的“线状”空间要素类型，用于描述连续的、由多个点连接而成的线段或折线，例如：
     * 道路、河流的中心线
     * 两个地点之间的路径
     * 区域边界的一部分（如省界的某一段）
     * 它的核心特征是：
     * 由至少2个不重复的点（Point）按顺序组成（少于2个点则为无效LineString）；
     * 点与点之间通过直线段连接，整体可呈“直线”（2个点）或”折线“（≥3个点）形态。
     *
     * @param lineStringWKTStr 线串的WKT串
     * @param wktReader        WKT解析器
     * @return 线串
     * @throws ParseException
     */
    public static LineString generateLineString(String lineStringWKTStr, WKTReader wktReader)
            throws ParseException {
        return (LineString) wktReader.read(lineStringWKTStr);
    }

    /**
     * 构造多边形
     * <p>
     * Polygon（多边形）是GIS（地理信息系统）中用于表示二维闭合区域的基础空间要素类型，
     * 由OGC（开放地理空间联盟）标准化定义。
     * 它适用于描述具有明确边界的面状区域，如国家、湖泊、建筑物轮廓等。
     *
     * @param polygonWKTStr 多边形的WKT串
     * @param wktReader     WKT解析器
     * @return 多边形
     * @throws ParseException
     */
    public static Polygon generatePolygon(String polygonWKTStr, WKTReader wktReader)
            throws ParseException {
        return (Polygon) wktReader.read(polygonWKTStr);
    }

    /**
     * 构造多点
     * <p>
     * MultiPoint（多点）是GIS（地理信息系统）中用于表示多个独立Point（点）集合的空间要素类型，
     * 由OGC（开放地理空间联盟）标准化定义。它适用于描述彼此分离但逻辑上相关的多个单点位置，例如：
     * 一组监测站点、多个兴趣点（POI）、同一区域的采样点等，核心价值是将“多个独立点”作为一个整体进行管理和操作。
     *
     * @param points          坐标点集合
     * @param geometryFactory 几何对象创建器
     * @return 多点
     */
    public static MultiPoint generateMultiPoint(List<WrapLngLat> points, GeometryFactory geometryFactory) {
        Assert.notEmpty(points, "The parameter points is empty.");
        Point[] ps = new Point[points.size()];
        for (int i = 0; i < points.size(); i++) {
            WrapLngLat lngLat = points.get(i);
            Coordinate coordinate = new Coordinate(lngLat.getLongitude(), lngLat.getLatitude());
            Point point = geometryFactory.createPoint(coordinate);
            ps[i] = point;
        }
        return new MultiPoint(ps, geometryFactory);
    }

    /**
     * 构造多点
     * <p>
     * MultiPoint（多点）是GIS（地理信息系统）中用于表示多个独立Point（点）集合的空间要素类型，
     * 由OGC（开放地理空间联盟）标准化定义。它适用于描述彼此分离但逻辑上相关的多个单点位置，例如：
     * 一组监测站点、多个兴趣点（POI）、同一区域的采样点等，核心价值是将“多个独立点”作为一个整体进行管理和操作。
     *
     * @param points    坐标点集合
     * @param wktReader WKT解析器
     * @return 多点
     * @throws ParseException
     */
    public static MultiPoint generateMultiPoint(List<WrapLngLat> points, WKTReader wktReader) throws ParseException {
        String multiPointWKTStr = WktUtils.toMultiPointWkt(points);
        return (MultiPoint) wktReader.read(multiPointWKTStr);
    }

    /**
     * 构造多点
     * <p>
     * MultiPoint（多点）是GIS（地理信息系统）中用于表示多个独立Point（点）集合的空间要素类型，
     * 由OGC（开放地理空间联盟）标准化定义。它适用于描述彼此分离但逻辑上相关的多个单点位置，例如：
     * 一组监测站点、多个兴趣点（POI）、同一区域的采样点等，核心价值是将“多个独立点”作为一个整体进行管理和操作。
     *
     * @param multiPointWKTStr 多点的WKT串
     * @param wktReader        WKT解析器
     * @return 多点
     * @throws ParseException
     */
    public static MultiPoint generateMultiPoint(String multiPointWKTStr, WKTReader wktReader) throws ParseException {
        return (MultiPoint) wktReader.read(multiPointWKTStr);
    }

    /**
     * 构造多线串
     * <p>
     * MultiLineString是GIS（地理信息系统）中用于表示多个独立LineString（线串）集合的空间要素类型，
     * 属于OGC（开放地理空间联盟）定义的标准空间数据类型之一。
     * 它适用于描述彼此分离但逻辑上相关的多条线段，例如一组不相连的道路、多条独立的河流支流等。
     *
     * @param multiLineStringWKTStr 多线串的WKT串
     * @param wktReader             WKT解析器
     * @return 多线串
     * @throws ParseException
     */
    public static MultiLineString generateMultiLineString(String multiLineStringWKTStr,
                                                          WKTReader wktReader) throws ParseException {
        return (MultiLineString) wktReader.read(multiLineStringWKTStr);
    }

    /**
     * 构造多边形集
     * <p>
     * MultiPolygon（多多边形）是GIS（地理信息系统）中用于表示多个独立Polygon（多边形）集合的空间要素类型，
     * 属于OGC标准定义的复合空间类型。它适用于描述彼此分离但逻辑上相关的多个面状区域，
     * 例如：群岛（多个岛屿）、分散的湖泊群、一组不相连的行政区域等。
     *
     * @param multiPolygonWKTStr 多边形集的WKT串
     * @param wktReader          WKT解析器
     * @return 多边形集
     * @throws ParseException
     */
    public static MultiPolygon generateMultiPolygon(String multiPolygonWKTStr, WKTReader wktReader)
            throws ParseException {
        return (MultiPolygon) wktReader.read(multiPolygonWKTStr);
    }

}
