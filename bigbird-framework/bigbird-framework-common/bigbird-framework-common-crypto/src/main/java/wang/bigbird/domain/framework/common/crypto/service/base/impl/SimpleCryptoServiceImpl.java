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

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.common.crypto.base.util.BytesUtils;
import wang.bigbird.domain.framework.common.crypto.service.base.ISimpleCryptoService;
import wang.bigbird.domain.framework.core.base.tool.Coder;

import java.nio.charset.StandardCharsets;

/**
 * 简单加密解密工具
 * <p>
 * 采用两次不同类型的编码方式实现
 *
 * @author Bigbird
 */
@Slf4j
public class SimpleCryptoServiceImpl implements ISimpleCryptoService {

    @Override
    public byte[] encrypt(byte[] data, String key, String iv) {
        BytesUtils.notEmpty(data);
        return encrypt2String(new String(data, StandardCharsets.UTF_8), key, iv).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String encrypt2String(@NonNull String text, String key, String iv) {
        return Base64Encoder.encode(Coder.convertUnicode10(text));
    }

    @Override
    public byte[] decrypt(byte[] data, String key, String iv) {
        BytesUtils.notEmpty(data);
        return decrypt2String(new String(data, StandardCharsets.UTF_8), key, iv).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String decrypt2String(@NonNull String text, String key, String iv) {
        return Coder.revertUnicode10(Base64Decoder.decodeStr(text));
    }
}
