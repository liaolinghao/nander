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

import org.apache.commons.codec.digest.DigestUtils;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.core.base.util.url.UrlUtils;
import wang.bigbird.domain.framework.server.core.exception.BusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名工具类，该类用于网站对外接口调用的安全处理。 为了防篡改、防止调用方抵赖等安全需求的API（如验证应用授权），需要采用MD5方式对API进行签名。
 * <p>
 * 1）MD5签名计算方式
 * <p>
 * MD5(签名字符串&APP_SECRET).toUpperCase() MD5大写字母
 * <p>
 * 2）签名字符串
 * <p>
 * 签名字符串 = URI&排序的参数列表&请求体，参数列表包含appKey
 * <p>
 * 签名字符串使用UTF-8编码。
 * <p>
 * URI：API的URI（从host结束到？之前）。注意，后端需要获得并使用NGINX转换前的URL进行计算，以和调用方保持签名结果一致。如果URI包含参数
 * ，需使用参数值替换后的实际请求URI。
 * <p>
 * 参数列表：除signature外的所有请求参数；如果请求头中包含access_token，
 * 则需要将其转换为请求参数的形式添加到参数列表中。参数列表中的所有参数按照参数名称进行文本升序排列，参数之间使用“&”符号链接。
 * <p>
 * 请求体：如果请求体不为空，则需在签名字符串中包含请求体，请求体按照键值升序排序，比如：{"name":"王老师","sex":"男","age":18}，排序后：{"age":18,"name":"王老师","sex":"男"}。
 * <p>
 * APP_SECRET：开发者中心分发给应用的密钥。
 * <p>
 * 3）签名实例
 * <p>
 * 请求：https://api.xxx.com/xxx/xxx?appKey={appKey}&signature={signature
 * } RequestHeader: Authorization: Bearer {accesstoken}
 * <p>
 * 签名字符串： /xxx/xxx&{accesstoken}&{appKey}&{body}&{appsecret}
 * <p>
 * 参数名排序：参数名文本字典序升序排列，参数名大小写敏感。如：
 * 字符串：["ax","abc","Acd","D","cDf6","123","1Dc","Ac","_ax"]
 * 排序后：["123","1Dc","Ac","Acd","D","_ax","abc","ax","cDf6"]
 * <p>
 * 4）签名传输方式
 * <p>
 * 调用方可以两种方式提交签名： 在API的请求头（Request Header）中添加signature请求头： signature:｛MD5签名｝
 * 在API的参数列表中添加signature参数： signature=｛MD5签名｝
 *
 * @author Bigbird
 */
public class SignatureUtils {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final int AUTH_HEADER_PARTS_COUNT = 2;

    private static final String SIGNATURE_PARAM_NAME = "signature";

    private static final String ACCESSTOKEN_PARAM_NAME = "accessToken";

    /**
     * 获取接口签名值，该方法提供给接口调用方获取接口签名
     *
     * @param urlInfo   请求url，指完整的接口地址
     * @param params    请求参数
     * @param headers   提取请求头中包含的accessToken并加入到签名字符串运算规则中
     * @param jsonBody  请求的json体
     * @param appSecret 分配的app密钥
     * @return 接口签名值
     */
    public static String signRequest(String urlInfo,
                                     Map<String, String> params, Map<String, String> headers,
                                     String jsonBody, String appSecret) throws MalformedURLException {
        String servicePoint = UrlUtils.getServicePoint(urlInfo);
        int index = urlInfo.indexOf(CommonConstants.QUESTION_MARK);
        String preUrl;
        if (index != -1) {
            preUrl = urlInfo.substring(0, index);
        } else {
            preUrl = urlInfo;
        }
        //去除域名
        preUrl = preUrl.substring(servicePoint.length());
        Map<String, String> requestParams = new TreeMap<>();
        String param = urlInfo.substring(urlInfo.indexOf(CommonConstants.QUESTION_MARK) + 1);
        String[] paramArray = param.split(CommonConstants.AMP);
        for (int i = 0; i < paramArray.length; i++) {
            String[] p = paramArray[i].split(CommonConstants.EQUAL);
            if (p.length == 2) {
                requestParams.put(p[0], p[1]);
            }
        }
        if (params != null) {
            requestParams.putAll(params);
        }
        return getMd5SignData(preUrl, requestParams, headers,
                jsonBody, appSecret);
    }

    /**
     * 获取接口签名值，该方法提供给接口服务方获取接口签名
     *
     * @param api       请求接口标识
     * @param params    请求参数
     * @param headers   提取请求头中包含的accessToken并加入到签名字符串运算规则中
     * @param jsonBody  请求的json体
     * @param appSecret 分配的app密钥
     * @return 接口签名值
     */
    public static String getMd5SignData(String api, Map<String, String> params, Map<String, String> headers,
                                        String jsonBody, String appSecret) {
        // 拼接请求字符串
        String signData = getSignData(api, params, headers,
                jsonBody, appSecret);
        // 将拼接后字符串转为md5
        return getMd5Hex(signData);
    }

    /**
     * 拼接字符串
     *
     * @param api       请求接口标识
     * @param params    请求参数
     * @param headers   提取请求头中包含的accessToken并加入到签名字符串运算规则中
     * @param jsonBody  请求的json体
     * @param appSecret 分配的app密钥
     * @return 待签名字符串
     */
    public static String getSignData(String api, Map<String, String> params,
                                     Map<String, String> headers, String jsonBody, String appSecret) {
        // 对参数进行排序
        Map<String, String> sortedParams = getSortedParamMap(params);
        // 从header中获取token
        String accessToken = headers == null ? null : getAccessToken(headers);
        // 若token不为空，参与排序
        if (StringUtils.isNotBlank(accessToken)) {
            sortedParams.put(ACCESSTOKEN_PARAM_NAME, accessToken);
        }
        // 以&为连接符拼接排序后参数值
        StringBuilder sb = new StringBuilder(api);
        // sortedParams里面均为有值的key-value对
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!SIGNATURE_PARAM_NAME.equals(key)) {
                // signature不参与签名运算
                sb.append(CommonConstants.AMP).append(value);
            }
        }
        // 若存在请求体json，拼接json
        if (StringUtils.isNotBlank(jsonBody)) {
            // 转换为json对象后，排序压缩，以防止编码不一致
            sb.append(CommonConstants.AMP).append(StringUtils.compress(JsonUtils.sortJson(jsonBody)));
        }
        // 最后拼接secret
        sb.append(CommonConstants.AMP).append(appSecret);
        return sb.toString();
    }

    /**
     * 获取字符串md5值
     *
     * @param signData 待签名的字符串
     * @return MD5签名后字符串
     */
    private static String getMd5Hex(String signData) {
        byte[] bytes;
        try {
            bytes = signData.getBytes(Coder.DEFAULT_ENCODING);
            String signMsg = DigestUtils.md5Hex(bytes);
            String sign = signMsg.toUpperCase();
            return sign;
        } catch (UnsupportedEncodingException e) {
            throw BusinessException.of(IBaseResponseStatus.UNSUPPORTED_ENCODING, Coder.DEFAULT_ENCODING);
        }
    }

    /**
     * 对请求参数进行排序
     *
     * @param params 待排序的请求参数
     * @return 自然排序后的请求参数
     */
    private static Map<String, String> getSortedParamMap(
            Map<String, String> params) {
        Map<String, String> sortedParams = new TreeMap<>();
        if (params == null) {
            return sortedParams;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isNotBlank(entry.getValue())) {
                sortedParams.put(key, entry.getValue());
            }
        }
        return sortedParams;
    }

    /**
     * 从header中获取accessToken
     *
     * @param headers 请求头
     * @return accessToken
     * @throws SecurityException
     */
    private static String getAccessToken(Map<String, String> headers) {
        if (headers == null || headers.get(AUTHORIZATION_HEADER) == null) {
            return null;
        }
        // 格式：Authorization: Bearer {accesstoken}
        String authorization = headers.get(AUTHORIZATION_HEADER);
        String[] accessTokenArray = authorization.split(" ");
        if (accessTokenArray.length != AUTH_HEADER_PARTS_COUNT) {
            throw BusinessException.of(IBaseResponseStatus.PARAMETERS_ANOMALIES, "Invalid Authorization header format. Expected 'Bearer {token}' but got: " + authorization);
        }
        return accessTokenArray[1];
    }

}
