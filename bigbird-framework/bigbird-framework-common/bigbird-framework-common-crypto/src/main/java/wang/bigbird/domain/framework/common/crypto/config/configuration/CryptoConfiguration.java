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
package wang.bigbird.domain.framework.common.crypto.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import wang.bigbird.domain.framework.common.crypto.base.enums.AesKeyLengthEnum;
import wang.bigbird.domain.framework.common.crypto.base.enums.AesModelEnum;
import wang.bigbird.domain.framework.common.crypto.base.enums.Byte2StringTypeEnum;
import wang.bigbird.domain.framework.common.crypto.base.enums.SignTypeEnum;
import wang.bigbird.domain.framework.common.crypto.base.tool.IBytesConverter;
import wang.bigbird.domain.framework.common.crypto.config.property.KeyProperties;
import wang.bigbird.domain.framework.common.crypto.service.base.*;
import wang.bigbird.domain.framework.common.crypto.service.base.impl.*;

import javax.annotation.PostConstruct;
import java.security.Security;

/**
 * 加解密器配置
 *
 * @author Bigbird
 */
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.common.crypto")
@Configuration
public class CryptoConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init crypto framework.");
    }

    @ConfigurationProperties(prefix = "bigbird.common.crypto.rsa", ignoreInvalidFields = true)
    @Bean
    public KeyProperties rsaKeyProperties() {
        return new KeyProperties();
    }

    @ConfigurationProperties(prefix = "bigbird.common.crypto.aes", ignoreInvalidFields = true)
    @Bean
    public KeyProperties aesKeyProperties() {
        return new KeyProperties();
    }

    /**
     * 扩展加解密类型
     */
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 获取RSA key的服务
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(name = "rsaKeyHolder")
    public IKeyHolderService rsaKeyHolder(@Qualifier("rsaKeyProperties") KeyProperties keyProperties) {
        KeyHolderServiceImpl keyHolder = new KeyHolderServiceImpl();
        BeanUtils.copyProperties(keyProperties, keyHolder);
        return keyHolder;
    }

    /**
     * 获取AES key的服务
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(name = "aesKeyHolder")
    public IKeyHolderService aesKeyHolder(@Qualifier("aesKeyProperties") KeyProperties keyProperties) {
        KeyHolderServiceImpl keyHolder = new KeyHolderServiceImpl();
        BeanUtils.copyProperties(keyProperties, keyHolder);
        return keyHolder;
    }

    /**
     * 签名服务
     *
     * @param signType  签名算法采用的加密方式
     * @param converter 字节数组转换为字符串的转换方式
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(ISignService.class)
    public ISignService signService(@Value("${bigbird.common.crypto.sign.type:MD5}") SignTypeEnum signType,
                                    @Value("${bigbird.common.crypto.sign.converter:HEX_LOWER}") Byte2StringTypeEnum converter) {
        return new SignServiceImpl()
                .setSignType(signType)
                .setBytesConverter(IBytesConverter.getInstance(converter));
    }

    /**
     * 简单加密解密器
     *
     * @return 简单加密解密器
     */
    @Bean
    @ConditionalOnMissingBean(ISimpleCryptoService.class)
    public ISimpleCryptoService simpleCryptoService() {
        return new SimpleCryptoServiceImpl();
    }

    /**
     * AES对称加密解密器
     *
     * @param cacheEnable 是否缓存key
     * @param model       AES加密模式
     * @param converter   字节码转换器类型
     * @return AES对称加密解密器
     */
    @Bean
    @ConditionalOnMissingBean(IAesCryptoService.class)
    public IAesCryptoService aesCryptoService(@Value("${bigbird.common.crypto.aes.key.cache-enable:false}") Boolean cacheEnable, @Value("${bigbird.common.crypto.aes.model:ECB}") AesModelEnum model,
                                              @Value("${bigbird.common.crypto.aes.converter:BASE64}") Byte2StringTypeEnum converter) {
        return new AesCryptoServiceImpl().setCacheEnable(cacheEnable)
                .setBytesConverter(IBytesConverter.getInstance(converter))
                .setModel(model.name());
    }

    /**
     * RSA非对称加密解密器
     *
     * @param converter 字节码转换器类型
     * @return RSA非对称加密解密器
     */
    @Bean
    @ConditionalOnMissingBean(IRsaCryptoService.class)
    public IRsaCryptoService rsaCryptoService(
            @Value("${bigbird.common.crypto.rsa.converter:BASE64}") Byte2StringTypeEnum converter) {
        return new RsaCryptoServiceImpl().setBytesConverter(IBytesConverter.getInstance(converter));
    }

    /**
     * 国密SM4加密解密器
     *
     * @param converter 字节码转换器类型
     * @return 国密SM4加密解密器
     */
    @Bean
    @ConditionalOnMissingBean(ISm4CryptoService.class)
    public ISm4CryptoService sm4CryptoService(
            @Value("${bigbird.common.crypto.sm4.converter:BASE64}") Byte2StringTypeEnum converter) {
        return new Sm4CryptoServiceImpl().setBytesConverter(IBytesConverter.getInstance(converter));
    }

    /**
     * 数字信封加密解密器
     *
     * @param aesCryptoService 对称加密器，用于加密正文
     * @param rsaCryptoService 非对称加密器，用于加密对称密钥
     * @return 数字信封加密解密器
     */
    @Bean
    @ConditionalOnMissingBean(IEnvelopeCryptoService.class)
    public IEnvelopeCryptoService envelopCryptoService(@Autowired @Qualifier("aesCryptoService") IAesCryptoService aesCryptoService,
                                                          @Autowired @Qualifier("rsaCryptoService") IRsaCryptoService rsaCryptoService) {
        return new EnvelopeCryptoServiceImpl().setRsaCryptoService(rsaCryptoService)
                .setAesCryptoService(aesCryptoService)
                .setKeyLength(AesKeyLengthEnum.BIT_128);
    }

}
