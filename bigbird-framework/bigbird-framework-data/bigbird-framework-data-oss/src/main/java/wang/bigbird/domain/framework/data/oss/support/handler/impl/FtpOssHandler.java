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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * Ftp远程文件存储服务
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class FtpOssHandler extends AbstractOssHandler {

    @Autowired(required = false)
    private ChannelSftp ftpClient;

    @Override
    public OssTypeEnum getOssType() {
        return OssTypeEnum.FTP;
    }

    @Override
    public String doGeneratePresignedUrl(String bucketName, String remotePath, int expireSeconds) {
        // TODO
        return null;
    }

    @Override
    protected void prepareBucketAndPath(String bucketName, String remotePath) throws Exception {
        for (String part : remotePath.split(CommonConstants.SLASH)) {
            if (part.contains(CommonConstants.DOT)) {
                break;
            }
            if (ftpClient.ls(part).isEmpty()) {
                ftpClient.mkdir(part);
                ftpClient.cd(part);
            }
        }
    }

    @Override
    protected String uploadLocalFileToRemote(String bucketName, String remotePath, InputStream fileInputStream, File localFile) throws Exception {
        ftpClient.put(fileInputStream, remotePath);
        return remotePath;
    }

    @Override
    protected String uploadStreamToRemote(String bucketName, String remotePath, InputStream inputStream, String fileName, long fileSize) throws Exception {
        // TODO
        return null;
    }

    @Override
    protected InputStream loadRemoteFileInputStream(String bucketName, String remotePath) throws Exception {
        return ftpClient.get(remotePath);
    }

    @Override
    public void doCopyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        // TODO
    }

    @Override
    protected void loadMetadata(String bucketName, String remotePath, Map info) throws Exception {
        SftpATTRS sftpATTRS = ftpClient.lstat(remotePath);
        info.put("size", sftpATTRS.getSize());
        int mTime = sftpATTRS.getMTime();
        Date lastModified = new Date(mTime * 1000L);
        info.put("lastModified", lastModified);
        info.put("access_url", remotePath);
    }

    @Override
    protected void deleteRemoteFile(String bucketName, String remotePath) throws Exception {
        ftpClient.rm(remotePath);
    }

}
