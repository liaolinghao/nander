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

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/**
 * MinIO云对象存储服务
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class MinioOssHandler extends AbstractOssHandler {

    @Autowired(required = false)
    private MinioClient minioClient;

    @Override
    public OssTypeEnum getOssType() {
        return OssTypeEnum.MINIO;
    }

    @Override
    public String doGeneratePresignedUrl(String bucketName, String remotePath, int expireSeconds) {
        // TODO
        return null;
    }

    @Override
    protected void prepareBucketAndPath(String bucketName, String remotePath) throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        }
    }

    @Override
    protected String uploadLocalFileToRemote(String bucketName, String remotePath, InputStream fileInputStream, File localFile) throws Exception {
        minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket(bucketName)
                .object(remotePath)
                .filename(localFile.getAbsolutePath())
                .build());
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs
                .builder()
                .bucket(bucketName)
                .object(remotePath).method(Method.GET).build();
        return minioClient.getPresignedObjectUrl(build);
    }

    @Override
    protected String uploadStreamToRemote(String bucketName, String remotePath, InputStream inputStream, String fileName, long fileSize) throws Exception {
        // TODO
        return null;
    }

    @Override
    protected InputStream loadRemoteFileInputStream(String bucketName, String remotePath) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(remotePath)
                        .build()
        );
    }

    @Override
    public void doCopyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        // TODO
    }

    @Override
    protected void loadMetadata(String bucketName, String remotePath, Map info) throws Exception {
        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(remotePath)
                        .build());
        GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs
                .builder()
                .bucket(bucketName)
                .object(remotePath).method(Method.GET).build();
        info.put("size", stat.size());
        info.put("etag", stat.etag());
        info.put("lastModified", stat.lastModified());
        info.put("access_url", minioClient.getPresignedObjectUrl(build));
    }

    @Override
    protected void deleteRemoteFile(String bucketName, String remotePath) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(remotePath)
                .build());
    }
}
