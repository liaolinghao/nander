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
package wang.bigbird.domain.framework.data.oss.base.enums;

/**
 * 对象存储类型
 *
 * @author Bigbird
 */
public enum OssTypeEnum {

    /**
     * MinIO对象存储
     */
    MINIO,
    /**
     * 阿里云对象存储
     */
    ALIYUN,
    /**
     * 腾讯云对象存储
     */
    QCLOUD,
    /**
     * 华为云对象存储
     */
    HUAWEI,
    /**
     * 天翼云对象存储
     */
    CT,
    /**
     * ftp远程文件存储
     */
    FTP,
    /**
     * 分布式文件存储
     */
    DFS

}
