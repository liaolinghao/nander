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

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.data.oss.support.handler.IOssHandler;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 云对象存储服务公共类
 *
 * @author Bigbird
 */
@Slf4j
public abstract class AbstractOssHandler implements IOssHandler {

    protected static final String RESPONSE_CODE = "code";
    protected static final String RESPONSE_MESSAGE = "message";
    protected static final String RESPONSE_DATA = "data";
    protected static final String ACCESS_URL = "access_url";

    /**
     * 默认缓冲区大小（8KB，可根据需求调整为 16KB/32KB）
     */
    protected static final int BUFFER_SIZE = 8192;

    /**
     * 处理路径
     *
     * @param path 待处理远程文件地址
     * @return 远程文件地址，格式：/xx/sample_file.txt
     */
    protected String processPathStartWithSeparator(String path) {
        path = path.replace(CommonConstants.BACKSLASH, CommonConstants.SLASH);
        if (!path.startsWith(CommonConstants.SLASH)) {
            return CommonConstants.SLASH + path;
        }
        return path;
    }

    /**
     * 处理路径
     *
     * @param path 待处理远程文件地址
     * @return 远程文件地址，格式：xx/sample_file.txt
     */
    protected String processPathStartWithOutSeparator(String path) {
        path = path.replace(CommonConstants.BACKSLASH, CommonConstants.SLASH);
        if (path.startsWith(CommonConstants.SLASH)) {
            return path.substring(1);
        }
        return path;
    }

    @Override
    public String storeFile(String bucketName, String localPath, String remotePath) {
        remotePath = processPathStartWithOutSeparator(remotePath);
        File localFile = new File(localPath);
        try (InputStream fileInputStream = new BufferedInputStream(new FileInputStream(localFile), BUFFER_SIZE)) {
            // 1、准备bucketName和remotePath
            prepareBucketAndPath(bucketName, remotePath);
            // 2、执行上传，返回文件可访问地址
            return uploadLocalFileToRemote(bucketName, remotePath, fileInputStream, localFile);
        } catch (Exception e) {
            log.error("StoreFile:", e);
        }
        return null;
    }

    @Override
    public String storeFileFromStream(String bucketName, InputStream inputStream, String fileName, long fileSize, String remotePath) {
        remotePath = processPathStartWithOutSeparator(remotePath);
        try {
            // 1、准备bucketName和remotePath
            prepareBucketAndPath(bucketName, remotePath);
            // 2、执行上传，返回文件可访问地址
            return uploadStreamToRemote(bucketName, remotePath, inputStream, fileName, fileSize);
        } catch (Exception e) {
            log.error("StoreFileFromStream:", e);
        }
        return null;
    }

    /**
     * 提前创建桶和远程路径对应的父目录
     *
     * @param bucketName 桶名称
     * @param remotePath 远程文件地址
     * @throws Exception
     */
    protected abstract void prepareBucketAndPath(String bucketName, String remotePath) throws Exception;

    /**
     * 上传本地文件到远程路径
     * 将本地文件输入流与本地文件都作为参数传入，具体实现时：
     * 如果中间件客户端上传方法支持本地文件，则优先采用本地文件，
     * 因为中间件客户端底层也是采用缓冲流实现上传，内存占用小，并且会根据文件大小自动分片，加速上传
     *
     * @param bucketName      桶名称
     * @param remotePath      远程文件地址
     * @param fileInputStream 本地文件输入流
     * @param localFile       本地文件
     * @return 文件可访问路径
     * @throws Exception
     */
    protected abstract String uploadLocalFileToRemote(String bucketName, String remotePath, InputStream fileInputStream, File localFile) throws Exception;

    /**
     * 上传输入流到远程路径
     *
     * @param bucketName 桶名称
     * @param remotePath 远程文件地址
     * @param inputStream 文件输入流
     * @param fileName 原文件名（设置元数据）
     * @param fileSize 文件大小（设置元数据，字节数）
     * @return 文件可访问路径
     * @throws Exception
     */
    protected abstract String uploadStreamToRemote(String bucketName, String remotePath, InputStream inputStream, String fileName, long fileSize) throws Exception;

    @Override
    public boolean downloadFile(String bucketName, String remotePath, String localPath) {
        File targetFile = new File(localPath);
        if (!FileUtils.newFolder(targetFile.getParentFile())) {
            return false;
        }
        remotePath = processPathStartWithOutSeparator(remotePath);
        // 流分块写入：直接从输入流写入本地文件，不缓存完整字节
        try (
                // 1、获取文件输入流
                InputStream inputStream = loadRemoteFileInputStream(
                        bucketName,
                        remotePath
                );
                // 2、本地文件输出流（覆盖写入）
                OutputStream localOutputStream = new BufferedOutputStream(
                        new FileOutputStream(targetFile),
                        BUFFER_SIZE
                )
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            // 循环读取-写入，每次最多读取 BUFFER_SIZE 字节
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                localOutputStream.write(buffer, 0, bytesRead);
            }
            // 强制刷新缓冲区，确保数据写入磁盘
            localOutputStream.flush();
            return true;
        } catch (Exception e) {
            // 下载失败时删除不完整文件
            if (targetFile.exists()) {
                boolean delete = targetFile.delete();
                if (!delete) {
                    log.info("Failed to delete file:{}", targetFile.getAbsolutePath());
                }
            }
            log.error("DownloadFile:", e);
        }
        return false;
    }

    @Override
    public InputStream loadFileInputStream(String bucketName, String remotePath) throws Exception {
        remotePath = processPathStartWithOutSeparator(remotePath);
        return loadRemoteFileInputStream(bucketName, remotePath);
    }

    /**
     * 获取远程目标文件输入流
     *
     * @param bucketName 桶名称
     * @param remotePath 远程文件地址
     * @return 文件输入流
     * @throws Exception
     */
    protected abstract InputStream loadRemoteFileInputStream(String bucketName, String remotePath) throws Exception;

    @Override
    public void copyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) throws Exception {
        sourceKey = processPathStartWithOutSeparator(sourceKey);
        destinationKey = processPathStartWithOutSeparator(destinationKey);
        doCopyFile(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
    }

    /**
     * 复制文件到新的桶
     *
     * @param sourceBucketName      源桶名
     * @param sourceKey             源对象名
     * @param destinationBucketName 目的桶名
     * @param destinationKey        目的对象名
     */
    protected abstract void doCopyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) throws Exception;

    @Override
    public String statFile(String bucketName, String remotePath) {
        remotePath = processPathStartWithOutSeparator(remotePath);
        Map info = new HashMap(CollectionUtils.initialMapCapacity(4));
        try {
            loadMetadata(bucketName, remotePath, info);
            return JsonUtils.object2Json(info);
        } catch (Exception e) {
            log.error("StatFile:", e);
        }
        return null;
    }

    /**
     * 获取远程文件的元数据
     *
     * @param bucketName 桶名称
     * @param remotePath 远程文件地址
     * @param info       元数据收集器
     * @throws Exception
     */
    protected abstract void loadMetadata(String bucketName, String remotePath, Map info) throws Exception;

    @Override
    public boolean deleteFile(String bucketName, String remotePath) {
        remotePath = processPathStartWithOutSeparator(remotePath);
        try {
            deleteRemoteFile(bucketName, remotePath);
            return true;
        } catch (Exception e) {
            log.error("DeleteFile:", e);
        }
        return false;
    }

    /**
     * 删除远程目标文件
     *
     * @param bucketName 桶名称
     * @param remotePath 远程文件地址
     * @throws Exception
     */
    protected abstract void deleteRemoteFile(String bucketName, String remotePath) throws Exception;

    @Override
    public String generatePresignedUrl(String bucketName, String remotePath, int expireSeconds) {
        remotePath = processPathStartWithOutSeparator(remotePath);
        return doGeneratePresignedUrl(bucketName, remotePath, expireSeconds);
    }

    /**
     * 生成远程文件临时可访问地址
     *
     * @param bucketName    桶名称
     * @param remotePath    远程文件地址
     * @param expireSeconds 有效时间，秒
     * @return 文件临时可访问地址
     */
    protected abstract String doGeneratePresignedUrl(String bucketName, String remotePath, int expireSeconds);

}
