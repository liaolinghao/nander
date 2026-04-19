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

import cn.hutool.json.JSONObject;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.request.DelFileRequest;
import com.qcloud.cos.request.GetFileInputStreamRequest;
import com.qcloud.cos.request.StatFileRequest;
import com.qcloud.cos.request.UploadFileRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.data.oss.config.property.OssProperties;
import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * 腾讯云云对象存储服务
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class QcloudOssHandler extends AbstractOssHandler {

    @Autowired
    private OssProperties ossProperties;

    @Autowired(required = false)
    private COSClient cosClient;

    @Override
    public OssTypeEnum getOssType() {
        return OssTypeEnum.QCLOUD;
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
        UploadFileRequest uploadFileRequest = new UploadFileRequest(
                bucketName, remotePath, localFile.getAbsolutePath());
        String json = cosClient.uploadFile(uploadFileRequest);
        JSONObject res = new JSONObject(json);
        if (res.getInt(RESPONSE_CODE) == 0) {
            return res.getJSONObject(RESPONSE_DATA).getStr(ACCESS_URL);
        }
        throw new Exception(res.getStr(RESPONSE_MESSAGE));
    }

    @Override
    protected String uploadStreamToRemote(String bucketName, String remotePath, InputStream inputStream, String fileName, long fileSize) throws Exception {
        // TODO
        return null;
    }

    @Override
    protected InputStream loadRemoteFileInputStream(String bucketName, String remotePath) throws Exception {
        GetFileInputStreamRequest getFileInputStreamRequest = new GetFileInputStreamRequest(bucketName, remotePath);
        getFileInputStreamRequest.setUseCDN(false);
        // 针对开启防止盗链的文件要设置referer
        getFileInputStreamRequest.setReferer(ossProperties.getQcloud().getReferer());
        return cosClient.getFileInputStream(getFileInputStreamRequest);
    }

    @Override
    public void doCopyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) {
        // TODO
    }

    @Override
    protected void loadMetadata(String bucketName, String remotePath, Map info) throws Exception {
        StatFileRequest statFileRequest = new StatFileRequest(
                bucketName, remotePath);
        String json = cosClient.statFile(statFileRequest);
        JSONObject res = new JSONObject(json);
        if (res.getInt(RESPONSE_CODE) == 0) {
            JSONObject data = res.getJSONObject(RESPONSE_DATA);
            info.put("size", data.getLong("filesize"));
            info.put("etag", data.getStr("sha"));
            info.put("lastModified", new Date(data.getLong("mtime")));
            info.put("access_url", data.getStr("access_url"));
            return;
        }
        throw new Exception(res.getStr(RESPONSE_MESSAGE));
    }

    @Override
    protected void deleteRemoteFile(String bucketName, String remotePath) throws Exception {
        DelFileRequest delFileRequest = new DelFileRequest(
                bucketName, remotePath);
        String json = cosClient.delFile(delFileRequest);
        JSONObject res = new JSONObject(json);
        if (res.getInt(RESPONSE_CODE) == 0) {
            return;
        }
        throw new Exception(res.getStr(RESPONSE_MESSAGE));
    }

}
