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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import wang.bigbird.domain.framework.core.base.tool.Assert;


/**
 * 几何体关系分析工具
 *
 * @author Bigbird
 */
public class GeometryRelationshipsUtils {

    /**
     * 判断两个几何体是否相同，包括两个几何体的坐标与坐标的顺序。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 是否相同
     */
    public static boolean equalsExact(Geometry source, Geometry target) {
        return source.equalsExact(target);
    }

    /**
     * 判断两个几何体是否相同，包括两个几何体的坐标与坐标的顺序。
     *
     * @param source    源几何体
     * @param target    目标几何体
     * @param tolerance 在坐标比对的时候设置一个容差tolerance，小于该容差就认为相同
     * @return 是否相同
     */
    public static boolean equalsExact(Geometry source, Geometry target, double tolerance) {
        return source.equalsExact(target, tolerance);
    }

    /**
     * 判断两个几何体是否相同，只需要坐标相同，不考虑坐标顺序。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 是否相同
     */
    public static boolean equalsNorm(Geometry source, Geometry target) {
        return source.equalsNorm(target);
    }

    /**
     * 判断两个几何体是否拓扑相等，是一个比较耗时的操作。
     * <p>
     * 在 GIS（地理信息系统）中，判断两个几何体是否”拓扑相等“（Topologically Equal）的核心逻辑是
     * 忽略几何对象的具体坐标精度差异，仅关注其空间结构和相互关系是否完全一致。
     * 这种判断不依赖于坐标的绝对数值，而是基于几何体的“拓扑属性”（如点的位置关系、线的连接方式、面的包含关系等）。
     * <p>
     * 拓扑相等的本质是：两个几何体在经历平移、旋转、缩放（等距变换）或微小坐标扰动后，能够完全重合。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 是否拓扑相等
     */
    public static boolean equalsTopo(Geometry source, Geometry target) {
        return source.equalsTopo(target);
    }

    /**
     * 判断两个几何体是否不相交。
     * <p>
     * 在GIS（地理信息系统）中，判断两个几何体“是否不相交”（Disjoint）的核心逻辑是：
     * 两个几何体在空间中没有任何公共点或公共部分，即它们的空间范围完全分离，不存在重叠、接触或包含等关系。
     * 这种判断基于几何体的拓扑关系，遵循OGC（开放地理空间联盟）定义的“九交模型”（9 Intersection Model），
     * 核心规则是：两个几何体的内部、边界和外部之间均无交集。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 是否不相交
     */
    public static boolean disjoint(Geometry source, Geometry target) {
        return source.disjoint(target);
    }

    /**
     * 判断两个几何体是否相交。
     * <p>
     * 在GIS中，判断两个几何体“是否相交”（Intersects）的核心逻辑是：
     * 两个几何体在空间中存在至少一个公共点或公共部分，包括边界接触、部分重叠或完全包含等情况。
     * 这一判断基于OGC定义的”九交模型”，只要两个几何体的内部或边界存在交集，就判定为相交。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 是否相交
     */
    public static boolean intersects(Geometry source, Geometry target) {
        return source.intersects(target);
    }

    /**
     * 判断两个几何体是否接触。
     * <p>
     * 在GIS（地理信息系统）中，判断两个几何体“是否接触”（Topologically Touches）的核心逻辑是：
     * 两个几何体的边界（Boundary）存在公共点或公共部分，但内部（Interior）完全不重叠。
     * 简单来说，就是“边缘碰边缘，内部不相交”，几何体仅在边界处接触，没有任何内部区域的重叠，也不存在一个几何体完全包含另一个的情况。
     * 这一判断严格遵循OGC（开放地理空间联盟）定义的“九交模型”（9 Intersection Model），核心是“边界有交集，内部无交集“。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 是否接触
     */
    public static boolean touches(Geometry source, Geometry target) {
        return source.touches(target);
    }

    /**
     * 判断两个几何体是否交叉。
     * <p>
     * 在GIS（地理信息系统）中，判断两个几何体“是否交叉”（Crosses）的核心逻辑是：
     * 两个几何体的内部存在交集，但彼此都不被对方完全包含且交集的维度低于两个几何体中维度较低的那个。
     * 简单来说，就是“互相穿过但不完全覆盖且交叉部分的维度更低”。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 是否接触
     */
    public static boolean crosses(Geometry source, Geometry target) {
        return source.crosses(target);
    }

    /**
     * 判断源几何体是否在目标几何体内部。
     * <p>
     * 在GIS（地理信息系统）中，判断一个几何体A“是否在另一个几何体B内部”（Within）的核心逻辑是：
     * 几何体A的所有点（包括内部和边界）都完全位于几何体B的内部或边界上，且A的内部至少有一个点位于B的内部（避免A仅与B的边界重合而不进入其内部）。
     * 简单来说，就是“A被B完全包含，且A不是仅贴在B的边界上”。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 源几何体是否在目标几何体内部
     */
    public static boolean within(Geometry source, Geometry target) {
        return source.within(target);
    }

    /**
     * 判断源几何体是否包含目标几何体。
     * <p>
     * 在GIS（地理信息系统）中，判断几何体A“是否包含”几何体B（Contains）的核心逻辑是：
     * 几何体B的所有点（包括内部和边界）都完全位于几何体A的内部或边界上，且B的内部至少有一个点位于A的内部（避免B仅与A的边界重合）。
     * 简单来说，就是“B被A完全包裹，且B不只是贴在A的边缘上”。这一判断与“within”是互逆关系：
     * 若A contains B，则 B within A。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 源几何体是否包含目标几何体
     */
    public static boolean contains(Geometry source, Geometry target) {
        return source.contains(target);
    }

    /**
     * 判断两个几何体是否重叠。
     * <p>
     * 在GIS（地理信息系统）中，判断两个几何体“是否重叠”（Overlaps）的核心逻辑是：
     * 两个同维度的几何体存在部分公共内部区域（即互相穿透且不完全包含）且重叠部分的维度与原几何体维度相同。
     * 简单来说，就是“同维度、部分交叠、互不包含” —— 既不是完全分离，也不是一个包含另一个，而是中间有共享的内部区域。
     * 这一判断严格遵循OGC拓扑关系模型，核心规则聚焦于“维度一致性”和“部分交叠”。
     *
     * @param source 源几何体
     * @param target 目标几何体
     * @return 是否重叠
     */
    public static boolean overlaps(Geometry source, Geometry target) {
        return source.overlaps(target);
    }

    /**
     * 几何体缓冲区计算
     * <p>
     * 在GIS（地理信息系统）中，缓冲区（Buffer）是指以目标几何体为中心，
     * 按照指定的距离（或距离范围）向外（或向内）扩展生成的新面状几何体（Polygon/MultiPolygon）。
     * 其核心逻辑是“空间范围的规则扩展” —— 通过对原几何体的每个点、边或面施加统一的距离偏移，
     * 构建一个包含原几何体且边界与原几何体保持固定距离的区域，用于描述“围绕原几何体的特定影响范围”。
     * <p>
     * 缓冲区的生成过程可拆解为3个关键步骤，本质是“对原几何体的拓扑结构进行距离偏移，再处理边界衔接”：
     * 1. 基础要素的距离偏移
     * 根据原几何体的类型（点、线、面），对其核心要素（点、线段、面边界）按指定距离（d）进行偏移：
     * 点（Point/MultiPoint）：
     * 以点为圆心，d为半径画圆，生成的圆形面即为缓冲区（零维要素→二维面）。
     * 例：对坐标 (1,1) 的点，按 d=0.5 生成缓冲区，结果是圆心 (1,1)、半径 0.5 的圆。
     * 线（LineString/MultiLineString）：
     * 对线段的每一段（视为直线），沿垂直于线段的两个方向偏移 d，形成 “矩形条带”；同时对线段的端点（起点 / 终点），以 d 为半径画半圆，衔接条带的两端，最终形成闭合面（一维要素→二维面）。
     * 例：对线段 (0,0)-(2,0)，按 d=0.5 生成缓冲区，结果是 “中间矩形（宽 1、长 2）+ 两端半圆（半径 0.5）” 组成的跑道形面。
     * 面（Polygon/MultiPolygon）：
     * 分为 “外缓冲区”（向外偏移，扩大面范围）和 “内缓冲区”（向内偏移，缩小面范围）：
     * 外缓冲区：对多边形的外环边界向外偏移 d，内环边界也向外偏移 d（孔洞缩小），形成更大的面；
     * 内缓冲区：对多边形的外环边界向内偏移 d，内环边界向内偏移 d（孔洞扩大），若偏移后外环边界自交或面积为 0，则缓冲区无效（面消失）。
     * 例：对正方形面 ((0,0)-(0,2)-(2,2)-(2,0)-(0,0))，按 d=0.5 生成外缓冲区，结果是边长为 3 的正方形（原边长 2，向外各扩 0.5）。
     * 2. 边界衔接与拓扑修复
     * 偏移过程中可能出现 “边界交叉”“线段重叠” 等问题，需通过拓扑修复确保缓冲区是闭合、无自交的有效面：
     * 对线段偏移后的 “条带” 与端点 “半圆” 进行平滑衔接，避免棱角；
     * 若偏移后边界自交（如折线的锐角偏移后交叉），通过 “裁剪” 或 “合并” 处理，保留有效区域；
     * 对 Multi 类型几何体（如 MultiLineString），若各子要素的缓冲区重叠，自动合并为单一连续面（避免重复区域）。
     * 3. 距离参数的灵活控制
     * 缓冲区的距离（d）可支持多种场景：
     * 固定距离：如 d=100米，所有方向偏移距离一致（最常用）；
     * 可变距离：按要素的属性动态调整距离（如对高速公路缓冲区 d=50米，普通道路 d=20米）；
     * 负距离：仅适用于面要素，等效于 “内缓冲区”（缩小面范围）。
     *
     * @param geometry       几何体
     * @param bufferDistance 缓冲距离
     * @return 几何体缓冲区
     */
    public static Geometry calBuffer(Geometry geometry, Double bufferDistance) {
        Assert.notNull(bufferDistance, "The parameter bufferDistance is null.");
        BufferParameters bufferParams = new BufferParameters();
        // （1）设置端帽样式（仅对线要素生效）：圆形端帽
        bufferParams.setEndCapStyle(BufferParameters.CAP_ROUND);
        // （2）设置侧角样式（仅对线要素生效）：圆形侧角
        bufferParams.setJoinStyle(BufferParameters.JOIN_ROUND);
        // （3）设置圆弧精度（象限分段数：8，默认值，平衡平滑度与效率）
        bufferParams.setQuadrantSegments(8);
        return BufferOp.bufferOp(geometry, bufferDistance, bufferParams);
    }

}
