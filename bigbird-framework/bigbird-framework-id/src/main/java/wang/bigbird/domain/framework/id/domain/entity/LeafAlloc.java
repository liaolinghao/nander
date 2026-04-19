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
package wang.bigbird.domain.framework.id.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.domain.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 美团leaf信息表实体
 *
 * @author Bigbird
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@TableName(value = "td_leaf_alloc")
public class LeafAlloc {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bizTag;

    private Long maxId;

    private Integer step;

    private String description;

    private LocalDateTime createTime = LocalDateTime.now();

    private LocalDateTime updateTime = LocalDateTime.now();

}
