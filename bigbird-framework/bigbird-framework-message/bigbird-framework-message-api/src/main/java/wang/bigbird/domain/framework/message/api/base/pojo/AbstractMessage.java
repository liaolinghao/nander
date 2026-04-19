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
package wang.bigbird.domain.framework.message.api.base.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息对象
 *
 * @author Bigbird
 */
@Data
public abstract class AbstractMessage implements Serializable {

    private static final long serialVersionUID = -5686172516201457055L;

    /**
     * 收件人
     */
    private String recipients;

    /**
     * 抄送人
     */
    private String copyRecipients;

}
