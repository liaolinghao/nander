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

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.SimplifiedObjectMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * 阿里云云对象存储服务
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class AliyunOssHandler extends AbstractOssHandler {

    @Autowired(required = false)
    private OSSClient ossClient;

    @Override
    public OssTypeEnum getOssType() {
        return OssTypeEnum.ALIYUN;
    }

    @Override
    public String doGeneratePresignedUrl(String bucketName, String remotePath, int expireSeconds) {
        // TODO
        return null;
    }

    @Override
    protected void prepareBucketAndPath(String bucketName, String remotePath) {
        if (!ossClient.doesBucketExist(bucketName)) {
            CreateBucketRequest createBucketRequest = new CreateBucketRequest(
                    bucketName);
            createBucketRequest
                    .setCannedACL(CannedAccessControlList.PublicRead);
            ossClient.createBucket(createBucketRequest);
        }
    }

    @Override
    protected String uploadLocalFileToRemote(String bucketName, String remotePath, InputStream fileInputStream, File localFile) {
        ossClient.putObject(bucketName, remotePath, localFile);
        return StringUtils.joinStr(CommonConstants.HTTP_PROTOCOL, CommonConstants.PROTOCOL_DELIMITER, bucketName, CommonConstants.DOT
                , ossClient.getEndpoint(), CommonConstants.SLASH, remotePath);
    }

    @Override
    protected String uploadStreamToRemote(String bucketName, String remotePath, InputStream inputStream, String fileName, long fileSize) throws Exception {
        // TODO
        return null;
    }

    @Override
    protected InputStream loadRemoteFileInputStream(String bucketName, String remotePath) {
        return ossClient.getObject(
                new GetObjectRequest(bucketName,
                        remotePath)).getObjectContent();
    }

    @Override
    public void doCopyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        // TODO
    }

    @Override
    protected void loadMetadata(String bucketName, String remotePath, Map info) {
        SimplifiedObjectMeta objectMeta = ossClient
                .getSimplifiedObjectMeta(bucketName,
                        remotePath);
        info.put("size", objectMeta.getSize());
        info.put("etag", objectMeta.getETag());
        info.put("lastModified", objectMeta.getLastModified());
        info.put("access_url", StringUtils.joinStr(CommonConstants.HTTP_PROTOCOL, CommonConstants.PROTOCOL_DELIMITER, bucketName, CommonConstants.DOT
                , ossClient.getEndpoint(), CommonConstants.SLASH, remotePath));
    }

    @Override
    protected void deleteRemoteFile(String bucketName, String remotePath) {
        ossClient.deleteObject(bucketName, remotePath);
    }

}
