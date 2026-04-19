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

import wang.bigbird.domain.framework.common.crypto.base.enums.RsaKeyBitsEnum;
import wang.bigbird.domain.framework.common.crypto.domain.bo.KeyPairBO;

/**
 * RSA加密解密服务，非对称算法，加密和解密构成一个密钥对
 *
 * @author Bigbird
 */
public interface IRsaCryptoService extends ICryptoService {

    /**
     * 生成秘钥
     *
     * @param rsaKeyBits 生成密钥的位数
     * @return 私钥公钥对象
     */
    KeyPairBO createKeyPair(RsaKeyBitsEnum rsaKeyBits);
}
