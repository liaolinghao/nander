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
package wang.bigbird.domain.framework.server.web.core.domain.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.IPageable;
import wang.bigbird.domain.framework.core.base.tool.pageable.param.Order;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.List;

/**
 * 分页参数信息
 *
 * @author Bigbird
 */
@Data
@ApiModel("分页参数信息")
public class PageParam implements IPageable, Serializable {

    private static final long serialVersionUID = -8698410500385452607L;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    @ApiModelProperty("第几页，默认为1")
    private int page = 1;

    @Min(0)
    @Max(Integer.MAX_VALUE)
    @ApiModelProperty("每页显示的总条数，默认10")
    private int pageSize = 10;

    @Valid
    @ApiModelProperty("排序字段及排序方式（asc,desc） 默认 asc，例如['name,asc','age,desc']")
    private List<Order> sort;

    @ApiModelProperty("是否统计总数，0不统计，1统计，默认统计")
    private boolean searchCount = true;
}
