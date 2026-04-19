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

import lombok.Data;

import java.util.List;

/**
 * 动态图片数据对象
 *
 * @author Bigbird
 */
@Data
public class ImageData {

    /**
     * 图片标识
     */
    private String imageMark;
    /**
     * 图片占据的单元格
     */
    private List<ImageCell> imageCells;
    /**
     * 图像值
     */
    private String imageValue;
    /**
     * 最终表单中的图片起始行
     */
    private int realStartRow;
    /**
     * 图片最大宽
     */
    private int width;
    /**
     * 图片最大高
     */
    private int height;

}
