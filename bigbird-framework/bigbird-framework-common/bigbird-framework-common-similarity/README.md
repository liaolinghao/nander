# 相似度计算组件

本组件用于文本相似度计算，提供如下类型服务：

数字签名：MD5、SHA1、SHA_256、SHA_512、SM3

数字加解密：简单加解密、对称加解密AES、非对称加解密RSA、国密加解密SM4、数字信封加解密

```
// 签名服务
@Autowired
private ISignService signService;

// 简单加密解密服务
@Autowired
private ISimpleCryptoService simpleCryptoService;

// AES对称加密解密服务
@Autowired
private IAesCryptoService aesCryptoService;

// RSA非对称加密解密服务
@Autowired
private IRsaCryptoService rsaCryptoService;

// 国密SM4加密解密服务
@Autowired
private ISm4CryptoService sm4CryptoService;

// 数字信封加密解密服务
@Autowired
private IEnvelopeCryptoService envelopeCryptoService;
```

## 配置

本组件融合各种类型的加解密算法配置，具体如下：

```
bigbird:
  common:
    crypto:
      rsa:
        encryptKey: # RSA加密公钥
        decryptKey: # RSA解密私钥
        converter: # 可配置字节数组到字符串转换方式：HEX_LOWER，HEX_UPPER，BASE64，默认BASE64
      aes:
        encryptKey: # AES加密密钥
        decryptKey: # AES解密密钥
        key:
          cache-enable: # true或者false，是否缓存密钥，默认不缓存
        model: # AES加密模式，可配置CBC或者ECB，默认ECB
        converter: # 可配置字节数组到字符串转换方式：HEX_LOWER，HEX_UPPER，BASE64，默认BASE64
      sm4:
        converter: # 可配置字节数组到字符串转换方式：HEX_LOWER，HEX_UPPER，BASE64，默认BASE64
      sign: # 签名服务
        type: # 可配置签名算法类型：MD5，SHA，SHA-256，SHA-512，SM3，默认MD5
        converter: # 可配置字节数组到字符串转换方式：HEX_LOWER，HEX_UPPER，BASE64，默认HEX_LOWER
```
