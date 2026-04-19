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

/**
 * 动态表格包含的单元格数据对象
 *
 * @author Bigbird
 */
@Data
public class CellData {

    /**
     * 单元格标识
     */
    private String cellMark;
    /**
     * 单元格所在的行起始索引
     */
    private int rowStart;
    /**
     * 单元格所在的行结束索引
     */
    private int rowEnd;
    /**
     * 单元格所在的列起始索引
     */
    private int colStart;
    /**
     * 单元格所在的列结束索引
     */
    private int colEnd;
    /**
     * 单元格的数值
     */
    private String cellValue;
    /**
     * 单元格对应数值效验规则
     */
    private String cellRule;

    /**
     * 单元格是否需要执行行列合并处理
     *
     * @return 判断结果
     */
    public boolean needMerge() {
        return rowStart != rowEnd || colStart != colEnd;
    }

}
