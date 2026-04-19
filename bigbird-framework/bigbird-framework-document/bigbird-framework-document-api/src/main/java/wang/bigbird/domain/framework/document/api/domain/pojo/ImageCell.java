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
package wang.bigbird.domain.framework.document.api.domain.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 图像单元对象
 *
 * @author Bigbird
 */
@Data
@AllArgsConstructor
public class ImageCell {

    /**
     * 图片左上角所在单元格行索引
     */
    private int leftTopRow;
    /**
     * 图片左上角所在单元格列索引
     */
    private int leftTopCol;
    /**
     * 图片右下角所在单元格行索引
     */
    private int rightBottomRow;
    /**
     * 图片右下角所在单元格列索引
     */
    private int rightBottomCol;
    /**
     * 图片左上角距离所在单元格左边框像素值
     */
    private int left;
    /**
     * 图片左上角距离所在单元格上边框像素值
     */
    private int top;
    /**
     * 图片右下角距离所在单元格右边框像素值
     */
    private int right;
    /**
     * 图片右下角距离所在单元格下边框像素值
     */
    private int bottom;

}
