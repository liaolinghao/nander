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

import cn.hutool.core.bean.BeanUtil;
import lombok.Data;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 动态表格数据对象
 *
 * @author Bigbird
 */
@Data
public class TableData {

    /**
     * 表格索引，代表这是文档中第几个表格对应的数据
     */
    private int tableIndex;
    /**
     * 表格标识
     */
    private String tableMark;
    /**
     * 一页纸内的最少行
     */
    private int pageMinRow;
    /**
     * 一页纸内的最大行
     */
    private int pageMaxRow;
    /**
     * 模板里面的起始行索引
     */
    private int startRow;
    /**
     * 逻辑行占据物理行数
     */
    private int rowSpan;
    /**
     * 表格按照分页页码划分的行数据集合
     */
    private Map<Integer, List<RowData>> rowDataMap;
    /**
     * 最终文件中的表格起始行偏移量
     */
    private int rowOffset;
    /**
     * 表格跨行合并列的行起始索引
     */
    private int mergeStartRow;
    /**
     * 表格跨行合并列的列索引
     */
    private int mergeCol;
    /**
     * 是否需要特别执行跨行跨列处理
     */
    private boolean hasSpecialMergeArea;

    /**
     * 获取对应页的行数据集合，如果对应页不存在，则构造空行用于多页填充数据时出现的数据不足场景
     *
     * @param pageNum 页码
     * @return 页码对应的数据行集合
     */
    public List<RowData> getRowData(int pageNum) {
        if (rowDataMap.containsKey(pageNum)) {
            return rowDataMap.get(pageNum);
        }
        // 以第一页数据为基准
        List<RowData> copyRowDatas = new ArrayList<>();
        List<RowData> rowDatas = rowDataMap.get(1);
        int size = pageMinRow == 0 ? 1 : pageMinRow;
        for (int i = 0; i < size; i++) {
            RowData rowData = rowDatas.get(i);
            RowData blankRowData = new RowData();
            BeanUtil.copyProperties(rowData, blankRowData);
            List<CellData> cellDatas = blankRowData.getCellDatas();
            for (CellData cellData : cellDatas) {
                cellData.setCellValue("");
            }
            copyRowDatas.add(blankRowData);
        }
        return copyRowDatas;
    }

    /**
     * 获取表格数据占据的页面数量
     *
     * @return 页面数量
     */
    public int getPageNum() {
        return CollectionUtils.isEmpty(rowDataMap) ? 1 : rowDataMap.size();
    }

}
