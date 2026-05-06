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
package wang.bigbird.domain.framework.server.web.core.controller;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.document.pdf.base.tool.PdfProcessor;
import wang.bigbird.domain.framework.server.core.exception.BusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.core.support.response.RespResult;
import wang.bigbird.domain.framework.server.web.core.base.constant.WebCoreConstants;
import wang.bigbird.domain.framework.server.web.core.base.util.WebUtils;
import wang.bigbird.domain.framework.server.web.core.config.property.WebProperties;
import wang.bigbird.domain.framework.server.web.core.support.handler.SecurityHandler;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * web公共业务接口
 *
 * @author Bigbird
 */
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Autowired
    private WebProperties webProperties;

    @Autowired
    private SecurityHandler securityHandler;

    /**
     * 网站对外提供的服务器时间戳
     *
     * @return 服务器时间戳
     */
    @GetMapping(value = "/load-server-time-stamp")
    public RespResult<Long> loadServerTimeStamp() {
        return RespResult.ok(System.currentTimeMillis());
    }

    /**
     * 从Session中获取对应键值
     *
     * @param key     键
     * @param session session
     * @return 指定键值
     */
    @GetMapping(value = "/get-from-session/{key}")
    public RespResult<String> getFromSession(@PathVariable(value = "key") String key, HttpSession session) {
        String value = (String) session.getAttribute(key);
        return RespResult.ok(StringUtils.processNullStr(value));
    }

    /**
     * 将指定键值信息存入Session
     *
     * @param map     键值对
     * @param session session
     * @return 保存结果
     */
    @PutMapping(value = "/save-in-session")
    public RespResult<Void> saveInSession(@RequestBody Map<String, String> map, HttpSession session) {
        Set<Map.Entry<String, String>> entrys = map.entrySet();
        for (Map.Entry<String, String> entry : entrys) {
            session.setAttribute(entry.getKey(), entry.getValue());
        }
        return RespResult.ok();
    }

    /**
     * 从Session中移除指定键值
     *
     * @param keyInfo 键
     * @param session session
     * @return 删除结果
     */
    @DeleteMapping(value = "/remove-from-session/{keyInfo}")
    public RespResult<Void> removeFromSession(@PathVariable(value = "keyInfo") String keyInfo, HttpSession session) {
        String[] keys = keyInfo.split(CommonConstants.COMMA);
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            session.removeAttribute(key);
        }
        return RespResult.ok();
    }

    /**
     * 取消处理过程
     *
     * @param processId 处理过程ID
     * @param session   session
     * @return 取消结果
     */
    @PutMapping(value = "/cancel-process/{processId}")
    public RespResult<Void> cancelProcess(@PathVariable(value = "processId") String processId, HttpSession session) {
        session.setAttribute(
                WebCoreConstants.CANCEL_PROGRESS + CommonConstants.DOLLAR + processId, true);
        return RespResult.ok();
    }

    /**
     * 获取处理过程状态
     *
     * @param processId 处理过程ID
     * @param session   session
     * @return 当前处理过程状态
     */
    @GetMapping(value = "/get-process/{processId}")
    public RespResult<String> getProcess(@PathVariable(value = "processId") String processId, HttpSession session) {
        String process = StringUtils.processNullStr((String) session.getAttribute(
                WebCoreConstants.CURRENT_PROGRESS + CommonConstants.DOLLAR + processId));
        if (process.equalsIgnoreCase(WebCoreConstants.CANCELED)
                || process.equalsIgnoreCase(WebCoreConstants.FAILED)
                || process.equalsIgnoreCase(WebCoreConstants.SUCCESSED)) {
            clearCache(session, WebCoreConstants.CANCEL_PROGRESS + CommonConstants.DOLLAR + processId,
                    WebCoreConstants.CURRENT_PROGRESS + CommonConstants.DOLLAR + processId);
        }
        return RespResult.ok(process);
    }

    /**
     * 上传文件，仅支持上传一个附件
     *
     * @param currentAttachName 当前附件名称
     * @param supportSuffix     支持的文件格式，必须是支持的白名单中的格式子集
     * @param file              上传文件对象
     * @param session           session
     * @return 上传文件信息
     * @throws IOException
     */
    @PostMapping(value = "/file/upload")
    public RespResult<Map<String, Object>> uploadFile(@RequestParam(value = "currentAttachName", required = false) String currentAttachName,
                                                      @RequestParam(value = "supportSuffix", required = false) String supportSuffix,
                                                      @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        // 验证上传文件是否符合约定，执行白名单检查
        verifyingUploadedFiles(supportSuffix, Lists.newArrayList(file));
        // 删除同名附件，释放空间
        deleteOldFile(currentAttachName, session);
        // 保存上传文件
        Map<String, Object> map = saveUploadedFiles(file, session);
        return RespResult.ok(map);
    }

    /**
     * 删除文件
     *
     * @param filePath 待解密的文件路径
     * @return 删除结果
     */
    @DeleteMapping(value = "/file/delete")
    public RespResult<Boolean> deleteFile(@RequestParam(value = "filePath") String filePath) throws FileNotFoundException, UnsupportedEncodingException {
        Assert.isTrue(StringUtils.isNotBlank(filePath), "The file path is null.");
        File file = new File(WebUtils.getAppDir(), decrypt(filePath));
        boolean ret = FileUtils.delFileOrFolder(file);
        return RespResult.ok(ret);
    }

    /**
     * 下载文件
     *
     * @param filePath 待解密的文件路径
     * @param fileName 下载文件保存名称
     * @param request  请求对象
     * @param response 响应对象
     * @throws IOException 可能抛出该异常
     */
    @GetMapping(value = "/file/download")
    public void downloadFile(@RequestParam(value = "filePath") String filePath,
                             @RequestParam(value = "fileName", required = false) String fileName,
                             HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        Assert.isTrue(StringUtils.isNotBlank(filePath), "The file path is null.");
        // 规范化路径并安全检查
        String normalizedPath = Paths.get(decrypt(filePath)).normalize().toString();
        if (normalizedPath.contains(CommonConstants.DOUBLE_DOT)) {
            throw BusinessException.of(IBaseResponseStatus.MULTIPART_FILE_PATH_INVALID);
        }
        File file = new File(WebUtils.getAppDir(), normalizedPath);
        Assert.isTrue(file.exists(), "The file path is not existed.");
        WebUtils.exportFile(file, fileName, request, response);
    }

    /**
     * 初始化取消状态
     *
     * @param processId 处理过程ID
     * @param session   session
     */
    protected void initCancelStatus(String processId, HttpSession session) {
        String key = WebCoreConstants.CANCEL_PROGRESS + CommonConstants.DOLLAR + processId;
        session.setAttribute(key, false);
    }

    /**
     * 开始处理
     *
     * @param processId 处理过程ID
     * @param session   session
     */
    protected void startProgress(String processId, HttpSession session) {
        String key = WebCoreConstants.CURRENT_PROGRESS + CommonConstants.DOLLAR + processId;
        session.setAttribute(key, WebCoreConstants.STARTED);
    }

    /**
     * 设置当前处理状态
     *
     * @param processId 处理过程ID
     * @param info      状态信息
     * @param session   session
     */
    protected void setProgressStatus(
            String processId, String info, HttpSession session) {
        String key = WebCoreConstants.CURRENT_PROGRESS + CommonConstants.DOLLAR + processId;
        session.setAttribute(key, info);
    }

    /**
     * 是否取消任务
     *
     * @param processId 处理过程ID
     * @param session   session
     * @return 是否取消
     */
    protected boolean isCancel(String processId, HttpSession session) {
        String key = WebCoreConstants.CANCEL_PROGRESS + CommonConstants.DOLLAR + processId;
        Object value = session.getAttribute(key);
        return value == null ? false : (Boolean) value;
    }

    /**
     * 清除session中指定键值
     *
     * @param session session
     * @param keys    键信息
     */
    protected void clearCache(HttpSession session, String... keys) {
        for (String key : keys) {
            session.removeAttribute(key);
        }
    }

    /**
     * 获取真实的文件路径
     *
     * @param filePath 待解密的文件路径
     * @return 解密后的文件路径
     */
    private String decrypt(String filePath) {
        return securityHandler.decrypt(filePath);
    }

    /**
     * 上传附件时，删除重名的旧文件，防止文件堆积
     *
     * @param currentAttachName 附件名称
     * @param session           session
     * @throws FileNotFoundException
     */
    private void deleteOldFile(String currentAttachName, HttpSession session) throws IOException {
        if (StringUtils.isBlank(currentAttachName)) {
            return;
        }
        // 如果传递了当前附件名称过来，那么意味着需要把以前的附件删除，以防止垃圾文件过多
        String attachPath = (String) session.getAttribute(currentAttachName);
        if (StringUtils.isBlank(attachPath)) {
            return;
        }
        session.removeAttribute(currentAttachName);
        // 规范化路径并防止目录遍历
        attachPath = Paths.get(attachPath).normalize().toString();
        if (attachPath.contains(CommonConstants.DOUBLE_DOT)) {
            log.warn("Suspicious path attempt detected: {}", attachPath);
            return;
        }
        // 限制路径在temp子目录下
        String safeBaseDir = webProperties.getDir() + File.separator + "temp";
        if (!attachPath.startsWith(safeBaseDir)) {
            log.warn("Attempt to access an illegal path: {}", attachPath);
            return;
        }
        // 进一步验证路径是否在应用目录内
        String canonicalBaseDir = new File(WebUtils.getAppDir()).getCanonicalPath();
        File targetFile = new File(canonicalBaseDir, attachPath);
        if (!targetFile.getCanonicalPath().startsWith(canonicalBaseDir)) {
            log.warn("Attempt to access outside application directory: {}", attachPath);
            return;
        }
        // 将原有文件删除，如果删除失败这里不再处理，留待后续在周期性清理任务中进行
        FileUtils.delFileOrFolder(targetFile);
    }

    /**
     * 验证上传的文件集合
     *
     * @param supportSuffix 支持的文件后缀
     * @param files         上传文件
     * @throws IOException
     */
    private void verifyingUploadedFiles(String supportSuffix, List<MultipartFile> files) throws IOException {
        for (MultipartFile mpf : files) {
            String fileName = mpf.getOriginalFilename();
            String suffix = FileUtils.getSuffix(fileName).toLowerCase();
            if (StringUtils.isBlank(suffix)) {
                throw BusinessException.of(IBaseResponseStatus.MULTIPART_FILE_SUFFIX_EMPTY);
            } else {
                // 这里为了防止用户上传一些脚本运行文件进行攻击，先执行白名单检测
                if (WebUtils.inWhiteList(suffix)) {
                    if (StringUtils.isNotBlank(supportSuffix)) {
                        if (!Arrays.asList(supportSuffix.toLowerCase().split(CommonConstants.COMMA)).contains(suffix)) {
                            throw BusinessException.of(IBaseResponseStatus.MULTIPART_FILE_NOT_IN_WHITELIST);
                        }
                    }
                } else {
                    throw BusinessException.of(IBaseResponseStatus.MULTIPART_FILE_NOT_IN_WHITELIST);
                }
            }
        }
    }

    /**
     * 保存上传文件
     *
     * @param mpf
     * @param session
     * @return
     * @throws IOException
     */
    private Map<String, Object> saveUploadedFiles(MultipartFile mpf, HttpSession session) throws IOException {
        Map<String, Object> map = new HashMap<>(CollectionUtils.initialMapCapacity(5));
        String fileName = FileUtils.sanitize(mpf.getOriginalFilename());
        String suffix = FileUtils.getSuffix(fileName);
        // 构造上传附件唯一性标识
        String uuid = StringUtils.getUuid();
        // 上传附件的存放路径
        String attachPath = WebUtils.getTempPath(webProperties.getDir(), uuid, suffix);
        if (attachPath.contains(CommonConstants.DOUBLE_DOT)) {
            throw BusinessException.of(IBaseResponseStatus.MULTIPART_FILE_PATH_INVALID);
        }
        // 获取应用基础目录并转换为规范路径
        File appDir = new File(WebUtils.getAppDir());
        String baseDir = appDir.getCanonicalPath();
        // 构建完整目标路径并规范化
        Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
        Path destPath = basePath.resolve(attachPath).normalize();
        // 更严格的路径验证
        if (!destPath.startsWith(basePath)) {
            throw BusinessException.of(IBaseResponseStatus.MULTIPART_FILE_PATH_INVALID);
        }
        File desFile = destPath.toFile();
        File parentDir = desFile.getParentFile();
        if (parentDir == null || !FileUtils.newFolder(parentDir)) {
            throw BusinessException.of(IBaseResponseStatus.MULTIPART_FILE_DIR_NOT_EXIST);
        }
        mpf.transferTo(desFile);
        if (FileUtils.isPdfFile(desFile) && PdfProcessor.isContainJavaScript(desFile)) {
            // 文件包含脚本，删除文件
            FileUtils.delFileOrFolder(desFile);
            throw BusinessException.of(IBaseResponseStatus.MULTIPART_FILE_CONTENT_CONTAIN_JS);
        }
        session.setAttribute(fileName, attachPath);
        map.put("fileName", fileName);
        // 此处把附件路径传递给前端，对于windows路径，要转换为网页链接分隔符
        attachPath = attachPath.replace(CommonConstants.BACKSLASH, CommonConstants.SLASH);
        map.put("attachPath", attachPath);
        map.put("downloadPath", securityHandler.encrypt(attachPath));
        if (FileUtils.isImageFile(fileName)) {
            try (InputStream is = new FileInputStream(desFile)) {
                BufferedImage img = ImageIO.read(is);
                map.put("width", img.getWidth());
                map.put("height", img.getHeight());
            }
        }
        return map;
    }

}
