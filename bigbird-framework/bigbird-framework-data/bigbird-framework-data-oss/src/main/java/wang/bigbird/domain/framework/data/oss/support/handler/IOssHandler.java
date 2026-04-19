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
package wang.bigbird.domain.framework.data.oss.support.handler;

import wang.bigbird.domain.framework.data.oss.base.enums.OssTypeEnum;

import java.io.InputStream;

/**
 * 对象存储处理器
 *
 * @author Bigbird
 */
public interface IOssHandler {

    /**
     * 获取对象存储处理器类型
     *
     * @return 对象存储处理器类型
     */
    OssTypeEnum getOssType();

    /**
     * 存储文件
     *
     * @param bucketName 桶名称
     *                   腾讯云不支持检查桶是否存在以及主动创建桶的方法，在使用时一定要检查在云对象里面是否提前创建好桶了
     *                   FTP不支持桶，采用FTP时该值会被忽略
     *                   DFS不支持桶，实现上将桶赋予分组的含义，必须是FastDFS服务器端预先部署并配置好的Storage组名称（如 group1、group2），无法自定义不存在的Group名称
     * @param localPath  本地文件路径
     * @param remotePath 远程文件地址
     *                   DFS不支持远程文件地址，实现上将远程文件地址作为文件元数据
     * @return 存储成功后，外界可访问的资源链接
     */
    String storeFile(String bucketName, String localPath, String remotePath);

    /**
     * 存储文件（通过输入流）
     *
     * @param bucketName  桶名称
     *                    腾讯云不支持检查桶是否存在以及主动创建桶的方法，在使用时一定要检查在云对象里面是否提前创建好桶了
     *                    FTP不支持桶，采用FTP时该值会被忽略
     *                    DFS不支持桶，实现上将桶赋予分组的含义，必须是FastDFS服务器端预先部署并配置好的Storage组名称（如 group1、group2），无法自定义不存在的Group名称
     * @param inputStream 文件输入流
     * @param fileName    原文件名（设置元数据）
     * @param fileSize    文件大小（设置元数据，字节数）
     * @param remotePath  远程文件地址
     *                    DFS不支持远程文件地址，实现上将远程文件地址作为文件元数据
     * @return 存储成功后，外界可访问的资源链接
     */
    String storeFileFromStream(String bucketName, InputStream inputStream, String fileName, long fileSize, String remotePath);

    /**
     * 下载文件
     *
     * @param bucketName 桶名称
     *                   FTP不支持桶，采用FTP时该值会被忽略
     *                   DFS不支持桶，实现上将桶赋予分组的含义，必须是FastDFS服务器端预先部署并配置好的Storage组名称（如 group1、group2），无法自定义不存在的Group名称
     * @param remotePath 远程文件地址
     *                   DFS不支持远程文件地址，实现上该值传递不带分组的路径
     * @param localPath  本地文件路径
     * @return 是否下载成功
     */
    boolean downloadFile(String bucketName, String remotePath, String localPath);

    /**
     * 复制文件到新的桶
     *
     * @param sourceBucketName      源桶名
     * @param sourceKey             源对象名
     * @param destinationBucketName 目的桶名
     * @param destinationKey        目的对象名
     */
    void copyFile(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey) throws Exception;

    /**
     * 获取文件属性
     *
     * @param bucketName 桶名称
     *                   FTP不支持桶，采用FTP时该值会被忽略
     *                   DFS不支持桶，实现上将桶赋予分组的含义，必须是FastDFS服务器端预先部署并配置好的Storage组名称（如 group1、group2），无法自定义不存在的Group名称
     * @param remotePath 远程文件地址
     *                   DFS不支持远程文件地址，实现上该值传递不带分组的路径
     * @return 文件信息，格式： {"access_url":"xxx","size":xxx,"etag"
     * :"xxx","lastModified":xxx }
     */
    String statFile(String bucketName, String remotePath);

    /**
     * 删除文件
     *
     * @param bucketName 桶名称
     *                   FTP不支持桶，采用FTP时该值会被忽略
     *                   DFS不支持桶，实现上将桶赋予分组的含义，必须是FastDFS服务器端预先部署并配置好的Storage组名称（如 group1、group2），无法自定义不存在的Group名称
     * @param remotePath 远程文件地址
     *                   DFS不支持远程文件地址，实现上该值传递不带分组的路径
     * @return 是否删除成功
     */
    boolean deleteFile(String bucketName, String remotePath);

    /**
     * 获取目标文件输入流
     *
     * @param bucketName 桶名称
     * @param remotePath 远程文件地址
     * @return 文件输入流
     * @throws Exception
     */
    InputStream loadFileInputStream(String bucketName, String remotePath) throws Exception;

    /**
     * 生成远程文件临时可访问地址
     *
     * @param bucketName    桶名称
     * @param remotePath    远程文件地址
     * @param expireSeconds 有效时间，秒
     * @return 文件临时可访问地址
     */
    String generatePresignedUrl(String bucketName, String remotePath, int expireSeconds);

}
