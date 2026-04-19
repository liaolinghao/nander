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
package wang.bigbird.domain.framework.server.web.core.support.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.common.crypto.service.base.*;
import wang.bigbird.domain.framework.server.web.core.config.property.WebProperties;

/**
 * 安全处理器
 *
 * @author Bigbird
 */
@Component
public class SecurityHandler {

    @Autowired
    private WebProperties webProperties;

    @Autowired
    private ISimpleCryptoService simpleCryptoService;
    @Autowired
    private IAesCryptoService aesCryptoService;
    @Autowired
    private IRsaCryptoService rsaCryptoService;
    @Autowired
    private ISm4CryptoService sm4CryptoService;
    @Autowired
    private IEnvelopeCryptoService envelopeCryptoService;

    /**
     * 将明文文本加密返回
     *
     * @param text 明文文本
     * @return 密文
     */
    public String encrypt(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        switch (webProperties.getCrypto()) {
            case AES:
                return aesCryptoService.encrypt2String(text, webProperties.getEncryptKey(), null);
            case RSA:
                return rsaCryptoService.encrypt2String(text, webProperties.getEncryptKey(), null);
            case SM4:
                return sm4CryptoService.encrypt2String(text, webProperties.getEncryptKey(), null);
            case ENVELOPE:
                return envelopeCryptoService.encrypt2String(text, webProperties.getEncryptKey(), null);
            default:
                return simpleCryptoService.encrypt2String(text, null, null);
        }
    }

    /**
     * 将密文文本解密返回
     *
     * @param text 密文文本
     * @return 明文
     */
    public String decrypt(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        switch (webProperties.getCrypto()) {
            case AES:
                return aesCryptoService.decrypt2String(text, webProperties.getDecryptKey(), null);
            case RSA:
                return rsaCryptoService.decrypt2String(text, webProperties.getDecryptKey(), null);
            case SM4:
                return sm4CryptoService.decrypt2String(text, webProperties.getDecryptKey(), null);
            case ENVELOPE:
                return envelopeCryptoService.decrypt2String(text, webProperties.getDecryptKey(), null);
            default:
                return simpleCryptoService.decrypt2String(text, null, null);
        }
    }

}
