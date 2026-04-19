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

import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.MetaData;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.domain.upload.FastFile;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dfs分布式文件存储服务
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class DfsOssHandler extends AbstractOssHandler {

    @Autowired(required = false)
    private FastFileStorageClient storageClient;

    @Autowired(required = false)
    private FdfsWebServer fdfsWebServer;

    @Override
    public OssTypeEnum getOssType() {
        return OssTypeEnum.DFS;
    }

    @Override
    public String doGeneratePresignedUrl(String bucketName, String remotePath, int expireSeconds) {
        // TODO
        return null;
    }

    @Override
    protected void prepareBucketAndPath(String bucketName, String remotePath) {

    }

    @Override
    protected String uploadLocalFileToRemote(String bucketName, String remotePath, InputStream fileInputStream, File localFile) throws Exception {
        Set<MetaData> metaDataSet = new HashSet<>();
        metaDataSet.add(new MetaData("remotePath", remotePath));
        metaDataSet.add(new MetaData("size", String.valueOf(localFile.length())));
        metaDataSet.add(new MetaData("etag", FileUtils.getFileMd5(localFile)));
        metaDataSet.add(new MetaData("lastModified", String.valueOf(System.currentTimeMillis())));
        FastFile fastFile = new FastFile.Builder()
                .withFile(fileInputStream, localFile.length(), FileUtils.getSuffix(localFile))
                .withMetaData(metaDataSet)
                .toGroup(bucketName)
                .build();
        StorePath storePath = storageClient.uploadFile(fastFile);
        return StringUtils.joinStr(fdfsWebServer.getWebServerUrl() + CommonConstants.SLASH + storePath.getFullPath());
    }

    @Override
    protected String uploadStreamToRemote(String bucketName, String remotePath, InputStream inputStream, String fileName, long fileSize) throws Exception {
        // TODO
        return null;
    }

    @Override
    protected InputStream loadRemoteFileInputStream(String bucketName, String remotePath) {
        return storageClient.downloadFile(
                bucketName,
                remotePath,
                inputStream -> inputStream
        );
    }

    @Override
    public void doCopyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        // TODO
    }

    @Override
    protected void loadMetadata(String bucketName, String remotePath, Map info) throws Exception {
        Set<MetaData> metaDataSet = storageClient.getMetadata(bucketName, remotePath);
        for (MetaData metaData : metaDataSet) {
            switch (metaData.getName()) {
                case "size":
                case "etag":
                case "lastModified":
                    info.put(metaData.getName(), metaData.getValue());
                    break;
                default:
            }
        }
        info.put("access_url", StringUtils.joinStr(fdfsWebServer.getWebServerUrl() + CommonConstants.SLASH + bucketName + CommonConstants.SLASH + remotePath));
    }

    @Override
    protected void deleteRemoteFile(String bucketName, String remotePath) {
        storageClient.deleteFile(bucketName, remotePath);
    }

}
