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
package wang.bigbird.domain.framework.data.oss.service.base.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wang.bigbird.domain.framework.data.oss.config.property.OssProperties;
import wang.bigbird.domain.framework.data.oss.context.OssHandlerContextRegister;
import wang.bigbird.domain.framework.data.oss.support.handler.IOssHandler;
import wang.bigbird.domain.framework.data.oss.service.base.IOssPersistenceService;

import java.io.InputStream;

/**
 * 云对象存储服务
 *
 * @author Bigbird
 */
@Service
public class OssPersistenceServiceImpl implements IOssPersistenceService {

    @Autowired
    private OssProperties ossProperties;

    @Autowired
    private OssHandlerContextRegister ossHandlerContextRegister;

    @Override
    public String storeFile(String bucketName, String localPath, String remotePath) {
        IOssHandler ossHandler = ossHandlerContextRegister.getOssHandler(ossProperties.getType());
        return ossHandler.storeFile(bucketName, localPath, remotePath);
    }

    @Override
    public String storeFileFromStream(String bucketName, InputStream inputStream, String fileName, long fileSize, String remotePath) {
        IOssHandler ossHandler = ossHandlerContextRegister.getOssHandler(ossProperties.getType());
        return ossHandler.storeFileFromStream(bucketName, inputStream, fileName, fileSize, remotePath);
    }

    @Override
    public boolean downloadFile(String bucketName, String remotePath, String localPath) {
        IOssHandler ossHandler = ossHandlerContextRegister.getOssHandler(ossProperties.getType());
        return ossHandler.downloadFile(bucketName, remotePath, localPath);
    }

    @Override
    public void copyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) throws Exception {
        IOssHandler ossHandler = ossHandlerContextRegister.getOssHandler(ossProperties.getType());
        ossHandler.copyFile(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
    }

    @Override
    public String statFile(String bucketName, String remotePath) {
        IOssHandler ossHandler = ossHandlerContextRegister.getOssHandler(ossProperties.getType());
        return ossHandler.statFile(bucketName, remotePath);
    }

    @Override
    public boolean deleteFile(String bucketName, String remotePath) {
        IOssHandler ossHandler = ossHandlerContextRegister.getOssHandler(ossProperties.getType());
        return ossHandler.deleteFile(bucketName, remotePath);
    }

    @Override
    public InputStream loadFileInputStream(String bucketName, String remotePath) throws Exception {
        IOssHandler ossHandler = ossHandlerContextRegister.getOssHandler(ossProperties.getType());
        return ossHandler.loadFileInputStream(bucketName, remotePath);
    }

    @Override
    public String generatePresignedUrl(String bucketName, String remotePath, int expireSeconds) {
        IOssHandler ossHandler = ossHandlerContextRegister.getOssHandler(ossProperties.getType());
        return ossHandler.generatePresignedUrl(bucketName, remotePath, expireSeconds);
    }

}
