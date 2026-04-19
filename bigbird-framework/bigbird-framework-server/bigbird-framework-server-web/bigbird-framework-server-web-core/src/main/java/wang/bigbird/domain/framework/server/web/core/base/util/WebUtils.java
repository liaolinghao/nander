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
package wang.bigbird.domain.framework.server.web.core.base.util;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Arrays;

/**
 * WEB通用工具
 *
 * @author Bigbird
 */
public class WebUtils {

    private static ResourcePatternResolver batchLoader = new PathMatchingResourcePatternResolver();

    /**
     * 白名单配置文件路径
     */
    private static String WHITE_LIST_PATH = "/static/conf/whiteList.txt";

    private static final String TARGET_DIR = "/target/";
    private static final String APP_JAR_MARKER = "app.jar!";
    private static final String JAR_INDICATOR = ".jar!";
    private static final String APP_WAR_MARKER = "app.war!";
    private static final String WAR_INDICATOR = ".war!";
    private static final String FILE_PROTOCOL = "file:";
    private static final String CLASSPATH_PREFIX = "classpath*:";

    /**
     * 读取类路径中的配置文件
     *
     * @param classConfigFilePath 类路径中配置文件路径
     * @return 配置文件内容
     * @throws IOException
     */
    public static String readClassConfigFileContent(String classConfigFilePath) throws IOException {
        String path = StringUtils.joinStr(CLASSPATH_PREFIX, classConfigFilePath);
        Resource[] resources = batchLoader.getResources(path);
        if (resources.length > 0) {
            InputStream inputStream = null;
            try {
                inputStream = resources[0].getInputStream();
                int len;
                byte[] buf = new byte[1024];
                StringBuilder sb = new StringBuilder();
                while ((len = inputStream.read(buf)) != -1) {
                    sb.append(new String(buf, 0, len));
                }
                return sb.toString();
            } finally {
                StreamUtils.close(inputStream);
            }
        }
        return "";
    }

    /**
     * 获取应用根目录
     *
     * @return 应用根目录
     * @throws FileNotFoundException
     */
    public static String getAppDir() throws FileNotFoundException {
        String classPath = ResourceUtils.getURL("classpath:").getPath();
        if (classPath.contains(TARGET_DIR)) {
            return classPath.substring(0, classPath.lastIndexOf(TARGET_DIR));
        }
        // 部署应用时，将应用包改为app.jar或者app.war
        if (classPath.contains(APP_JAR_MARKER)) {
            classPath = classPath.substring(FILE_PROTOCOL.length(), classPath.lastIndexOf(JAR_INDICATOR));
            return classPath.substring(0, classPath.lastIndexOf(CommonConstants.SLASH));
        }
        if (classPath.contains(APP_WAR_MARKER)) {
            classPath = classPath.substring(FILE_PROTOCOL.length(), classPath.lastIndexOf(WAR_INDICATOR));
            return classPath.substring(0, classPath.lastIndexOf(CommonConstants.SLASH));
        }
        return (new File("")).getAbsolutePath();
    }

    /**
     * 判断文件格式是否在白名单中
     *
     * @param suffix 文件格式后缀
     * @return 是否在白名单中
     */
    public static boolean inWhiteList(String suffix) throws IOException {
        String content = readClassConfigFileContent(WHITE_LIST_PATH);
        if (StringUtils.isBlank(content)) {
            File whiteListFile = new File(StringUtils.joinStr(getAppDir(), WHITE_LIST_PATH));
            content = FileUtils.readContent(whiteListFile, false);
        }
        // 去除配置文件中因为换行符号导致的文件后缀解析错误
        content = StringUtils.compress(content);
        return Arrays.asList(content.split(",")).contains(suffix);
    }

    /**
     * 获取网站存放上传图片的路径
     *
     * @param dir             业务数据目录
     * @param uploadImageMark 图片名称
     * @param suffix          图片格式
     * @return 上传图片保存路径
     */
    public static String getUploadImagePath(String dir, String uploadImageMark,
                                            String suffix) {
        return getUploadFilePath(dir, uploadImageMark, suffix, "images");
    }

    /**
     * 获取网站存放上传视频的路径
     *
     * @param dir             业务数据目录
     * @param uploadVideoMark 视频名称
     * @param suffix          视频格式
     * @return 上传视频保存路径
     */
    public static String getUploadVideoPath(String dir, String uploadVideoMark,
                                            String suffix) {
        return getUploadFilePath(dir, uploadVideoMark, suffix, "videos");
    }

    /**
     * 获取网站存放上传头像的路径
     *
     * @param dir              业务数据目录
     * @param uploadAvatarMark 头像名称
     * @param suffix           头像格式
     * @return 上传头像保存路径
     */
    public static String getUploadAvatarPath(String dir, String uploadAvatarMark,
                                             String suffix) {
        return getUploadFilePath(dir, uploadAvatarMark, suffix, "avatars");
    }

    /**
     * 获取网站存放上传附件的路径
     *
     * @param dir                  业务数据目录
     * @param uploadAttachmentMark 附件名称
     * @param suffix               附件格式
     * @return 上传附件保存路径
     */
    public static String getUploadAttachmentPath(String dir, String uploadAttachmentMark,
                                                 String suffix) {
        return getUploadFilePath(dir, uploadAttachmentMark, suffix, "attachments");
    }

    /**
     * 获取网站存放上传文件的路径
     *
     * @param dir            业务数据目录
     * @param uploadFileMark 文件名称
     * @param suffix         文件格式
     * @param type           文件所属类别
     * @return 上传文件保存路径
     */
    private static String getUploadFilePath(String dir, String uploadFileMark,
                                            String suffix, String type) {
        StringBuffer sb = new StringBuffer();
        sb.append(dir).append(File.separator);
        sb.append("upload").append(File.separator);
        sb.append(type).append(File.separator);
        sb.append(uploadFileMark);
        sb.append(".");
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * 临时文件路径
     *
     * @param dir    业务数据目录
     * @param uuid   文件名称
     * @param suffix 文件格式
     * @return 临时文件保存路径
     */
    public static String getTempPath(String dir, String uuid, String suffix) {
        StringBuffer sb = new StringBuffer();
        sb.append(dir).append(File.separator);
        sb.append("temp").append(File.separator);
        sb.append(uuid);
        sb.append(".");
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * 报告文件路径
     *
     * @param dir    业务数据目录
     * @param uuid   报告文件名称
     * @param suffix 报告文件格式
     * @return 报告文件保存路径
     */
    public static String getReportPath(String dir, String uuid, String suffix) {
        StringBuffer sb = new StringBuffer();
        sb.append(dir).append(File.separator);
        sb.append("report").append(File.separator);
        sb.append(uuid);
        sb.append(".");
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * 模板文件路径
     *
     * @param id 模板ID
     * @return 模板文件路径
     */
    public static String getTemplatePath(String id) {
        StringBuffer sb = new StringBuffer();
        sb.append("static").append(File.separator);
        sb.append("systemTemplate").append(File.separator);
        sb.append(id).append(File.separator);
        sb.append("template.xlsx");
        return sb.toString();
    }

    /**
     * 模板规则文件路径
     *
     * @param id 模板ID
     * @return 模板规则文件路径
     */
    public static String getTemplateRulePath(String id) {
        StringBuffer sb = new StringBuffer();
        sb.append("static").append(File.separator);
        sb.append("systemTemplate").append(File.separator);
        sb.append(id).append(File.separator);
        sb.append("formatRule.ini");
        return sb.toString();
    }

    /**
     * 导出文件
     *
     * @param file     文件
     * @param fileName 文件名称
     * @param request  请求对象
     * @param response 响应对象
     * @throws IOException
     */
    public static void exportFile(File file, String fileName, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 安全处理文件名
        String safeFileName;
        if (StringUtils.isBlank(fileName)) {
            safeFileName = FileUtils.sanitize(file.getName());
        } else {
            safeFileName = FileUtils.sanitize(fileName);
        }
        String encodedFilename = Coder.urlEncoderUtf8(safeFileName);
        // 设置安全相关的HTTP头
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy", "default-src 'none'");
        response.setHeader("X-Frame-Options", "DENY");
        // 提供两种格式以兼容不同版本浏览器
        String contentDisposition = String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
                safeFileName, encodedFilename);
        // 设置内容类型和长度
        response.setHeader("Content-Disposition", contentDisposition);
        response.setHeader("Content-Length", "" + file.length());
        response.setContentType(request.getSession().getServletContext()
                .getMimeType(file.getName()));
        // 使用try-with-resources确保资源释放
        try (InputStream fis = new BufferedInputStream(new FileInputStream(file));
             OutputStream fos = new BufferedOutputStream(response.getOutputStream())) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        }
    }

}
