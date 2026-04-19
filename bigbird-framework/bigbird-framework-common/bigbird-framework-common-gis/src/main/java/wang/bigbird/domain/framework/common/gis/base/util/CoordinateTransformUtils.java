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

/**
 * 坐标系转换工具
 * <p>
 * 1、WGS84：国际坐标系，为一种大地坐标系，也是目前广泛使用的GPS全球卫星定位系统使用的坐标系。
 * 2、GCJ02：火星坐标系，是由中国国家测绘局制订的地理信息系统的坐标系统。由WGS84坐标系经加密后的坐标系。
 * 3、BD09：百度坐标系，在GCJ02坐标系基础上再次加密。其中bd09ll表示百度经纬度坐标，bd09mc表示百度墨卡托米制坐标。
 * <p>
 * 美国GPS使用的是WGS84坐标系统，以经纬度的形式来表示地球平面上的某一个位置。
 * 但在我国，出于国家安全考虑，国内所有导航电子地图必须使用国家测绘局制定的加密坐标系统，
 * 即将一个真实的经纬度坐标（WGS84坐标系）通过中国国家测绘局制订的加密算法加密成一个不正确的经纬度坐标（GCJ-02坐标系），
 * 我们在业内将前者称之为地球坐标，后者称之为火星坐标。
 * 百度坐标在此基础上，进行了BD-09ll二次加密措施，更加保护了个人隐私。
 *
 * @author Bigbird
 */
public class CoordinateTransformUtils {
    /**
     * 圆周率
     */
    private final static double PI = 3.1415926535897932384626433832795028841971;
    private final static double X_PI = PI * 3000.0 / 180.0;
    /**
     * 长半径
     */
    private final static double EARTH_RADIUS = 6378245.0;
    /**
     * 扁率
     */
    private final static double FLATTENING = 0.00669342162296594323;

    /**
     * WGS84(国际坐标系)转GCj02(火星坐标系)
     *
     * @param wgsLng WGS84经度
     * @param wgsLat WGS84维度
     * @returns 转换后的坐标
     */
    public static WrapLngLat wgs84ToGcj02(double wgsLng, double wgsLat) {
        if (outOfChina(wgsLng, wgsLat)) {
            return new WrapLngLat(wgsLng, wgsLat);
        } else {
            double[] delta = delta(wgsLng, wgsLat);
            double mgLng = wgsLng + delta[0];
            double mgLat = wgsLat + delta[1];
            return new WrapLngLat(mgLng, mgLat);
        }
    }

    /**
     * GCJ02(火星坐标系)转WGS84(国际坐标系)
     *
     * @param gcjLng GCJ02经度
     * @param gcjLat GCJ02纬度
     * @return 转换后的坐标
     */
    public static WrapLngLat gcj02ToWgs84(double gcjLng, double gcjLat) {
        if (outOfChina(gcjLng, gcjLat)) {
            return new WrapLngLat(gcjLng, gcjLat);
        } else {
            double[] delta = delta(gcjLng, gcjLat);
            double wgsLng = gcjLng - delta[0];
            double wgsLat = gcjLat - delta[1];
            // 二次修正
            double[] delta2 = delta(wgsLng, wgsLat);
            wgsLng = gcjLng - delta2[0];
            wgsLat = gcjLat - delta2[1];
            return new WrapLngLat(wgsLng, wgsLat);
        }
    }

    /**
     * GCJ02(火星坐标系)转BD09(百度坐标系)
     *
     * @param gcjLng GCJ02经度
     * @param gcjLat GCJ02纬度
     * @return 转换后的坐标
     */
    public static WrapLngLat gcj02ToBd09(double gcjLng, double gcjLat) {
        double z =
                Math.sqrt(gcjLng * gcjLng + gcjLat * gcjLat) + 0.00002 * Math.sin(gcjLat * X_PI);
        double theta = Math.atan2(gcjLat, gcjLng) + 0.000003 * Math.cos(gcjLng * X_PI);
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new WrapLngLat(bdLng, bdLat);
    }

    /**
     * BD09(百度坐标系)转GCJ02(火星坐标系)
     *
     * @param bdLng BD09经度
     * @param bdLat BD09维度
     * @return 转换后的坐标
     */
    public static WrapLngLat bd09ToGcj02(double bdLng, double bdLat) {
        double x = bdLng - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        double gcjLng = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        return new WrapLngLat(gcjLng, gcjLat);
    }

    /**
     * WGS84(国际坐标系)转BD09(百度坐标系)
     *
     * @param wgsLng WGS84经度
     * @param wgsLat WGS84纬度
     * @return 转换后的坐标
     */
    public static WrapLngLat wgs84ToBd09(double wgsLng, double wgsLat) {
        // 1、wgs84->gcj02
        WrapLngLat gcj02 = wgs84ToGcj02(wgsLng, wgsLat);
        // 2、gcj02->bd09
        return gcj02ToBd09(gcj02.getLongitude(), gcj02.getLatitude());
    }

    /**
     * BD09(百度坐标系)转WGS84(国际坐标系)
     *
     * @param bdLng BD09经度
     * @param bdLat BD09维度
     * @return 转换后的坐标
     */
    public static WrapLngLat bd09ToWgs84(double bdLng, double bdLat) {
        // 1、bd09->gcj02
        WrapLngLat gcj02 = bd09ToGcj02(bdLng, bdLat);
        // 2、gcj02->wgs84
        return gcj02ToWgs84(gcj02.getLongitude(), gcj02.getLatitude());
    }

    /**
     * 判断经纬度是否在中国范围之外
     *
     * @param lng 经度
     * @param lat 维度
     * @return 经纬度是否在中国范围之外
     */
    private static boolean outOfChina(double lng, double lat) {
        return lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    /**
     * 计算偏移量
     *
     * @param lng 经度
     * @param lat 纬度
     * @return 偏移量[经度偏移, 纬度偏移]
     */
    private static double[] delta(double lng, double lat) {
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - FLATTENING * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLng = (dLng * 180.0) / (EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * PI);
        dLat = (dLat * 180.0) / ((EARTH_RADIUS * (1 - FLATTENING)) / (magic * sqrtMagic) * PI);
        return new double[]{dLng, dLat};
    }

    /**
     * 经度转换
     *
     * @param lon 经度
     * @param lat 维度
     * @return 转换后经度
     */
    public static double transformLng(double lon, double lat) {
        double ret = 300.0 + lon + 2.0 * lat + 0.1 * lon * lon + 0.1 * lon * lat + 0.1 * Math
                .sqrt(Math.abs(lon));
        ret += (20.0 * Math.sin(6.0 * lon * PI) + 20.0 * Math.sin(2.0 * lon * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lon * PI) + 40.0 * Math.sin(lon / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lon / 12.0 * PI) + 300.0 * Math.sin(lon / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 纬度转换
     *
     * @param lon 经度
     * @param lat 维度
     * @return 转换后维度
     */
    public static double transformLat(double lon, double lat) {
        double ret = -100.0 + 2.0 * lon + 3.0 * lat + 0.2 * lat * lat + 0.1 * lon * lat + 0.2 * Math
                .sqrt(Math.abs(lon));
        ret += (20.0 * Math.sin(6.0 * lon * PI) + 20.0 * Math.sin(2.0 * lon * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

}
