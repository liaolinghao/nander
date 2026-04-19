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
package wang.bigbird.domain.framework.core.base.util.url;

import lombok.extern.slf4j.Slf4j;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.FileUtils;
import wang.bigbird.domain.framework.core.base.util.StreamUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.core.base.util.url.cert.SslUtils;
import wang.bigbird.domain.framework.core.base.util.url.proxy.HttpProxy;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

/**
 * 以get或者post方式传递参数进行通信，由于该类中的方法对异常都进行了处理，所以对通信过程中传递参数的有效性需要采用标准约定来保证
 *
 * @author Bigbird
 */
@Slf4j
public class UrlConnectionUtils {

    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36";
    public static int CONN_TIME_OUT = 30000;
    public static int READ_TIME_OUT = 30000;

    /**
     * 设置超时时间
     */
    private static void setTimeOut() {
        System.setProperty("sun.net.client.defaultConnectTimeout",
                String.valueOf(CONN_TIME_OUT));
        System.setProperty("sun.net.client.defaultReadTimeout",
                String.valueOf(READ_TIME_OUT));
    }

    private static Map<String, String> constructRequestProperty(String referer, String cookieVal) {
        Map<String, String> requestProperty = new HashMap<>(CollectionUtils.initialMapCapacity(3));
        if (StringUtils.isNotBlank(referer)) {
            requestProperty.put("Referer", referer);
        }
        if (StringUtils.isNotBlank(cookieVal)) {
            // 发送cookie信息上去，以表明自己的身份，否则会被认为没有权限
            requestProperty.put("Cookie", cookieVal);
        }
        requestProperty.put("User-Agent", USER_AGENT);
        return requestProperty;
    }

    /**
     * 采用Get方式
     *
     * @param urlStr          无参数的完整的url语句
     * @param param           参数，格式为aaa=111&bbb=222.....
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String get(String urlStr, String param,
                             boolean isLineSeparator) {
        return get(urlStr, param, Coder.DEFAULT_ENCODING, null, "", null,
                isLineSeparator);
    }

    /**
     * 采用Get方式
     *
     * @param urlStr          无参数的完整的url语句
     * @param param           参数，格式为aaa=111&bbb=222.....
     * @param encoding        编码
     * @param referer         请求来源
     * @param cookieVal       cookie值
     * @param proxy           代理对象
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String get(String urlStr, String param, String encoding,
                             String referer, String cookieVal, HttpProxy proxy,
                             boolean isLineSeparator) {
        Map<String, String> requestProperty = constructRequestProperty(referer, cookieVal);
        return get(urlStr, param, encoding, requestProperty, proxy,
                isLineSeparator);
    }

    /**
     * 采用Get方式
     *
     * @param urlStr          无参数的完整的url语句
     * @param param           参数，格式为aaa=111&bbb=222.....
     * @param encoding        编码
     * @param requestProperty 请求头
     * @param proxy           代理对象
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String get(String urlStr, String param, String encoding,
                             Map<String, String> requestProperty, HttpProxy proxy,
                             boolean isLineSeparator) {
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        HttpURLConnection connection = null;
        try {
            connection = get(urlStr, param, requestProperty, proxy);
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), encoding));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
                if (isLineSeparator == true) {
                    result.append(StringUtils.getLineSeparator());
                }
            }
        } catch (Exception e) {
            log.error("Get:", e);
        } finally {
            StreamUtils.close(in);
            if (connection != null) {
                connection.disconnect();
                connection = null;
            }
        }
        return result.toString();
    }

    /**
     * 采用Get方式
     *
     * @param urlStr          无参数的完整的url语句
     * @param param           参数，格式为aaa=111&bbb=222.....
     * @param requestProperty 请求头
     * @param httpProxy       代理对象
     * @return 返回Web Server的连接
     */
    public static HttpURLConnection get(String urlStr, String param,
                                        Map<String, String> requestProperty, HttpProxy httpProxy) {
        try {
            URL url;
            if (StringUtils.isBlank(param)) {
                url = new URL(urlStr);
            } else {
                url = new URL(urlStr + "?" + param);
            }
            setTimeOut();
            ignoreSsl(url);
            URLConnection connection = null;
            if (httpProxy != null) {
                SocketAddress addr = new InetSocketAddress(httpProxy.getHost(),
                        httpProxy.getPort());
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                connection = url.openConnection(proxy);
                if (StringUtils.isNotBlank(httpProxy.getUser())
                        && StringUtils.isNotBlank(httpProxy.getPassword())) {
                    String encoded = Base64.getEncoder().encodeToString((httpProxy.getUser() + ":" + httpProxy.getPassword())
                            .getBytes());
                    connection.setRequestProperty("Proxy-Authorization",
                            "Basic " + encoded);
                }
            } else {
                connection = url.openConnection();
            }
            connection.setConnectTimeout(CONN_TIME_OUT);
            connection.setReadTimeout(READ_TIME_OUT);
            if (requestProperty != null) {
                Set<String> keys = requestProperty.keySet();
                for (String key : keys) {
                    connection
                            .setRequestProperty(key, requestProperty.get(key));
                }
            }
            return (HttpURLConnection) connection;
        } catch (Exception e) {
            log.error("Get:", e);
        }
        return null;
    }

    /**
     * 采用Post方式，以字符串形式发送参数
     *
     * @param urlStr          无参数的完整的url语句
     * @param param           参数，格式为aaa=111&bbb=222.....
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String postByString(String urlStr, String param,
                                      boolean isLineSeparator) {
        return postByString(urlStr, param, Coder.DEFAULT_ENCODING, null, "",
                null, isLineSeparator);
    }

    /**
     * 采用Post方式，以字符串形式发送参数
     *
     * @param urlStr          无参数的完整的url语句
     * @param param           参数，格式为aaa=111&bbb=222.....
     * @param encoding        编码
     * @param referer         请求来源
     * @param cookieVal       cookie值
     * @param proxy           代理对象
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String postByString(String urlStr, String param,
                                      String encoding, String referer, String cookieVal, HttpProxy proxy,
                                      boolean isLineSeparator) {
        Map<String, String> requestProperty = constructRequestProperty(referer, cookieVal);
        return postByString(urlStr, param, encoding, requestProperty, proxy,
                isLineSeparator);
    }

    /**
     * 采用Post方式，以字符串形式发送参数
     *
     * @param urlStr          无参数的完整的url语句
     * @param param           参数，格式为aaa=111&bbb=222.....
     * @param encoding        编码
     * @param requestProperty 请求头
     * @param proxy           代理对象
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String postByString(String urlStr, String param,
                                      String encoding, Map<String, String> requestProperty,
                                      HttpProxy proxy, boolean isLineSeparator) {
        StringBuffer result = new StringBuffer();
        BufferedReader in = null;
        OutputStreamWriter out = null;
        HttpURLConnection httpConn = null;
        try {
            httpConn = post(urlStr, requestProperty, proxy);
            if (StringUtils.isNotBlank(param)) {
                out = new OutputStreamWriter(httpConn.getOutputStream(),
                        encoding);
                out.write(param);
                out.flush();
            }
            in = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream(), encoding));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
                if (isLineSeparator == true) {
                    result.append(StringUtils.getLineSeparator());
                }
            }
        } catch (Exception e) {
            log.error("PostByString:", e);
        } finally {
            StreamUtils.close(out, in);
            if (httpConn != null) {
                httpConn.disconnect();
                httpConn = null;
            }
        }
        return result.toString();
    }

    /**
     * 采用Post方式，以字节数组形式发送参数
     *
     * @param urlStr          无参数的完整的url语句
     * @param buffer          指定传送内容的Buffer
     * @param len             指定缓冲区的大小
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String postByBinary(String urlStr, byte[] buffer, int len,
                                      boolean isLineSeparator) {
        return postByBinary(urlStr, buffer, len, Coder.DEFAULT_ENCODING, null,
                "", null, isLineSeparator);
    }

    /**
     * 采用Post方式，以字节数组形式发送参数
     *
     * @param urlStr          无参数的完整的url语句
     * @param buffer          指定传送内容的Buffer
     * @param len             指定缓冲区的大小
     * @param encoding        编码
     * @param referer         请求来源
     * @param cookieVal       cookie值
     * @param proxy           代理对象
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String postByBinary(String urlStr, byte[] buffer, int len,
                                      String encoding, String referer, String cookieVal, HttpProxy proxy,
                                      boolean isLineSeparator) {
        Map<String, String> requestProperty = constructRequestProperty(referer, cookieVal);
        return postByBinary(urlStr, buffer, len, encoding, requestProperty,
                proxy, isLineSeparator);
    }

    /**
     * 采用Post方式，以字节数组形式发送参数
     *
     * @param urlStr          无参数的完整的url语句
     * @param buffer          指定传送内容的Buffer
     * @param len             指定缓冲区的大小
     * @param encoding        编码
     * @param requestProperty 请求头
     * @param proxy           代理对象
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String postByBinary(String urlStr, byte[] buffer, int len,
                                      String encoding, Map<String, String> requestProperty,
                                      HttpProxy proxy, boolean isLineSeparator) {
        StringBuffer result = new StringBuffer();
        BufferedOutputStream wr = null;
        BufferedReader rd = null;
        HttpURLConnection httpConn = null;
        try {
            httpConn = post(urlStr, requestProperty, proxy);
            wr = new BufferedOutputStream(httpConn.getOutputStream(), len);
            wr.write(buffer, 0, buffer.length);
            wr.flush();
            // Get the response
            rd = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream(), encoding));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
                if (isLineSeparator == true) {
                    result.append(StringUtils.getLineSeparator());
                }
            }
        } catch (Exception e) {
            log.error("PostByBinary:", e);
        } finally {
            StreamUtils.close(wr, rd);
            if (httpConn != null) {
                httpConn.disconnect();
                httpConn = null;
            }
        }
        return result.toString();
    }

    /**
     * 采用Post方式
     *
     * @param urlStr          无参数的完整的url语句
     * @param requestProperty 请求头
     * @param httpProxy       代理对象
     * @return 返回Web Server的连接
     */
    public static HttpURLConnection post(String urlStr,
                                         Map<String, String> requestProperty, HttpProxy httpProxy) {
        try {
            URL url = new URL(urlStr);
            setTimeOut();
            ignoreSsl(url);
            HttpURLConnection httpConn = null;
            if (httpProxy != null) {
                SocketAddress addr = new InetSocketAddress(httpProxy.getHost(),
                        httpProxy.getPort());
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                httpConn = (HttpURLConnection) url.openConnection(proxy);
                if (StringUtils.isNotBlank(httpProxy.getUser())
                        && StringUtils.isNotBlank(httpProxy.getPassword())) {
                    String encoded = Base64.getEncoder().encodeToString((httpProxy.getUser() + ":" + httpProxy.getPassword())
                            .getBytes());
                    httpConn.setRequestProperty("Proxy-Authorization", "Basic "
                            + encoded);
                }
            } else {
                httpConn = (HttpURLConnection) url.openConnection();
            }
            httpConn.setConnectTimeout(CONN_TIME_OUT);
            httpConn.setReadTimeout(READ_TIME_OUT);
            if (requestProperty != null) {
                Set<String> keys = requestProperty.keySet();
                for (String key : keys) {
                    httpConn.setRequestProperty(key, requestProperty.get(key));
                }
            }
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            return httpConn;
        } catch (Exception e) {
            log.error("Post:", e);
        }
        return null;
    }

    /**
     * 模拟表单提交，可以起到上传文件的作用
     *
     * @param urlStr          提交地址
     * @param textMap         模拟表单提交的字段
     * @param fileMap         模拟表单提交的文件
     * @param encoding        编码格式
     * @param referer         请求来源
     * @param cookieVal       cookie值
     * @param httpProxy       代理对象
     * @param isLineSeparator 指定从服务器端接收的数据是否每行要添加换行
     * @return 返回Web Server的响应
     */
    public static String formUpload(String urlStr, Map<String, String> textMap,
                                    Map<String, String> fileMap, String encoding, String referer,
                                    String cookieVal, HttpProxy httpProxy, boolean isLineSeparator) {
        StringBuffer result = new StringBuffer();
        OutputStream out = null;
        BufferedReader reader = null;
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(urlStr);
            setTimeOut();
            ignoreSsl(url);
            if (httpProxy != null) {
                SocketAddress addr = new InetSocketAddress(httpProxy.getHost(),
                        httpProxy.getPort());
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                httpConn = (HttpURLConnection) url.openConnection(proxy);
                if (StringUtils.isNotBlank(httpProxy.getUser())
                        && StringUtils.isNotBlank(httpProxy.getPassword())) {
                    String encoded = Base64.getEncoder().encodeToString((httpProxy.getUser() + ":" + httpProxy.getPassword())
                            .getBytes());
                    httpConn.setRequestProperty("Proxy-Authorization", "Basic "
                            + encoded);
                }
            } else {
                httpConn = (HttpURLConnection) url.openConnection();
            }
            httpConn.setConnectTimeout(CONN_TIME_OUT);
            httpConn.setReadTimeout(READ_TIME_OUT);
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Accept", "*/*");
            httpConn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            httpConn.setRequestProperty("User-Agent", USER_AGENT);
            httpConn.setRequestProperty("Charset", encoding);
            if (StringUtils.isNotBlank(referer)) {
                httpConn.setRequestProperty("Referer", referer);
            }
            if (StringUtils.isNotBlank(cookieVal)) {
                // 发送cookie信息上去，以表明自己的身份，否则会被认为没有权限
                httpConn.setRequestProperty("Cookie", cookieVal);
            }
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            // post方式不推荐使用缓存
            httpConn.setUseCaches(false);
            // 模拟上传文件的逻辑代码
            // 设置边界
            String BOUNDARY = "----WebKitFormBoundary"
                    + System.currentTimeMillis();
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);
            // 请求正文信息
            out = new DataOutputStream(httpConn.getOutputStream());
            // 第一部分，表单字段
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator<Entry<String, String>> iter = textMap.entrySet()
                        .iterator();
                while (iter.hasNext()) {
                    Entry<String, String> entry = iter
                            .next();
                    String inputName = entry.getKey();
                    String inputValue = entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    strBuf.append("\r\n").append("--").append(BOUNDARY)
                            .append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\""
                            + inputName + "\"\r\n\r\n");
                    strBuf.append(inputValue);
                }
                out.write(strBuf.toString().getBytes());
            }
            // 第二部分，表单文件
            if (fileMap != null) {
                Iterator<Entry<String, String>> iter = fileMap.entrySet()
                        .iterator();
                while (iter.hasNext()) {
                    Entry<String, String> entry = iter
                            .next();
                    String inputName = entry.getKey();
                    String inputValue = entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    File file = new File(inputValue);
                    String filename = file.getName();
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
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(BOUNDARY)
                            .append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\""
                            + inputName + "\"; filename=\"" + filename
                            + "\"\r\n");
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                    out.write(strBuf.toString().getBytes());
                    DataInputStream in = null;
                    try {
                        in = new DataInputStream(new FileInputStream(file));
                        int bytes = 0;
                        byte[] bufferOut = new byte[1024];
                        while ((bytes = in.read(bufferOut)) != -1) {
                            out.write(bufferOut, 0, bytes);
                        }
                    } finally {
                        StreamUtils.close(in);
                    }
                }
            }
            // 第三部分，结尾部分
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream(), encoding));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                if (isLineSeparator == true) {
                    result.append(StringUtils.getLineSeparator());
                }
            }
        } catch (Exception e) {
            log.error("FormUpload:", e);
        } finally {
            StreamUtils.close(out, reader);
            if (httpConn != null) {
                httpConn.disconnect();
                httpConn = null;
            }
        }
        return result.toString();
    }

    /**
     * 下载文件
     *
     * @param targetURL     网络文件路径
     * @param localFilePath 保存文件到本地目录路径
     * @param defaultName   文件名称
     * @param referer       referer
     * @param cookieVal     cookie
     * @param httpProxy     代理
     * @return 是否下载成功
     */
    public static boolean downloadFile(String targetURL, String localFilePath,
                                       String defaultName, String referer, String cookieVal, HttpProxy httpProxy) {
        boolean result = true;
        DataInputStream is = null;
        FileOutputStream out = null;
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(targetURL);
            setTimeOut();
            ignoreSsl(url);
            if (httpProxy != null) {
                SocketAddress addr = new InetSocketAddress(httpProxy.getHost(),
                        httpProxy.getPort());
                Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
                httpConn = (HttpURLConnection) url.openConnection(proxy);
                if (StringUtils.isNotBlank(httpProxy.getUser())
                        && StringUtils.isNotBlank(httpProxy.getPassword())) {
                    String encoded = Base64.getEncoder().encodeToString((httpProxy.getUser() + ":" + httpProxy.getPassword())
                            .getBytes());
                    httpConn.setRequestProperty("Proxy-Authorization", "Basic "
                            + encoded);
                }
            } else {
                httpConn = (HttpURLConnection) url.openConnection();
            }
            httpConn.setConnectTimeout(CONN_TIME_OUT);
            httpConn.setReadTimeout(READ_TIME_OUT);
            httpConn.setRequestProperty("Connection", "Keep-Alive");
            httpConn.setRequestProperty("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            httpConn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
            httpConn.setRequestProperty("User-Agent", USER_AGENT);
            if (StringUtils.isNotBlank(referer)) {
                httpConn.setRequestProperty("Referer", referer);
            }
            if (StringUtils.isNotBlank(cookieVal)) {
                // 发送cookie信息上去，以表明自己的身份，否则会被认为没有权限
                httpConn.setRequestProperty("Cookie", cookieVal);
            }
            String encoding = httpConn.getContentEncoding();
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                is = new DataInputStream(new GZIPInputStream(
                        httpConn.getInputStream()));
            } else {
                is = new DataInputStream(httpConn.getInputStream());
            }
            // 构造目录
            localFilePath = FileUtils.processFileSeparator(localFilePath);
            File dir = new File(localFilePath);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    return false;
                }
            }
            Map<String, List<String>> headerFields = httpConn.getHeaderFields();
            List<String> headerValues = headerFields.get("Content-Disposition");
            if (headerValues != null) {
                for (String headerValue : headerValues) {
                    if (headerValue.startsWith("attachment; filename=")) {
                        try {
                            String filename = new String(headerValue
                                    .substring(
                                            ("attachment; filename=").length())
                                    .replace("\"", "").getBytes("iso-8859-1"),
                                    Coder.DEFAULT_ENCODING);
                            out = new FileOutputStream(localFilePath
                                    + File.separator + filename);
                        } catch (UnsupportedEncodingException ue) {

                        }
                    }
                }
            }
            if (out == null) {
                File file = new File(StringUtils.joinStr(localFilePath,
                        File.separator, defaultName));
                dir = file.getParentFile();
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        return false;
                    }
                }
                out = new FileOutputStream(file);
            }
            byte[] buffer = new byte[1024 * 1000];
            int read = -1;
            while ((read = is.read(buffer)) != -1) {
                if (read > 0) {
                    byte[] chunk = new byte[read];
                    System.arraycopy(buffer, 0, chunk, 0, read);
                    out.write(chunk);
                }
            }
        } catch (Exception e) {
            log.error("DownloadFile:", e);
            result = false;
        } finally {
            StreamUtils.close(is, out);
            if (httpConn != null) {
                httpConn.disconnect();
                httpConn = null;
            }
        }
        return result;
    }

    /**
     * 获取链接对应的Cookie值
     *
     * @param urlStr 网络接口地址
     * @param param  cookie键
     * @return cookie值
     */
    public static String getCookieVal(String urlStr, String param) {
        StringBuffer result = new StringBuffer();
        OutputStreamWriter out = null;
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(urlStr);
            setTimeOut();
            ignoreSsl(url);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(CONN_TIME_OUT);
            httpConn.setReadTimeout(READ_TIME_OUT);
            httpConn.setRequestProperty("User-Agent", USER_AGENT);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");
            if (StringUtils.isNotBlank(param)) {
                out = new OutputStreamWriter(httpConn.getOutputStream(),
                        Coder.DEFAULT_ENCODING);
                out.write(param);
                out.flush();
            }
            List<String> cookies = httpConn.getHeaderFields().get("Set-Cookie");
            if (cookies != null) {
                for (String str : cookies) {
                    if (StringUtils.isNotBlank(str)) {
                        str = str.substring(0, str.indexOf(";") + 1);
                    }
                    result.append(str);
                }
            }
        } catch (Exception e) {
            log.error("GetCookieVal:", e);
            return null;
        } finally {
            StreamUtils.close(out);
            if (httpConn != null) {
                httpConn.disconnect();
                httpConn = null;
            }
        }
        return result.toString();
    }

    /**
     * 启动Cookie管理策略，执行该方法后，将每次访问的Cookie信息都保存起来，以便模拟登录
     */
    public static void startCookieManager() {
        CookieHandler.setDefault(new CookieManager(null,
                CookiePolicy.ACCEPT_ALL));
    }

    /**
     * 忽略SSL，信任所有证书
     *
     * @param url 网络接口
     * @throws Exception
     */
    public static void ignoreSsl(URL url) throws Exception {
        if ("https".equalsIgnoreCase(url.getProtocol())) {
            SslUtils.ignoreSsl();
        }
    }

}
