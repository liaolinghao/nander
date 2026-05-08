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
package wang.bigbird.domain.framework.data.oss.support.handler.impl;

import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;
import wang.bigbird.domain.framework.data.oss.config.property.OssProperties;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 华为云云对象存储服务
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class HuaweiOssHandler extends AbstractOssHandler {

    @Autowired
    private OssProperties ossProperties;

    @Autowired(required = false)
    private ObsClient obsClient;

    @Override
    public OssTypeEnum getOssType() {
        return OssTypeEnum.HUAWEI;
    }

    @Override
    public String doGeneratePresignedUrl(String bucketName, String remotePath, int expireSeconds) {
        TemporarySignatureRequest request = new TemporarySignatureRequest();
        request.setBucketName(bucketName);
        request.setObjectKey(remotePath);
        request.setExpires(expireSeconds);
        TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
        return response.getSignedUrl();
    }

    @Override
    protected void prepareBucketAndPath(String bucketName, String remotePath) {
        if (!doesBucketExist(bucketName)) {
            // 创建桶实例
            S3Bucket s3Bucket = new S3Bucket();
            String location = "cn-south-1";
            // 设置桶的元数据(可选)
            Map<String, Object> metadata = new HashMap<>(CollectionUtils.initialMapCapacity(2));
            metadata.put("x-amz-acl", "public-read");
            // 需要OBS服务端打开温冷存储开关
            // 设置桶的默认存储类型，STANDARD（标准存储）、STANDARD_IA（近线存储）、GLACIER（归档存储）
            metadata.put("x-default-storage-class", "STANDARD");
            s3Bucket.setBucketName(bucketName);
            s3Bucket.setLocation(location);
            s3Bucket.setMetadata(metadata);
            obsClient.createBucket(s3Bucket);
        }
    }

    @Override
    protected String uploadLocalFileToRemote(String bucketName, String remotePath, InputStream fileInputStream, File localFile) {
        // 设置上传对象的元数据
        ObjectMetadata metadata = new ObjectMetadata();
        // 设置头信息中的文件长度
        metadata.setContentLength(localFile.length());
        // 设置公共读权限，保证匿名用户可以访问
        Map<String, Object> meta = new HashMap<>(CollectionUtils.initialMapCapacity(1));
        meta.put("x-amz-acl", "public-read");
        metadata.setMetadata(meta);
        // 封装上传对象的请求
        PutObjectRequest request = new PutObjectRequest();
        request.setBucketName(bucketName);
        request.setInput(fileInputStream);
        request.setMetadata(metadata);
        request.setObjectKey(remotePath);
        // 调用putObject接口上传对象
        obsClient.putObject(request);
        return StringUtils.joinStr(ossProperties.getHuawei().getProtocol(), CommonConstants.PROTOCOL_DELIMITER, ossProperties.getHuawei().getEndpoint(), CommonConstants.SLASH
                , bucketName, CommonConstants.SLASH, remotePath);
    }

    @Override
    protected String uploadStreamToRemote(String bucketName, String remotePath, InputStream inputStream, String fileName, long fileSize) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.addUserMetadata("name", fileName);
        metadata.addUserMetadata("size", String.valueOf(fileSize));
        metadata.setContentLength(fileSize);
        PutObjectRequest putObjectRequest = new PutObjectRequest();
        putObjectRequest.setBucketName(bucketName);
        putObjectRequest.setObjectKey(remotePath);
        putObjectRequest.setInput(inputStream);
        putObjectRequest.setMetadata(metadata);
        obsClient.putObject(putObjectRequest);
        return StringUtils.joinStr(ossProperties.getHuawei().getProtocol(), CommonConstants.PROTOCOL_DELIMITER, ossProperties.getHuawei().getEndpoint(), CommonConstants.SLASH
                , bucketName, CommonConstants.SLASH, remotePath);
    }

    @Override
    protected InputStream loadRemoteFileInputStream(String bucketName, String remotePath) {
        // 如果对象所属的桶开启了多版本状态，则可以设置versionId指定对象所属的版本
        S3Object s3 = obsClient.getObject(
                bucketName, remotePath, null);
        return s3.getObjectContent();
    }

    @Override
    public void doCopyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
        obsClient.copyObject(copyObjectRequest);
    }

    @Override
    protected void loadMetadata(String bucketName, String remotePath, Map info) {
        // 如果对象所属的桶开启了多版本状态，则可以设置versionId指定对象所属的版本
        ObjectMetadata metadata = obsClient.getObjectMetadata(
                bucketName, remotePath, null);
        info.put("size", metadata.getContentLength());
        info.put("etag", metadata.getEtag());
        info.put("lastModified", metadata.getLastModified());
        info.put("access_url", StringUtils.joinStr(ossProperties.getHuawei().getProtocol(), CommonConstants.PROTOCOL_DELIMITER, ossProperties.getHuawei().getEndpoint(), CommonConstants.SLASH
                , bucketName, CommonConstants.SLASH, remotePath));
    }

    @Override
    protected void deleteRemoteFile(String bucketName, String remotePath) {
        obsClient.deleteObject(bucketName,
                remotePath, null);
    }

    private boolean doesBucketExist(String bucketName) throws ObsException {
        List<S3Bucket> bucketList = obsClient.listBuckets();
        Iterator<S3Bucket> iterator = bucketList.iterator();
        while (iterator.hasNext()) {
            S3Bucket bucket = iterator.next();
            if (bucket.getBucketName().equalsIgnoreCase(bucketName)) {
                return true;
            }
        }
        return false;
    }

}
