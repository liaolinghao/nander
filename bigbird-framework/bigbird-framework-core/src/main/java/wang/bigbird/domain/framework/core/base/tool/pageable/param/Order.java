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
package wang.bigbird.domain.framework.core.base.tool.pageable.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;

import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 排序方式
 *
 * @author Bigbird
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    private static final long serialVersionUID = -4316583183659545619L;

    private static final String SORT_DELIMITER = ",";
    private static final int ORDER_PARTS_WITH_DIRECTION = 2;

    @Pattern(regexp = "^\\w+$", message = "无效的字段名称！")
    private String column;

    private Boolean asc = true;

    /**
     * 排序表示式，格式为：fieldName,asc 或者 fieldName,desc
     *
     * @param order
     */
    public Order(String order) {
        String[] split = order.split(SORT_DELIMITER);
        String field = split[0].trim();
        boolean ascend;
        if (split.length == 1) {
            ascend = true;
        } else if (split.length == ORDER_PARTS_WITH_DIRECTION) {
            String direction = split[1].trim().toLowerCase();
            if (CommonConstants.OrderType.DESC.equals(direction)) {
                ascend = false;
            } else if (CommonConstants.OrderType.ASC.equals(direction)) {
                ascend = true;
            } else {
                throw new IllegalArgumentException("Order direction must be EITHER ascend OR desc");
            }
        } else {
            throw new IllegalArgumentException("Order param is not valid, like: name OR name,ascend");
        }
        this.column = field;
        this.asc = ascend;
    }

    @Override
    public String toString() {
        String direction;
        if (null == asc || asc) {
            direction = CommonConstants.OrderType.ASC;
        } else {
            direction = CommonConstants.OrderType.DESC;
        }
        return column + SORT_DELIMITER + direction;
    }
}
