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
package wang.bigbird.domain.framework.common.crypto.service.base;

import wang.bigbird.domain.framework.common.crypto.base.enums.Sm4KeyLengthEnum;

/**
 * SM4加密解密服务，对称算法，加密和解密都是用同一个密钥
 *
 * @author Bigbird
 */
public interface ISm4CryptoService extends ICryptoService {

    /**
     * 生成密钥
     *
     * @param keyLength 生成的密钥长度
     * @return 返回字符串
     *
     */
    String createKey(Sm4KeyLengthEnum keyLength);

}
