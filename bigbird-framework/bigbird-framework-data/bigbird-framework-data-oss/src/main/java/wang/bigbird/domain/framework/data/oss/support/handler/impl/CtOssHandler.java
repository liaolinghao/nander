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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;
import wang.bigbird.domain.framework.data.oss.config.property.OssProperties;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * 天翼云云对象存储服务
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class CtOssHandler extends AbstractOssHandler {

    @Autowired
    private OssProperties ossProperties;

    @Autowired(required = false)
    private AmazonS3 oosClient;

    @Override
    public OssTypeEnum getOssType() {
        return OssTypeEnum.CT;
    }

    @Override
    public String doGeneratePresignedUrl(String bucketName, String remotePath, int expireSeconds) {
        GeneratePresignedUrlRequest shareUrlRequest = new GeneratePresignedUrlRequest(bucketName, remotePath);
        Date expire = DateUtils.addSeconds(new Date(), expireSeconds);
        shareUrlRequest.setExpiration(expire);
        URL url = oosClient.generatePresignedUrl(shareUrlRequest);
        return url.toString();
    }

    @Override
    protected void prepareBucketAndPath(String bucketName, String remotePath) {
        if (!oosClient.doesBucketExistV2(bucketName)) {
            oosClient.createBucket(bucketName);
        }
    }

    @Override
    protected String uploadLocalFileToRemote(String bucketName, String remotePath, InputStream fileInputStream, File localFile) {
        oosClient.putObject(bucketName, remotePath, localFile);
        return StringUtils.joinStr(ossProperties.getCt().getProtocol(), CommonConstants.PROTOCOL_DELIMITER, bucketName, CommonConstants.DOT
                , ossProperties.getCt().getEndpoint(), CommonConstants.SLASH, remotePath);
    }

    @Override
    protected String uploadStreamToRemote(String bucketName, String remotePath, InputStream inputStream, String fileName, long fileSize) {
        ObjectMetadata metadata = new ObjectMetadata();
        Map<String, String> userMetadata = Map.of("name", fileName, "size", String.valueOf(fileSize));
        metadata.setUserMetadata(userMetadata);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, remotePath, inputStream, metadata);
        oosClient.putObject(putObjectRequest);
        return StringUtils.joinStr(ossProperties.getCt().getProtocol(), CommonConstants.PROTOCOL_DELIMITER, bucketName, CommonConstants.DOT
                , ossProperties.getCt().getEndpoint(), CommonConstants.SLASH, remotePath);
    }

    @Override
    protected InputStream loadRemoteFileInputStream(String bucketName, String remotePath) {
        return oosClient.getObject(
                new GetObjectRequest(bucketName, remotePath)).getObjectContent();
    }

    @Override
    public void doCopyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
        oosClient.copyObject(copyObjectRequest);
    }

    @Override
    protected void loadMetadata(String bucketName, String remotePath, Map info) {
        S3Object object = oosClient.getObject(bucketName, remotePath);
        ObjectMetadata objectMetadata = object.getObjectMetadata();
        info.put("size", objectMetadata.getContentLength());
        info.put("etag", objectMetadata.getETag());
        info.put("lastModified", objectMetadata.getLastModified());
        info.put("access_url", StringUtils.joinStr(ossProperties.getCt().getProtocol(), CommonConstants.PROTOCOL_DELIMITER, bucketName, CommonConstants.DOT
                , ossProperties.getCt().getEndpoint(), CommonConstants.SLASH, remotePath));
    }

    @Override
    protected void deleteRemoteFile(String bucketName, String remotePath) {
        oosClient.deleteObject(bucketName, remotePath);
    }

}
