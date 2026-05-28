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
package wang.bigbird.domain.framework.core.base.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Assert;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.event.TraverseEvent;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 文件操作工具类
 *
 * @author Bigbird
 */
@Slf4j
public class FileUtils {

    /**
     * 按字节计算
     */
    public static final int BYTE = 0;
    /**
     * 按KB计算
     */
    public static final int K = 1;
    /**
     * 按MB计算
     */
    public static final int M = 2;
    /**
     * 按GB计算
     */
    public static final int G = 3;
    /**
     * 按TB计算
     */
    public static final int T = 4;

    /**
     * 路径分隔符
     */
    private static final String FOLDER_SEPARATOR = "/";

    /**
     * 压缩文件格式
     */
    private static final List<String> COMPRESSED_TYPE = new ArrayList<>(Arrays.asList("zip", "rar"));

    /**
     * 图像文件后缀
     */
    private static final String[] IMAGE_TYPE = new String[]{"bmp", "jpg",
            "png", "gif", "jpeg"};
    /**
     * 网页文件后缀
     */
    private static final String[] HTML_TYPE = new String[]{"html", "htm"};
    /**
     * PDF文件后缀
     */
    private static final String[] PDF_TYPE = new String[]{"pdf"};
    /**
     * 文本文件后缀
     */
    private static final String[] TXT_TYPE = new String[]{"txt"};
    /**
     * word文档后缀
     */
    private static final String[] WORD_TYPE = new String[]{"doc", "docx"};
    /**
     * excel文档后缀
     */
    private static final String[] EXCEL_TYPE = new String[]{"xls", "xlsx"};
    /**
     * ppt文档后缀
     */
    private static final String[] POWERPOINT_TYPE = new String[]{"ppt", "pptx"};
    /**
     * 音频文件后缀
     */
    private static final String[] AUDIO_TYPE = new String[]{"mp3", "wma", "wav"};
    /**
     * 视频文件后缀
     */
    private static final String[] VIDEO_TYPE = new String[]{"mp4", "avi",
            "mpg", "wmv", "rm", "rmvb", "mpeg", "mkv"};

    /**
     * 默认缓冲区大小（8KB，可根据需求调整为 16KB/32KB）
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * 文件过滤器，对于指定格式的文件返回true，若未指定文件格式，则任何文件都返回true
     *
     * @param file   进行判断的文件
     * @param filter 文件名后缀集合
     * @return 判断结果
     */
    public static boolean filterResult(File file, List<String> filter) {
        Assert.notNull(file, "The parameter file is null.");
        Assert.isTrue(file.isFile(), "The parameter file is not a valid file.");
        if (filter == null) {
            return true;
        }
        String fileSuffix = getSuffix(file);
        for (String suffix : filter) {
            if (fileSuffix.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 文件过滤器，对于指定格式的文件返回true，若未指定文件格式，则任何文件都返回true
     *
     * @param fileName 进行判断的文件名
     * @param filter   文件名后缀集合
     * @return 判断结果
     */
    public static boolean filterResult(String fileName, List<String> filter) {
        Assert.notNull(fileName, "The parameter fileName is null.");
        if (filter == null) {
            return true;
        }
        String fileSuffix = getSuffix(fileName);
        for (String suffix : filter) {
            if (fileSuffix.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理文件分隔符，把各种文件分隔符转换成平台标准文件分隔符，同时去除末尾多余分隔符
     *
     * @param filePath 文件路径
     * @return 处理后结果
     */
    public static String processFileSeparator(String filePath) {
        if (filePath == null) {
            return null;
        }
        filePath = filePath.replace("\\", File.separator);
        filePath = filePath.replace("/", File.separator);
        if (filePath.endsWith(File.separator)) {
            filePath = filePath.substring(0, filePath.length() - 1);
        }
        return filePath;
    }

    /**
     * 判断文件是否属于指定类型文件
     *
     * @param fileName  文件名
     * @param fileTypes 文件类型集合
     * @return 判断结果
     */
    public static boolean isSpecifiedTypeFile(String fileName, String[] fileTypes) {
        Assert.notNull(fileName, "The parameter fileName is null.");
        Assert.notNull(fileTypes, "The parameter fileTypes is null.");
        String suffix = getSuffix(fileName);
        for (String type : fileTypes) {
            if (type.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否属于指定类型文件
     *
     * @param file      文件
     * @param fileTypes 文件类型集合
     * @return 判断结果
     */
    public static boolean isSpecifiedTypeFile(File file, String[] fileTypes) {
        Assert.notNull(file, "The parameter file is null.");
        Assert.notNull(fileTypes, "The parameter fileTypes is null.");
        String suffix = getSuffix(file);
        for (String type : fileTypes) {
            if (type.equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断文件是否是图像文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isImageFile(File file) {
        return isSpecifiedTypeFile(file, IMAGE_TYPE);
    }

    /**
     * 判断文件是否是图像文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isImageFile(String fileName) {
        return isSpecifiedTypeFile(fileName, IMAGE_TYPE);
    }

    /**
     * 判断文件是否是网页文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isHtmlFile(File file) {
        return isSpecifiedTypeFile(file, HTML_TYPE);
    }

    /**
     * 判断文件是否是网页文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isHtmlFile(String fileName) {
        return isSpecifiedTypeFile(fileName, HTML_TYPE);
    }

    /**
     * 判断文件是否是PDF文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isPdfFile(File file) {
        return isSpecifiedTypeFile(file, PDF_TYPE);
    }

    /**
     * 判断文件是否是PDF文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isPdfFile(String fileName) {
        return isSpecifiedTypeFile(fileName, PDF_TYPE);
    }

    /**
     * 判断文件是否是txt文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isTxtFile(File file) {
        return isSpecifiedTypeFile(file, TXT_TYPE);
    }

    /**
     * 判断文件是否是txt文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isTxtFile(String fileName) {
        return isSpecifiedTypeFile(fileName, TXT_TYPE);
    }

    /**
     * 判断文件是否是word文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isWordFile(File file) {
        return isSpecifiedTypeFile(file, WORD_TYPE);
    }

    /**
     * 判断文件是否是word文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isWordFile(String fileName) {
        return isSpecifiedTypeFile(fileName, WORD_TYPE);
    }

    /**
     * 判断文件是否是excel文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isExcelFile(File file) {
        return isSpecifiedTypeFile(file, EXCEL_TYPE);
    }

    /**
     * 判断文件是否是excel文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isExcelFile(String fileName) {
        return isSpecifiedTypeFile(fileName, EXCEL_TYPE);
    }

    /**
     * 判断文件是否是powerpoint文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isPowerPointFile(File file) {
        return isSpecifiedTypeFile(file, POWERPOINT_TYPE);
    }

    /**
     * 判断文件是否是powerpoint文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isPowerPointFile(String fileName) {
        return isSpecifiedTypeFile(fileName, POWERPOINT_TYPE);
    }

    /**
     * 判断文件是否是音频文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isAudioFile(File file) {
        return isSpecifiedTypeFile(file, AUDIO_TYPE);
    }

    /**
     * 判断文件是否是音频文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isAudioFile(String fileName) {
        return isSpecifiedTypeFile(fileName, AUDIO_TYPE);
    }

    /**
     * 判断文件是否是视频文件
     *
     * @param file 文件
     * @return 判断结果
     */
    public static boolean isVideoFile(File file) {
        return isSpecifiedTypeFile(file, VIDEO_TYPE);
    }

    /**
     * 判断文件是否是视频文件
     *
     * @param fileName 文件名称
     * @return 判断结果
     */
    public static boolean isVideoFile(String fileName) {
        return isSpecifiedTypeFile(fileName, VIDEO_TYPE);
    }

    /**
     * 获取文件格式
     *
     * @param file 文件
     * @return 文件后缀
     */
    public static String getSuffix(File file) {
        Assert.notNull(file, "The parameter file is null.");
        return getSuffix(file.getName());
    }

    /**
     * 获取文件格式
     *
     * @param fileName 文件名
     * @return 文件后缀
     */
    public static String getSuffix(String fileName) {
        Assert.notNull(fileName, "The parameter fileName is null.");
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(index + 1);
        }
        return "";
    }

    /**
     * 获取文件的不包含文件格式的名字
     *
     * @param file 文件
     * @return 不带格式的文件名称
     */
    public static String getRealName(File file) {
        Assert.notNull(file, "The parameter file is null.");
        return getRealName(file.getName());
    }

    /**
     * 获取文件的不包含文件格式的名字
     *
     * @param fileName 带格式的文件名称
     * @return 不带格式的文件名称
     */
    public static String getRealName(String fileName) {
        Assert.notNull(fileName, "The parameter fileName is null.");
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            fileName = fileName.substring(0, index);
        }
        return fileName;
    }

    /**
     * 获取完整文件名，该方法并不验证文件的有效性
     *
     * @param file 文件
     * @return 带格式的文件名称
     */
    public static String getFileFullName(File file) {
        Assert.notNull(file, "The parameter file is null.");
        return getFileFullName(file.getAbsolutePath());
    }

    /**
     * 获取完整文件名，该方法并不验证文件的有效性
     *
     * @param filePath 文件路径
     * @return 带格式的文件名称
     */
    public static String getFileFullName(String filePath) {
        Assert.notNull(filePath, "The parameter filePath is null.");
        filePath = processFileSeparator(filePath);
        int index = filePath.lastIndexOf(File.separator);
        return filePath.substring(index + 1);
    }

    /**
     * 获取指定文件路径对应的目录绝对路径，如果传递的是一个文件，则获取对应父目录，如果传递的不是目录，也不是文件，则返回空
     *
     * @param file 文件
     * @return 指定文件对应的目录绝对路径
     */
    public static String getFolderPath(File file) {
        Assert.notNull(file, "The parameter file is null.");
        String folderPath;
        if (file.isFile()) {
            folderPath = file.getParentFile().getAbsolutePath();
        } else if (file.isDirectory()) {
            folderPath = file.getAbsolutePath();
        } else {
            return null;
        }
        if (StringUtils.isNotBlank(folderPath)) {
            if (!folderPath.endsWith(File.separator)) {
                folderPath = folderPath + File.separator;
            }
        }
        return folderPath;
    }

    /**
     * 获取指定文件路径对应的目录绝对路径，如果传递的是一个文件，则获取对应父目录，如果传递的不是目录，也不是文件，则返回空
     *
     * @param filePath 文件路径
     * @return 指定文件路径对应的目录绝对路径
     */
    public static String getFolderPath(String filePath) {
        Assert.notNull(filePath, "The parameter filePath is null.");
        return getFolderPath(new File(filePath));
    }

    /**
     * 新建文件，同时采用UTF-8编码写入文件内容，新文件会完全覆盖旧文件
     *
     * @param filePathAndName 文件路径
     * @param fileContent     为null，则只创建文件
     * @return 新建是否成功
     */
    public static boolean newFile(String filePathAndName, String fileContent) {
        return newFile(filePathAndName, fileContent, false, Coder.DEFAULT_ENCODING);
    }

    /**
     * 新建文件，同时采用UTF-8编码写入文件内容
     *
     * @param filePathAndName 文件路径
     * @param fileContent     为null，则只创建文件
     * @param append          文件内容是否采用追加的形式
     * @return 新建是否成功
     */
    public static boolean newFile(String filePathAndName, String fileContent,
                                  boolean append) {
        return newFile(filePathAndName, fileContent, append, Coder.DEFAULT_ENCODING);
    }

    /**
     * 新建文件，同时采用指定编码写入文件内容
     *
     * @param filePathAndName 文件路径
     * @param fileContent     为null，则只创建文件
     * @param append          文件内容是否采用追加的形式
     * @param encode          编码类型
     * @return 新建是否成功
     */
    public static boolean newFile(String filePathAndName, String fileContent,
                                  boolean append, String encode) {
        Assert.notNull(filePathAndName, "The parameter filePathAndName is null.");
        BufferedWriter writer = null;
        try {
            File myFilePath = new File(filePathAndName);
            File dir = myFilePath.getParentFile();
            if (dir != null) {
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        return false;
                    }
                }
            }
            if (!myFilePath.exists()) {
                if (!myFilePath.createNewFile()) {
                    return false;
                }
            }
            if (fileContent != null) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(myFilePath, append), encode));
                writer.write(fileContent);
                writer.flush();
            }
            return true;
        } catch (Exception e) {
            log.error("Create File:", e);
            return false;
        } finally {
            StreamUtils.close(writer);
        }
    }

    /**
     * 创建一个文件夹
     *
     * @param folder 文件夹
     * @return 创建是否成功
     */
    public static boolean newFolder(File folder) {
        Assert.notNull(folder, "The parameter folder is null.");
        boolean bFlag = false;
        if (folder.isFile()) {
            folder = folder.getParentFile();
        }
        if (folder != null) {
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    bFlag = true;
                }
            } else {
                bFlag = true;
            }
        }
        return bFlag;
    }

    /**
     * 创建一个文件夹
     *
     * @param folderPath 文件夹路径
     * @return 创建是否成功
     */
    public static boolean newFolder(String folderPath) {
        Assert.notNull(folderPath, "The parameter folderPath is null.");
        return newFolder(new File(folderPath));
    }

    /**
     * 将字节数组中的内容写入到文件中
     *
     * @param file    文件
     * @param content 字节数组内容
     * @return 写入是否成功
     */
    public static boolean write(File file, byte[] content) {
        Assert.notNull(file, "The parameter file is null.");
        Assert.notNull(content, "The parameter content is null.");
        return write(file, new ByteArrayInputStream(content));
    }

    /**
     * 将字节数组中的内容写入到文件中
     *
     * @param filePath 文件路径
     * @param content  字节数组内容
     * @return 写入是否成功
     */
    public static boolean write(String filePath, byte[] content) {
        Assert.notNull(filePath, "The parameter filePath is null.");
        Assert.notNull(content, "The parameter content is null.");
        return write(new File(filePath), new ByteArrayInputStream(content));
    }

    /**
     * 将输入流中的内容写入到文件中
     *
     * @param file 文件
     * @param in   内容输入流
     * @return 写入是否成功
     */
    public static boolean write(File file, InputStream in) {
        Assert.notNull(file, "The parameter file is null.");
        Assert.notNull(in, "The parameter in is null.");
        FileOutputStream fos = null;
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return false;
                }
            }
            fos = new FileOutputStream(file);
            int index;
            byte[] buffer = new byte[4096];
            while ((index = in.read(buffer)) != -1) {
                fos.write(buffer, 0, index);
            }
            return true;
        } catch (Exception e) {
            log.error("Write:", e);
            return false;
        } finally {
            StreamUtils.close(fos, in);
        }
    }

    /**
     * 将输入流中的内容写入到文件中
     *
     * @param filePath 文件路径
     * @param in       内容输入流
     * @return 写入是否成功
     */
    public static boolean write(String filePath, InputStream in) {
        Assert.notNull(filePath, "The parameter filePath is null.");
        Assert.notNull(in, "The parameter in is null.");
        return write(new File(filePath), in);
    }

    /**
     * 以指定编码格式将文件内容写入保存的文件路径中
     *
     * @param fileContent 文件内容
     * @param file        文件
     * @param encode      编码格式
     * @return 保存结果
     */
    public static boolean saveFile(String fileContent, File file,
                                   String encode) {
        Assert.notNull(file, "The parameter file is null.");
        Writer out = null;
        try {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return false;
                }
            }
            out = new OutputStreamWriter(
                    new FileOutputStream(file, false), encode);
            out.write(fileContent);
        } catch (Exception e) {
            log.error("Save file:", e);
            return false;
        } finally {
            StreamUtils.close(out);
        }
        return true;
    }

    /**
     * 以指定编码格式将文件内容写入保存的文件路径中
     *
     * @param fileContent 文件内容
     * @param filePath    文件路径
     * @param encode      编码格式
     * @return 保存结果
     */
    public static boolean saveFile(String fileContent, String filePath,
                                   String encode) {
        Assert.notNull(filePath, "The parameter filePath is null.");
        return saveFile(fileContent, new File(filePath), encode);
    }

    /**
     * 删除文件或文件夹
     *
     * @param file 文件
     * @return 删除结果
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean delFileOrFolder(File file) {
        Assert.notNull(file, "The parameter file is null.");
        return delFileOrFolder(file.getPath());
    }

    /**
     * 删除文件或文件夹
     *
     * @param fileOrFolderPath 文件路径
     * @return 删除结果
     */
    public static boolean delFileOrFolder(String fileOrFolderPath) {
        Assert.notNull(fileOrFolderPath, "The parameter fileOrFolderPath is null.");
        fileOrFolderPath = processFileSeparator(fileOrFolderPath);
        File file = new File(fileOrFolderPath);
        if (!file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            String[] tempList = file.list();
            if (tempList != null) {
                File temp;
                for (String s : tempList) {
                    temp = new File(StringUtils.join(fileOrFolderPath, File.separator, s));
                    if (temp.isFile()) {
                        if (!temp.delete()) {
                            return false;
                        }
                    }
                    if (temp.isDirectory()) {
                        if (!delFileOrFolder(temp.getAbsolutePath())) {
                            return false;
                        }
                    }
                }
            }
        }
        return file.delete();
    }

    /**
     * 清空文件夹，不删除该文件夹，删除该文件夹下的所有文件和子文件夹
     *
     * @param dir 目录
     * @return 清空结果
     */
    public static boolean clearDir(File dir) {
        Assert.notNull(dir, "The parameter dir is null.");
        Assert.isTrue(dir.exists() && dir.isDirectory(), "The parameter dir is not a valid directory.");
        File[] subFiles = dir.listFiles();
        if (subFiles != null) {
            for (File subFile : subFiles) {
                if (subFile.isFile()) {
                    if (!subFile.delete()) {
                        return false;
                    }
                } else if (subFile.isDirectory()) {
                    if (!delFileOrFolder(subFile)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 清空文件夹，不删除该文件夹，删除该文件夹下的所有文件和子文件夹
     *
     * @param dirPath 目录路径
     * @return 清空结果
     */
    public static boolean clearDir(String dirPath) {
        Assert.notNull(dirPath, "The parameter dirPath is null.");
        File dir = new File(dirPath);
        return clearDir(dir);
    }

    /**
     * 清空目录下除excludeFiles外的所有文件
     *
     * @param dir          目录
     * @param excludeFiles 为null或者未指定排除文件，则清空所有文件
     * @return 清理结果
     */
    public static boolean clearDirExcludeFiles(File dir, File[] excludeFiles) {
        Assert.notNull(dir, "The parameter dir is null.");
        Assert.isTrue(dir.exists() && dir.isDirectory(), "The parameter dir is not a valid directory.");
        if (CollectionUtils.isNullOrEmpty(excludeFiles)) {
            return clearDir(dir);
        } else {
            File[] subFiles = dir.listFiles();
            if (subFiles != null) {
                for (File subFile : subFiles) {
                    boolean exclude = false;
                    for (File excludeFile : excludeFiles) {
                        if (subFile.getAbsolutePath().equalsIgnoreCase(
                                excludeFile.getAbsolutePath())) {
                            exclude = true;
                            break;
                        }
                    }
                    if (exclude) {
                        continue;
                    }
                    if (subFile.isFile()) {
                        if (!subFile.delete()) {
                            return false;
                        }
                    } else if (subFile.isDirectory()) {
                        if (!clearDirExcludeFiles(subFile, excludeFiles)) {
                            return false;
                        }
                        if (CollectionUtils.isNullOrEmpty(subFile.list())) {
                            if (!delFileOrFolder(subFile)) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    /**
     * 复制文件到另一文件中
     *
     * @param srcFile 源文件
     * @param desFile 目标文件
     * @return 复制结果
     */
    public static boolean copyFileToOtherFile(File srcFile, File desFile) {
        Assert.notNull(srcFile, "The parameter srcFile is null.");
        Assert.notNull(desFile, "The parameter desFile is null.");
        // 首先判断两个文件是否是同一个文件
        if (srcFile.exists() && desFile.exists()) {
            try {
                // 判断两个File对象是否指向同一个文件
                if (srcFile.getCanonicalFile().equals(
                        desFile.getCanonicalFile())) {
                    // 同一个文件，直接返回true
                    return true;
                }
            } catch (IOException e) {
                log.error("Copy file to otherFile:", e);
            }
        }
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            // 先创建目标文件所在文件夹，创建失败，不直接返回false，让函数执行报错，以便于分析
            File dir = desFile.getParentFile();
            if (dir != null) {
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        throw new Exception("Failed to make destination dir.");
                    }
                }
            }
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(desFile);
            int byteRead;
            byte[] buffer = new byte[4096];
            while ((byteRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, byteRead);
            }
        } catch (Exception e) {
            log.error("Copy file to otherFile:", e);
            return false;
        } finally {
            StreamUtils.close(in, out);
        }
        return true;
    }

    /**
     * 复制文件到另一文件中
     *
     * @param srcPath 源文件路径
     * @param desPath 目标文件路径
     * @return 复制结果
     */
    public static boolean copyFileToOtherFile(String srcPath, String desPath) {
        Assert.notNull(srcPath, "The parameter srcPath is null.");
        Assert.notNull(desPath, "The parameter desPath is null.");
        return copyFileToOtherFile(new File(srcPath), new File(desPath));
    }

    /**
     * 文件复制前的检查准备工作
     *
     * @param srcFile 源文件或者文件夹
     * @param desDir  目标目录
     * @return 是否可执行复制操作
     */
    private static boolean preCopyCheck(File srcFile, File desDir) {
        if (!srcFile.exists()) {
            return false;
        }
        // 若文件不存在，必须先创建后判断
        if (!desDir.exists()) {
            if (!desDir.mkdirs()) {
                return false;
            }
        }
        if (desDir.isFile()) {
            return false;
        }
        if (srcFile.isDirectory()) {
            // 目标文件夹是源文件夹的子文件夹不允许复制
            return !isChildFolder(srcFile, desDir);
        }
        return true;
    }

    /**
     * 复制文件或文件夹到指定的文件夹，保持源目录结构
     *
     * @param srcFile 源文件或文件夹
     * @param desDir  目标文件夹
     * @param filter  文件后缀集合，若为空，则复制所有文件
     * @return 复制结果
     */
    public static boolean copyFileOrFolder(File srcFile, File desDir,
                                           List<String> filter) {
        Assert.notNull(srcFile, "The parameter srcFile is null.");
        Assert.notNull(desDir, "The parameter desDir is null.");
        return copyFileOrFolder(srcFile.getAbsolutePath(), desDir.getAbsolutePath(), filter);
    }

    /**
     * 复制文件或文件夹到指定的文件夹，保持源目录结构
     *
     * @param srcPath 源文件或文件夹路径
     * @param desPath 目标文件夹路径
     * @param filter  文件后缀集合，若为空，则复制所有文件
     * @return 复制结果
     */
    public static boolean copyFileOrFolder(String srcPath, String desPath,
                                           List<String> filter) {
        Assert.notNull(srcPath, "The parameter srcPath is null.");
        Assert.notNull(desPath, "The parameter desPath is null.");
        srcPath = processFileSeparator(srcPath);
        desPath = processFileSeparator(desPath);
        File srcFile = new File(srcPath);
        File desDir = new File(desPath);
        if (!preCopyCheck(srcFile, desDir)) {
            return false;
        }
        if (srcFile.isFile()) {
            if (srcFile.getParentFile().equals(desDir)) {
                // 文件就在目标目录，不需要复制
                return true;
            }
            if (filterResult(srcFile, filter)) {
                InputStream inStream = null;
                FileOutputStream fos = null;
                try {
                    int byteRead;
                    String fileName = srcFile.getName();
                    inStream = new FileInputStream(srcPath);
                    fos = new FileOutputStream(StringUtils.join(desPath
                            , File.separator, fileName));
                    byte[] buffer = new byte[4096];
                    while ((byteRead = inStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteRead);
                    }
                    fos.flush();
                } catch (Exception e) {
                    log.error("CopyFileOrFolder:", e);
                    return false;
                } finally {
                    StreamUtils.close(inStream, fos);
                }
            }
        } else if (srcFile.isDirectory()) {
            if (srcFile.equals(desDir)) {
                // 相同目录，不需要复制
                return true;
            }
            String[] file = srcFile.list();
            if (!CollectionUtils.isNullOrEmpty(file)) {
                File temp;
                for (String s : file) {
                    temp = new File(StringUtils.join(srcPath, File.separator, s));
                    if (temp.isFile()) {
                        if (filterResult(temp, filter)) {
                            if (!copyFileOrFolder(
                                    temp.getAbsolutePath(),
                                    StringUtils.join(desPath, File.separator
                                            , srcFile.getName()), filter)) {
                                return false;
                            }
                        }
                    } else if (temp.isDirectory()) {
                        if (!copyFileOrFolder(
                                temp.getAbsolutePath(),
                                StringUtils.join(desPath, File.separator
                                        , srcFile.getName()), filter)) {
                            return false;
                        }
                    }
                }
            } else {
                File desDir2 = new File(StringUtils.join(desPath, File.separator
                        , srcFile.getName()));
                if (!desDir2.exists()) {
                    return desDir2.mkdirs();
                }
            }
        }
        return true;
    }

    /**
     * 剪切文件或文件夹到指定文件夹
     *
     * @param srcFile 源文件或文件夹
     * @param desDir  目标文件夹
     * @return 剪切结果
     */
    public static boolean moveFileOrFolder(File srcFile, File desDir) {
        Assert.notNull(srcFile, "The parameter srcFile is null.");
        Assert.notNull(desDir, "The parameter desDir is null.");
        return moveFileOrFolder(srcFile.getAbsolutePath(), desDir.getAbsolutePath());
    }

    /**
     * 剪切文件或文件夹到指定文件夹
     *
     * @param oldPath 源文件或文件夹路径
     * @param newPath 目标文件夹路径
     * @return 剪切结果
     */
    public static boolean moveFileOrFolder(String oldPath, String newPath) {
        Assert.notNull(oldPath, "The parameter oldPath is null.");
        Assert.notNull(newPath, "The parameter newPath is null.");
        oldPath = processFileSeparator(oldPath);
        newPath = processFileSeparator(newPath);
        File srcFile = new File(oldPath);
        File desDir = new File(newPath);
        if (srcFile.getParentFile().equals(desDir)) {
            return true;
        }
        if (copyFileOrFolder(oldPath, newPath, null)) {
            return delFileOrFolder(oldPath);
        } else {
            return false;
        }
    }

    /**
     * 得到一个目录或者文件的大小，以字节为单位
     *
     * @param directory 目录
     * @return 目录占用磁盘空间容量大小
     */
    private static long getDirSize(File directory) {
        long size = 0;
        if (directory.isFile()) {
            size += directory.length();
        } else if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (!CollectionUtils.isNullOrEmpty(files)) {
                for (File file : files) {
                    size += getDirSize(file);
                }
            }
        }
        return size;
    }

    /**
     * 得到一个目录或者文件的大小，默认按字节单位返回
     *
     * @param directory 目录
     * @param type      统计单位类型：BYTE，K，M，G，T
     * @return 目录占用磁盘空间容量大小
     */
    public static float getDirSize(File directory, int type) {
        Assert.notNull(directory, "The parameter directory is null.");
        Assert.isTrue(directory.exists(), "The parameter directory is not existed.");
        NumberFormat formatter = new DecimalFormat("#.##");
        long bytes = getDirSize(directory);
        switch (type) {
            case K:
                return Float.parseFloat(formatter.format(bytes / 1024.0));
            case M:
                return Float.parseFloat(formatter.format(bytes / 1024.0 / 1024.0));
            case G:
                return Float.parseFloat(formatter
                        .format(bytes / 1024.0 / 1024.0 / 1024.0));
            case T:
                return Float.parseFloat(formatter.format(bytes / 1024.0 / 1024.0
                        / 1024.0 / 1024.0));
            default:
                return bytes;
        }
    }

    /**
     * 判断目标文件夹是否是源文件夹的子文件夹
     *
     * @param src  源文件夹
     * @param dest 目标文件夹
     * @return 判断结果
     */
    public static boolean isChildFolder(File src, File dest) {
        Assert.notNull(src, "The parameter src is null.");
        Assert.notNull(dest, "The parameter dest is null.");
        Assert.isTrue(src.exists() && src.isDirectory(),
                "The parameter src is not a valid directory.");
        Assert.isTrue(dest.exists() && dest.isDirectory(),
                "The parameter dest is not a valid directory.");
        File[] files = src.listFiles();
        if (!CollectionUtils.isNullOrEmpty(files)) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (dest.getAbsolutePath().equals(file.getAbsolutePath())
                            || isChildFolder(file, dest)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断目标文件夹是否是源文件夹的子文件夹
     *
     * @param srcPath 源文件夹路径
     * @param desPath 目标文件夹路径
     * @return 判断结果
     */
    public static boolean isChildFolder(String srcPath, String desPath) {
        Assert.notNull(srcPath, "The parameter srcPath is null.");
        Assert.notNull(desPath, "The parameter desPath is null.");
        return isChildFolder(new File(srcPath), new File(desPath));
    }

    /**
     * 判断是否是目录，如果目录不存在，则创建目录
     *
     * @param dir 目录
     * @return 判断结果
     */
    public static boolean isDirectory(File dir) {
        Assert.notNull(dir, "The parameter dir is null.");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return false;
            }
        }
        return dir.isDirectory();
    }

    /**
     * 判断是否是目录，如果路径对应目录不存在，则创建目录
     *
     * @param path 目录路径
     * @return 判断结果
     */
    public static boolean isDirectory(String path) {
        Assert.notNull(path, "The parameter path is null.");
        return isDirectory(new File(path));
    }

    /**
     * 处理路径，不以路径分隔符开头
     *
     * @param path 待处理路径
     * @return 处理结果
     */
    public static String processPathWithNoSeparatorStart(String path) {
        Assert.notNull(path, "The parameter path is null.");
        while (path.startsWith(FOLDER_SEPARATOR)) {
            path = path.substring(1);
        }
        return path;
    }

    /**
     * 处理路径，以路径分隔符开头
     *
     * @param path 待处理路径
     * @return 处理结果
     */
    public static String processPathWithSeparatorStart(String path) {
        Assert.notNull(path, "The parameter path is null.");
        if (!path.startsWith(FOLDER_SEPARATOR)) {
            return FOLDER_SEPARATOR + path;
        }
        return path;
    }

    /**
     * 压缩为zip文件
     *
     * @param srcFile  待压缩的源文件
     * @param destFile 目标文件应该是一个zip/rar文件
     * @return 压缩是否成功
     */
    public static boolean zip(String srcFile, String destFile) {
        Assert.notNull(srcFile, "The parameter srcFile is null.");
        Assert.notNull(destFile, "The parameter destFile is null.");
        return zip(new File(srcFile), new File(destFile));
    }

    /**
     * 压缩多个文件（目录）
     *
     * @param srcFiles 待压缩的源文件列表
     * @param destFile 目标文件应该是一个zip/rar文件
     * @return 压缩是否成功
     */
    public static boolean zip(File[] srcFiles, File destFile) {
        Assert.notNull(srcFiles, "The parameter srcFiles is null.");
        Assert.notNull(destFile, "The parameter destFile is null.");
        String suffix = getSuffix(destFile);
        if (StringUtils.isBlank(suffix) || !COMPRESSED_TYPE.contains(suffix)) {
            log.error("The destFile should be zip or rar file!");
            return false;
        }
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(destFile));
            for (File f : srcFiles) {
                if (!zip(out, f, "")) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            log.error("Zip File:", e);
            return false;
        } finally {
            StreamUtils.close(out);
        }
    }

    /**
     * 压缩为zip文件
     *
     * @param srcFile  待压缩的源文件
     * @param destFile 目标文件应该是一个zip/rar文件
     * @return 压缩是否成功
     */
    public static boolean zip(File srcFile, File destFile) {
        Assert.notNull(srcFile, "The parameter srcFile is null.");
        Assert.notNull(destFile, "The parameter destFile is null.");
        String suffix = getSuffix(destFile);
        if (StringUtils.isBlank(suffix) || !COMPRESSED_TYPE.contains(suffix)) {
            log.error("The destFile should be zip or rar file!");
            return false;
        }
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(destFile));
            return zip(out, srcFile, "");
        } catch (IOException e) {
            log.error("Zip:", e);
            return false;
        } finally {
            StreamUtils.close(out);
        }
    }

    /**
     * 压缩zip文件
     *
     * @param zipOutput 压缩流
     * @param srcFile   待压缩的源文件
     * @param baseDir   压缩根目录
     * @return 压缩是否成功
     */
    private static boolean zip(ZipOutputStream zipOutput, File srcFile, String baseDir) {
        if (baseDir == null) {
            baseDir = "";
        }
        FileInputStream fileIs = null;
        BufferedInputStream bufIs = null;
        try {
            if (srcFile.isDirectory()) {
                String currentDir = baseDir + srcFile.getName() + "/";
                log.info(currentDir);
                zipOutput.putNextEntry(new ZipEntry(currentDir));
                File[] subFiles = srcFile.listFiles();
                if (!CollectionUtils.isNullOrEmpty(subFiles)) {
                    for (File f : subFiles) {
                        if (!zip(zipOutput, f, currentDir)) {
                            return false;
                        }
                    }
                }
            } else {
                String currentFile = baseDir + srcFile.getName();
                log.info(currentFile);
                zipOutput.putNextEntry(new ZipEntry(currentFile));
                fileIs = new FileInputStream(srcFile);
                bufIs = new BufferedInputStream(fileIs);
                byte[] buf = new byte[1024 * 16];
                int len;
                while ((len = bufIs.read(buf)) > 0) {
                    zipOutput.write(buf, 0, len);
                }
            }
            return true;
        } catch (IOException e) {
            log.error("Zip {}:", srcFile.getAbsolutePath(), e);
            return false;
        } finally {
            StreamUtils.close(bufIs, fileIs);
        }
    }

    /**
     * 解压zip格式的压缩文件
     *
     * @param zipFile    压缩文件
     * @param extractDir 解压目录
     * @return 解压是否成功
     */
    public static boolean unZip(File zipFile, File extractDir) {
        Assert.notNull(zipFile, "The parameter zipFile is null.");
        Assert.notNull(extractDir, "The parameter extractDir is null.");
        return unZip(zipFile.getPath(), extractDir.getPath());
    }

    /**
     * 解压zip文件
     *
     * @param zipFilePath 解压文件，该解压文件应该为JAVA创建的压缩文件
     * @param desFilePath 目的文件夹
     * @return 解压是否成功
     */
    public static boolean unZip(String zipFilePath, String desFilePath) {
        Assert.notNull(zipFilePath, "The parameter zipFilePath is null.");
        Assert.notNull(desFilePath, "The parameter desFilePath is null.");
        desFilePath = processFileSeparator(desFilePath);
        String suffix = getSuffix(zipFilePath);
        if (StringUtils.isBlank(suffix) || !COMPRESSED_TYPE.contains(suffix)) {
            log.error("The decompressing files should be zip or rar file!");
            return false;
        }
        if (!isDirectory(desFilePath)) {
            log.error("The desFilePath should be directory!");
            return false;
        }
        if (!desFilePath.endsWith(File.separator)) {
            desFilePath = desFilePath + File.separator;
        }
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> emu = zipFile.entries();
            while (emu.hasMoreElements()) {
                ZipEntry entry = emu.nextElement();
                if (!extractEntry(desFilePath, entry, zipFile)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("UnZip:", e);
            return false;
        } finally {
            StreamUtils.close(zipFile);
        }
    }

    /**
     * 提取指定文件
     *
     * @param zipFilePath  压缩文件
     * @param fileName     要释放的文件名称
     * @param destFilePath 释放后的文件目录路径
     * @return 提取是否成功
     */
    public static boolean extractFile(String zipFilePath, String fileName, String destFilePath) {
        Assert.notNull(zipFilePath, "The parameter zipFilePath is null.");
        Assert.notNull(fileName, "The parameter fileName is null.");
        Assert.notNull(destFilePath, "The parameter destFilePath is null.");
        return extractFile(new File(zipFilePath), fileName, new File(destFilePath));
    }

    /**
     * 提取指定文件
     *
     * @param zipFile     压缩文件
     * @param fileName    要释放的文件名称，必须给出完整的路径名
     * @param destination 释放后的文件目录
     * @return 提取是否成功
     */
    @SuppressWarnings("resource")
    public static boolean extractFile(File zipFile, String fileName, File destination) {
        Assert.notNull(zipFile, "The parameter zipFile is null.");
        Assert.notNull(fileName, "The parameter fileName is null.");
        Assert.notNull(destination, "The parameter destination is null.");
        String suffix = getSuffix(zipFile);
        if (StringUtils.isBlank(suffix) || !COMPRESSED_TYPE.contains(suffix)) {
            log.error("The decompressing files should be zip or rar file!");
            return false;
        }
        if (!isDirectory(destination)) {
            log.error("The destination file should be directory!");
            return false;
        }
        if (fileName.endsWith(FOLDER_SEPARATOR)) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        String desFilePath = destination.getPath();
        if (!desFilePath.endsWith(File.separator)) {
            desFilePath = desFilePath + File.separator;
        }
        ZipFile zip = null;
        try {
            zip = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> emu = zip.entries();
            while (emu.hasMoreElements()) {
                // 遍历压缩文件包中的条目，找到一致的才提取
                ZipEntry entry = emu.nextElement();
                String name = entry.getName();
                if (name.toLowerCase().startsWith(fileName.toLowerCase())) {
                    int index = name.indexOf("/", fileName.length());
                    if (index != -1) {
                        name = name.substring(0, index);
                    }
                    if (name.equalsIgnoreCase(fileName)) {
                        if (!extractEntry(desFilePath, entry, zip)) {
                            return false;
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            log.error("ExtractFile:", e);
            return false;
        } finally {
            StreamUtils.close(zip);
        }
    }

    /**
     * 解压条目到具体目录或者文件
     *
     * @param desFilePath 目标文件
     * @param entry       解压条目
     * @param zipFile     压缩文件
     * @return 解压是否成功
     * @throws Exception
     */
    private static boolean extractEntry(String desFilePath, ZipEntry entry, ZipFile zipFile) throws Exception {
        if (entry.isDirectory()) {
            // 是目录，仅创建目录
            String des = StringUtils.join(desFilePath, entry.getName());
            if (!newFolder(des)) {
                throw new Exception(StringUtils.join("Create directory:", des, "failed!"));
            }
        } else {
            // 是文件，创建目录和生成相同内容的文件
            if (!extractEntry2File(desFilePath, entry, zipFile)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解压条目到具体文件
     *
     * @param desFilePath 目标文件
     * @param entry       解压条目
     * @param zipFile     压缩文件
     * @return 解压是否成功
     * @throws Exception
     */
    private static boolean extractEntry2File(String desFilePath, ZipEntry entry, ZipFile zipFile) throws Exception {
        File file = new File(StringUtils.join(desFilePath, entry.getName()));
        File parent = file.getParentFile();
        if (parent != null) {
            if (!newFolder(parent)) {
                throw new Exception(StringUtils.join("Create parent directory:", parent.getPath(), "failed!"));
            }
        }
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            bis = new BufferedInputStream(zipFile.getInputStream(entry));
            bos = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buf = new byte[1024 * 16];
            int len;
            while ((len = bis.read(buf)) > 0) {
                bos.write(buf, 0, len);
            }
            bos.flush();
        } catch (Exception e) {
            log.error("ExtractEntry2File:", e);
            return false;
        } finally {
            StreamUtils.close(bos, bis);
        }
        return true;
    }

    /**
     * 采用UTF-8编码将读取的文件内容以字符串形式返回，若keepFormat取值为true，则保持原有行格式，否则将文件内容做为一条字符串返回
     *
     * @param filePathAndName 文件路径
     * @param keepFormat      是否保持原格式
     * @return 文件内容
     */
    public static String readContent(String filePathAndName, boolean keepFormat) {
        Assert.notNull(filePathAndName, "The parameter filePathAndName is null.");
        return readContent(new File(filePathAndName), keepFormat);
    }

    /**
     * 采用UTF-8编码将读取的文件内容以字符串形式返回，若keepFormat取值为true，则保持原有行格式，否则将文件内容做为一条字符串返回
     *
     * @param file       文件
     * @param keepFormat 是否保持原格式
     * @return 文件内容
     */
    public static String readContent(File file, boolean keepFormat) {
        Assert.notNull(file, "The parameter file is null.");
        Assert.isTrue(file.exists(), "The parameter file is not existed.");
        return readContent(file, keepFormat, Coder.DEFAULT_ENCODING);
    }

    /**
     * 将读取的文件内容以字符串形式返回，若keepFormat取值为true，则保持原有行格式，否则将文件内容做为一条字符串返回，
     * <p>
     * 该方法将所有内容存放在内存中，适合小文件，超过百M的文件读取，容易出现内存溢出错误
     *
     * @param filePathAndName 文件路径
     * @param keepFormat      是否保持原格式
     * @param charsetName     文件编码
     * @return 文件内容
     */
    public static String readContent(String filePathAndName, boolean keepFormat, String charsetName) {
        Assert.notNull(filePathAndName, "The parameter filePathAndName is null.");
        Assert.notNull(charsetName, "The parameter charsetName is null.");
        return readContent(new File(filePathAndName), keepFormat, charsetName);
    }

    /**
     * 将读取的文件内容以字符串形式返回，若keepFormat取值为true，则保持原有行格式，否则将文件内容做为一条字符串返回，
     * <p>
     * 该方法将所有内容存放在内存中，适合小文件，超过百M的文件读取，容易出现内存溢出错误
     *
     * @param file        文件
     * @param keepFormat  是否保持原格式
     * @param charsetName 文件编码
     * @return 文件内容
     */
    public static String readContent(File file, boolean keepFormat, String charsetName) {
        Assert.notNull(file, "The parameter file is null.");
        Assert.notNull(charsetName, "The parameter charsetName is null.");
        Assert.isTrue(file.exists(), "The parameter file is not existed.");
        StringBuilder sb = new StringBuilder();
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charsetName));
            String line = fileReader.readLine();
            while (line != null) {
                if (keepFormat) {
                    sb.append(line).append(System.getProperty("line.separator"));
                } else {
                    sb.append(line.trim());
                }
                line = fileReader.readLine();
            }
        } catch (Exception e) {
            log.error("ReadContent:", e);
            return null;
        } finally {
            StreamUtils.close(fileReader);
        }
        return sb.toString();
    }

    /**
     * 依次处理从指定流中读入的每行文本
     *
     * @param input    输入流
     * @param encoding 编码
     * @param event    触发的事件
     * @throws IOException
     */
    public static void traverseLines(InputStream input, String encoding, TraverseEvent<String> event)
            throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(input, encoding))) {
            String line;
            while ((line = in.readLine()) != null) {
                event.visit(line);
            }
        }
    }

    /**
     * 允许的字符：字母、数字、点、下划线、连字符、中文字符
     * a-zA-Z：所有英文字母（大小写）。
     * 0-9：所有数字。
     * .：点号（常用于文件扩展名，如.txt）。
     * _：下划线。
     * \\-：连字符（在字符类中，连字符通常需要转义以避免与范围符号（如a-z）冲突）。
     * \u4e00-\u9fa5：中文字符范围（Unicode CJK 统一表意文字，覆盖约 20,000 个常用汉字）
     */
    private static final Pattern INVALID_CHARS = Pattern.compile("[^a-zA-Z0-9._\\-\\u4e00-\\u9fa5]");
    /**
     * 防止目录遍历攻击的模式
     */
    private static final Pattern TRAVERSAL_PATTERN = Pattern.compile("\\.\\.|/|\\\\");
    /**
     * 文件名称最大长度 255
     */
    private static final int FILENAME_MAX_LENGTH = 255;

    /**
     * 对文件名称做安全转换处理
     *
     * @param fileName 原始文件名称
     * @return 安全过滤后的文件名称
     */
    public static String sanitize(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        // 1. 防止目录遍历攻击
        String sanitized = TRAVERSAL_PATTERN.matcher(fileName).replaceAll("");
        // 2. 移除或替换不安全字符
        sanitized = INVALID_CHARS.matcher(sanitized).replaceAll("_");
        // 3. 防止隐藏文件或仅扩展名的文件
        if (sanitized.startsWith(CommonConstants.DOT)) {
            sanitized = "_" + sanitized;
        }
        // 4. 限制文件名长度
        if (sanitized.length() > FILENAME_MAX_LENGTH) {
            int dotIndex = sanitized.lastIndexOf(CommonConstants.DOT);
            if (dotIndex != -1) {
                String name = sanitized.substring(0, dotIndex);
                String ext = sanitized.substring(dotIndex);
                if (name.length() > FILENAME_MAX_LENGTH - ext.length()) {
                    name = name.substring(0, FILENAME_MAX_LENGTH - ext.length());
                }
                sanitized = name + ext;
            } else {
                sanitized = sanitized.substring(0, FILENAME_MAX_LENGTH);
            }
        }
        return sanitized;
    }

    /**
     * 自动识别文件 Content-Type
     *
     * @param file 文件
     * @return 文件对应的 Content-Type
     */
    public static String getContentType(File file) {
        String extension = getSuffix(file).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "csv":
                return "text/csv";
            case "txt":
                return "text/plain";
            case "pdf":
                return "application/pdf";
            case "iso":
            default:
                return "application/octet-stream";
        }
    }

    /**
     * 计算文件的MD5哈希值（推荐作为ETag）
     *
     * @param file 本地文件
     * @return MD5字符串（32位小写，如："e10adc3949ba59abbe56e057f20f883e"）
     */
    public static String getFileMd5(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(inputStream);
        }
    }

    /**
     * 计算文件的CRC32哈希值（轻量，效率高，可选）
     *
     * @param file 本地文件
     * @return CRC32字符串（16进制）
     */
    public static String getFileCrc32(File file) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            CRC32 crc32 = new CRC32();
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                crc32.update(buffer, 0, len);
            }
            return Long.toHexString(crc32.getValue());
        }
    }

}
