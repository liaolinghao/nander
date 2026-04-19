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
package wang.bigbird.domain.framework.data.oss.config.configuration;

import com.aliyun.oss.OSSClient;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.github.tobato.fastdfs.FdfsClientConfig;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.sign.Credentials;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import wang.bigbird.domain.framework.core.base.util.CryptUtils;
import wang.bigbird.domain.framework.data.oss.config.property.OssProperties;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * OSS 配置
 *
 * @author Bigbird
 */
@Import(FdfsClientConfig.class)
@Slf4j
@Configuration
@ComponentScan("wang.bigbird.domain.framework.data.oss")
public class OssConfiguration {

    @PostConstruct
    public void init() {
        log.info("Init oss framework.");
    }

    @Bean
    @ConditionalOnProperty(prefix = "bigbird.data.oss", name = "type", havingValue = "minio")
    public MinioClient minioClient(OssProperties ossProperties) {
        return MinioClient.builder().endpoint(ossProperties.getMinio().getUrl())
                .credentials(CryptUtils.decrypt(ossProperties.getMinio().getAccessKey(), ossProperties.getKey()), CryptUtils.decrypt(ossProperties.getMinio().getSecretKey(), ossProperties.getKey())).build();
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "bigbird.data.oss", name = "type", havingValue = "aliyun")
    public OSSClient ossClient(OssProperties ossProperties) {
        return new OSSClient(ossProperties.getAliyun().getEndpoint(),
                CryptUtils.decrypt(ossProperties.getAliyun().getAccessKeyId(), ossProperties.getKey()),
                CryptUtils.decrypt(ossProperties.getAliyun().getAccessKeySecret(), ossProperties.getKey()));
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "bigbird.data.oss", name = "type", havingValue = "qcloud")
    public COSClient cosClient(OssProperties ossProperties) {
        // 初始化秘钥信息
        Credentials cred = new Credentials(ossProperties.getQcloud().getAppId(),
                CryptUtils.decrypt(ossProperties.getQcloud().getSecretId(), ossProperties.getKey()),
                CryptUtils.decrypt(ossProperties.getQcloud().getSecretKey(), ossProperties.getKey()));
        // 初始化客户端配置
        ClientConfig clientConfig = new ClientConfig();
        // 设置bucket所在的区域，比如华南园区：gz；华北园区：tj；华东园区：sh；
        clientConfig.setRegion("sh");
        // 初始化cosClient
        return new COSClient(clientConfig, cred);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "bigbird.data.oss", name = "type", havingValue = "huawei")
    public ObsClient obsClient(OssProperties ossProperties) {
        // 设置客户端配置信息
        ObsConfiguration config = new ObsConfiguration();
        config.setEndPoint(ossProperties.getHuawei().getEndpoint());
        config.setHttpsOnly(true);
        // 设置鉴权方式，当设置SignatString为"v4"时采用v4鉴权，其他取值均为v2鉴权，并默认使用v2鉴权
        config.setSignatString("v4");
        // HTTPS请求对应的端口，如果使用HTTP请求，端口号为80
        config.setEndpointHttpPort(443);
        config.setDisableDnsBucket(true);
        // 开启CA证书认证，建议开启
        config.setValidateCertificate(true);
        return new ObsClient(CryptUtils.decrypt(ossProperties.getHuawei().getAk(), ossProperties.getKey()),
                CryptUtils.decrypt(ossProperties.getHuawei().getSk(), ossProperties.getKey()), config);
    }


    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(prefix = "bigbird.data.oss", name = "type", havingValue = "ct")
    public AmazonS3 oosClient(OssProperties ossProperties) {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setConnectionTimeout(ossProperties.getCt().getConnectionTimeout());
        clientConfig.setSocketTimeout(ossProperties.getCt().getSocketTimeout());
        clientConfig.setProtocol(ossProperties.getCt().getProtocol().equalsIgnoreCase(Protocol.HTTPS.toString()) ? Protocol.HTTPS : Protocol.HTTP);
        AmazonS3 ossClient = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                CryptUtils.decrypt(ossProperties.getCt().getAccessId(), ossProperties.getKey()),
                                CryptUtils.decrypt(ossProperties.getCt().getAccessKey(), ossProperties.getKey()))))
                .withClientConfiguration(clientConfig)
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(ossProperties.getCt().getEndpoint(), ""))
                .disableChunkedEncoding()
                //路径样式，第三方兼容存储必须开启
                //默认关闭，生成文件可访问地址时，存储桶名作为子域名：https://{bucket-name}.s3.{region}.amazonaws.com/{object-key}
                //如果开启，生成文件可访问地址时，强制把存储桶名放在URL路径中：https://s3.{region}.amazonaws.com/{bucket-name}/{object-key}
                .enablePathStyleAccess()
                .build();
        return ossClient;
    }

    @Bean(destroyMethod = "exit")
    @ConditionalOnProperty(prefix = "bigbird.data.oss", name = "type", havingValue = "ftp")
    public ChannelSftp ftpClient(OssProperties ossProperties) throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(CryptUtils.decrypt(ossProperties.getFtp().getUsername(), ossProperties.getKey()), ossProperties.getFtp().getHost(), ossProperties.getFtp().getPort());
        session.setPassword(CryptUtils.decrypt(ossProperties.getFtp().getPassword(), ossProperties.getKey()));
        // 配置链接的属性
        Properties properties = new Properties();
        properties.setProperty("StrictHostKeyChecking", "no");
        session.setConfig(properties);
        // 进行sftp链接
        session.connect();
        // 获取通信通道
        ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        return channel;
    }

}
