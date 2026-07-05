# 相似度计算组件

本组件用于文本相似度计算，提供如下类型服务：

词语相似度计算：词林相似度、知网概念相似度、拼音相似度、字面相似度

短语相似度计算：莱文斯坦归一化相似度、

句子相似度计算：

段落相似度计算：

```
// 词语相似度计算服务
@Autowired
private IWordSimilarityService wordSimilarityService;




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
