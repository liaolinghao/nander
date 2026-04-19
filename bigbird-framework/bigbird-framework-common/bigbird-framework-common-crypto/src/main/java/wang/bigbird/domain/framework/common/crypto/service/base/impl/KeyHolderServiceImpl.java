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
package wang.bigbird.domain.framework.common.crypto.service.base.impl;

import lombok.Data;
import lombok.experimental.Accessors;
import wang.bigbird.domain.framework.common.crypto.service.base.IKeyHolderService;

/**
 * @author Bigbird
 */
@Data
@Accessors(chain = true)
public class KeyHolderServiceImpl implements IKeyHolderService {
    /**
     * 加密的key
     */
    private String encryptKey;
    /**
     * 解密的key
     */
    private String decryptKey;
    /**
     * 偏移向量
     */
    private String iv;
}
