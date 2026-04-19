<p align="center">
    <a href="https://domain.bigbird.wang"><img src="https://domain.bigbird.wang/static/images/nander.svg" width="45%"></a>
</p>
<p align="center">
    <strong>A development framework that maintains technical standardization.</strong>
</p>
<p align="center">
    👉 <a href="https://domain.bigbird.wang">https://domain.bigbird.wang</a> 👈
</p>
<p align="center">
    <a target="_blank" href="https://license.coscl.org.cn/MulanPSL2">
        <img src="https://img.shields.io/:license-MulanPSL2-blue.svg" />
    </a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/downloads/#java11">
        <img src="https://img.shields.io/badge/JDK-11+-green.svg" />
    </a>
</p>
<p align="center">
    <a href="https://qm.qq.com/cgi-bin/qm/qr?k=vLkJq9aGVmS0NL4Loo-dZT_NbqiUqBk0&jump_from=webapi&authKey=XYuHJd7x9mF4Hki62SL81gsfuk17fqjdzHVuW9Ok7EfmVWl5JOqJCzCnk4ZOva4F">
    <img alt="" src="https://img.shields.io/badge/QQ%E7%BE%A4%E2%91%A7-643878358-orange"/></a>
</p>

-------------------------------------------------------------------------------

[**🌎English Documentation**](README-EN.md)

-------------------------------------------------------------------------------

## 📚简介

JAVA微服务构件库，涵盖了JAVA微服务架构中涉及的各类技术，它将微服务研发过程中涉及的公共功能按照复杂度划分为不同的三个维度并按照功能类别分别予以组织。

-------------------------------------------------------------------------------

## 🛠️ 成员

| 模块                |     介绍                                                                          |
|--------------------|---------------------------------------------------------------------------------- |
| bigbird-framework-core（CORE）          |     基础工具包，包含：字符串处理、数据格式处理、排序器、文件处理、数值处理、时间处理、编码处理、图像处理、分页器、永动机任务执行器等。|
| bigbird-framework-common（COMMON）      |     通用组件库，包含：加解密组件、GIS组件、禁用词检测组件、日志组件、脱敏组件。|
| bigbird-framework-data（DATA）          |     数据构件，包含：Elasticsearch构件、Kafka构件、Mybatisplus构件、Mongodb构件、Rabbitmq构件、Redis构件、Rocketmq构件、Zookeeper构件、OSS构件。|
| bigbird-framework-cache（CACHE）        |     缓存构件，包含：分布式缓存、本地缓存。|
| bigbird-framework-distributedlock（DL） |     分布式锁构件，包含：zk锁、redis锁。|
| bigbird-framework-document（DOC）       |     文档构件，包含：EXCEL制作、PDF制作、WORD制作、HTML制作。|
| bigbird-framework-message（MESSAGE）    |     消息构件，包含：短信、邮件、钉钉、企业微信。|
| bigbird-framework-id（ID）              |     ID构件，包含：twitter-snowflake、leaf-segment、baidu-uid。|
| bigbird-framework-server（SERVER）      |     服务框架构件，包含：提供通用web接口的WEB框架、提供接口安全防护的WEB框架，提供JWT认证的WEB框架、提供频控的WEB框架、提供验证码的WEB框架、提供外部接口调用的WEB框架、提供动态作业调度的WEB框架、提供WS服务框架。|
| bigbird-framework-plugin（PLUGIN）      |     插件库，包含：mybatis-plus代码生成插件。|
| bigbird-framework-parent               |     统一定义上述构件涉及的公共依赖。|
| bigbird-framework-dependencies         |     统一定义上述构件的依赖版本。|

-------------------------------------------------------------------------------

## 📝 文档

[📘 Nander文库](https://nander.bigbird.wang/)

[📙 参考API](https://domain.bigbird.wang/static/apidocs/index.html)

[🎬 视频介绍](https://player.bilibili.com/player.html?bvid=BV1Kw411T7Rz)

-------------------------------------------------------------------------------

## 🏗️ 添砖加瓦

### 🎋 分支说明

JAVA微服务构件库源码分为两个分支，功能如下：

| 分支       | 作用                                                          |
|-----------|---------------------------------------------------------------|
| master    | 生产稳定分支，受保护，禁止直接提交，仅通过 PR 合并。|
| develop   | 开发集成分支，所有新功能在此分支迭代。|
|           | 所有提交必须通过 Pull Request 进行，经审核后方可合并。|
