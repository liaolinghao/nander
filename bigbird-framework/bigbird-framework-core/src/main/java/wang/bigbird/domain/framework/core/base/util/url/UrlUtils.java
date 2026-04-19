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

import com.google.common.net.InternetDomainName;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.StringUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL解析工具
 *
 * @author Bigbird
 */
public class UrlUtils {

    /**
     * IP匹配组
     */
    private static final Pattern PATTEN_IP = Pattern
            .compile("((\\d+\\.){3}(\\d+))");

    /**
     * 多级域名提取前缀
     */
    private static final String RE_TOP_LEVEL_PREFIX = "([0-9A-Za-z\\-\u4e00-\u9fa5]+\\.){";
    private static final String RE_TOP_LEVEL_SUFFIX = "}";

    /**
     * 获取URL服务点地址，包含主机和对应服务端口
     *
     * @param urlInfo url地址信息
     * @return 完整的URL服务点地址
     * @throws MalformedURLException
     */
    public static String getServicePoint(String urlInfo) throws MalformedURLException {
        URL url = createUrl(urlInfo);
        String protocol = url.getProtocol();
        String host = url.getHost();
        int port = url.getPort();
        StringBuffer servicePoint = new StringBuffer();
        if (urlInfo.startsWith(protocol)) {
            servicePoint.append(protocol).append("://");
        }
        if (urlInfo.contains(host)) {
            servicePoint.append(host);
        }
        if (port >= 0 && urlInfo.contains(CommonConstants.COLON + port)) {
            servicePoint.append(CommonConstants.COLON).append(port);
        }
        return servicePoint.toString();
    }

    /**
     * 获取URL中域名后面的完整路径（包含?后面的参数）
     *
     * @param urlInfo url地址信息
     * @return 域名后面的完整路径（包含?后面的参数）
     * @throws MalformedURLException
     */
    public static String getFullPathAfterDomain(String urlInfo) throws MalformedURLException {
        URL url = createUrl(urlInfo);
        String path = url.getPath();
        String query = url.getQuery();
        if (query != null && !query.isEmpty()) {
            return path + "?" + query;
        }
        return path;
    }

    /**
     * 获取网页地址对应的协议
     *
     * @param urlInfo
     * @return
     * @throws MalformedURLException
     */
    public static String getProtocol(String urlInfo)
            throws MalformedURLException {
        URL url = createUrl(urlInfo);
        return url.getProtocol();
    }

    /**
     * 提取指定层级的域名
     *
     * @param urlInfo
     * @param level   0表示完整域名，1表示1级域名，2表示2级域名，依次类推，但是一般不超过5级域名
     * @return
     * @throws MalformedURLException
     */
    public static String getDomain(String urlInfo, int level)
            throws MalformedURLException {
        URL url = createUrl(urlInfo);
        String host = url.getHost();
        if (level <= 0) {
            return host;
        }
        Matcher matcher = PATTEN_IP.matcher(host);
        if (matcher.find()) {
            return matcher.group();
        }
        // 一级域名
        String privateDomain = InternetDomainName.from(host).topPrivateDomain()
                .toString();
        if (level == 1) {
            return privateDomain;
        }
        Pattern pattern = Pattern.compile(StringUtils.joinStr(
                RE_TOP_LEVEL_PREFIX, level - 1, RE_TOP_LEVEL_SUFFIX,
                privateDomain));
        matcher = pattern.matcher(host);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    /**
     * 获取链接中指定的参数，如果参数为空，则取默认值
     *
     * @param url
     * @param parameter
     * @param defaultValue
     * @return
     */
    public static String getParameter(String url, String parameter,
                                      String defaultValue) {
        url = Coder.urlDecoderUtf8(url);
        if (url.indexOf(CommonConstants.QUESTION_MARK) != -1) {
            String contents = url.substring(url.indexOf(CommonConstants.QUESTION_MARK) + 1);
            String[] keyValues = contents.split(CommonConstants.AMP);
            for (int i = 0; i < keyValues.length; i++) {
                int index = keyValues[i].indexOf(CommonConstants.EQUAL);
                if (index != -1) {
                    String key = keyValues[i].substring(0, index);
                    String value = keyValues[i].substring(index + 1);
                    if (key.equalsIgnoreCase(parameter)) {
                        if (StringUtils.isBlank(value)) {
                            return defaultValue;
                        }
                        return value;
                    }
                }
            }
        }
        return defaultValue;
    }

    /**
     * 判断一个链接地址是否标识网络文件
     *
     * @param urlInfo
     * @return
     * @throws Exception
     */
    public static boolean isFileLink(String urlInfo) throws Exception {
        HttpURLConnection urlconnection = null;
        try {
            URL url = createUrl(urlInfo);
            urlconnection = (HttpURLConnection) url.openConnection();
            String fileType = urlconnection.getContentType();
            if (StringUtils.isNotBlank(fileType)) {
                return !fileType.contains("text/html");
            }
        } finally {
            if (urlconnection != null) {
                urlconnection.disconnect();
            }
        }
        return false;
    }

    private static URL createUrl(String urlInfo) throws MalformedURLException {
        URL url;
        try {
            url = new URL(urlInfo);
        } catch (MalformedURLException mue) {
            url = new URL(StringUtils.joinStr("http://", urlInfo));
        }
        return url;
    }

}
