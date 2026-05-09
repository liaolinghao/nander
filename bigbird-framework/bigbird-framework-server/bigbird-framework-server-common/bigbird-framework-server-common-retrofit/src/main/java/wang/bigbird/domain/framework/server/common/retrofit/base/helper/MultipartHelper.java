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
package wang.bigbird.domain.framework.server.common.retrofit.base.helper;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;

/**
 * 表单提交参数构造辅助器
 * @Multipart
 * @POST(API.VIDEO_MODULE_UPLOAD_VIDEO)
 * Call<Object> uploadImage(@Part MultipartBody.Part imagePicFile,
 *                          @Part MultipartBody.Part videoFile,
 *                          @Part("videoDescription") RequestBody desc,
 *                          @Part("videoPicWidth") RequestBody videoPicWidth,
 *                          @Part("videoPicHeight") RequestBody videoPicHeight);
 *
 *
 * @author Bigbird
 */
public class MultipartHelper {

    /**
     * 创建表单的普通字段
     *
     * @param content 表单字段值
     * @return 表单普通字段
     */
    public static RequestBody createFormBody(String content) {
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), content);
        return body;
    }

    /**
     * 创建表单的文件字段
     *
     * @param fieldName 表单字段名
     * @param file      文件
     * @return 表单文件字段
     */
    public static MultipartBody.Part createFilePart(String fieldName, File file) {
        String contentType = new MimetypesFileTypeMap()
                .getContentType(file);
        if (FileUtils.isImageFile(file)) {
            contentType = "image/" + FileUtils.getSuffix(file);
        } else if (FileUtils.isAudioFile(file)) {
            contentType = "audio/" + FileUtils.getSuffix(file);
        }
        if (StringUtils.isBlank(contentType)) {
            contentType = "application/octet-stream";
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse(contentType), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData(fieldName, file.getName(), requestFile);
        return body;
    }

}
