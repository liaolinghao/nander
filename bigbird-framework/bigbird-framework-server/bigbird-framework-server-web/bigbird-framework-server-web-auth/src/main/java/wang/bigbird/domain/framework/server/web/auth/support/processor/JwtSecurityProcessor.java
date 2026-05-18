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
package wang.bigbird.domain.framework.server.web.auth.support.processor;

import cn.hutool.core.codec.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import wang.bigbird.domain.framework.core.base.constant.CommonConstants;
import wang.bigbird.domain.framework.core.base.tool.Coder;
import wang.bigbird.domain.framework.core.base.util.CollectionUtils;
import wang.bigbird.domain.framework.core.base.util.DateUtils;
import wang.bigbird.domain.framework.core.base.util.JsonUtils;
import wang.bigbird.domain.framework.core.base.util.StringUtils;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisService;
import wang.bigbird.domain.framework.data.redis.service.base.IRedisSortedSetService;
import wang.bigbird.domain.framework.server.core.exception.BusinessException;
import wang.bigbird.domain.framework.server.core.support.response.IBaseResponseStatus;
import wang.bigbird.domain.framework.server.web.auth.base.enums.MutexTypeEnum;
import wang.bigbird.domain.framework.server.web.auth.base.tool.SimpleGrantedAuthorityDeserializer;
import wang.bigbird.domain.framework.server.web.auth.config.property.JwtSecurityProperties;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.AccessTokenAuthData;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.JwtAuthData;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.JwtToken;
import wang.bigbird.domain.framework.server.web.auth.domain.pojo.user.JwtUser;
import wang.bigbird.domain.framework.server.web.auth.exception.DisposedJwtException;
import wang.bigbird.domain.framework.server.web.auth.service.base.IAppKeyAndSecretLoaderService;
import wang.bigbird.domain.framework.server.web.auth.service.base.IAppSecretLoaderService;
import wang.bigbird.domain.framework.server.web.auth.service.base.INonStandardJwtParserService;
import wang.bigbird.domain.framework.server.web.core.base.enums.ChannelEnum;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JWT token创建与解析工具
 * <p>
 * 在本模块中设计了一种对应关系用于记录：用户与refresh token，token与refresh token的映射。
 * 其中，用户-->refresh token是一对多的关系，token-->refresh token是一对一的关系。
 * 本模块采用redis有序set来记录：用户-->refresh token这种关系，
 * 当发生用户权限变更需要强制使对应用户的token失效时，
 * 可通过删除用户-->refresh token这种关系以达到目的，
 * 另外在redis有序set中可将refresh token的签发时间作为score，
 * 以便查询refresh token的签发时间。
 *
 * @author Bigbird
 */
@Slf4j
@Component("jwtSecurityProcessor")
public class JwtSecurityProcessor implements InitializingBean {

    /**
     * AccessToken 主题
     */
    private static final String ACCESS_TOKEN_SUBJECT = "access";
    /**
     * RefreshToken 主题
     */
    private static final String REFRESH_TOKEN_SUBJECT = "refresh";
    /**
     * token中持有的认证对象Key
     */
    private static final String AUTH_OBJECT_KEY = "auth_object";
    /**
     * access token与refresh token对应的ID长度
     */
    private static final int ID_LENGTH = 32;
    /**
     * 默认应用键，当不属于接入应用请求时，采用该值
     */
    private static final String DEFAULT_APP = "default";
    /**
     * 该槽下存放access token的ID与值之间的映射关系
     */
    private static final String ACCESS_TOKEN_ID2VALUE_KEY_MARK = "tk:access:id2value:";
    /**
     * 该槽下存放refresh token的ID与值之间的映射关系
     */
    private static final String REFRESH_TOKEN_ID2VALUE_KEY_MARK = "tk:refresh:id2value:";
    /**
     * 该槽下存放access token与refresh token之间的ID映射关系
     */
    private static final String ACCESS_TOKEN_ID2RID_KEY_MARK = "tk:access:id2rid:";
    /**
     * 该槽下存放某一个认证对象在某个应用获得的所有refresh token id
     */
    private static final String REFRESH_TOKEN_CREDENTIAL_IDS_KEY_MARK = "tk:refresh:credential:";
    /**
     * 该槽下存放某一个认证对象通过某个渠道在某个应用登录获得的refresh token id
     */
    private static final String REFRESH_TOKEN_CREDENTIAL_ID_KEY_MARK = "tk:refresh:credential:";
    /**
     * 该槽下存放refresh token的ID与认证标识之间的映射关系
     */
    private static final String REFRESH_TOKEN_ID2CREDENTIAL_KEY_MARK = "tk:refresh:id2credential:";

    @Autowired
    private IRedisService redisService;
    @Autowired
    private IRedisSortedSetService redisSortedSetService;

    /**
     * 非标准JWT解析器
     */
    @Autowired(required = false)
    private INonStandardJwtParserService nonStandardJwtParserService;
    /**
     * AppSecret获取服务
     */
    @Autowired(required = false)
    private IAppSecretLoaderService appSecretLoaderService;

    /**
     * AppKeyAndSecret获取服务
     */
    @Autowired(required = false)
    private IAppKeyAndSecretLoaderService appKeyAndSecretLoaderService;

    private final JwtSecurityProperties jwtSecurityProperty;

    private Key key;

    private final ObjectMapper objectMapper;

    public JwtSecurityProcessor(JwtSecurityProperties jwtSecurityProperty) {
        this.jwtSecurityProperty = jwtSecurityProperty;
        this.objectMapper = JsonUtils.getRegisterMapper();
        objectMapper.registerModule(new SimpleModule().addDeserializer(
                SimpleGrantedAuthority.class, new SimpleGrantedAuthorityDeserializer()));
    }

    @Override
    public void afterPropertiesSet() {
        if (isEnableJwtSecurity()) {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecurityProperty.getBase64Secret());
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /**
     * 是否启用认证授权机制
     *
     * @return 是否启用认证授权机制
     */
    public boolean isEnableJwtSecurity() {
        return jwtSecurityProperty.isEnable();
    }

    /**
     * 获取不需要认证的接口列表
     *
     * @return 不需要认证的接口列表
     */
    public String[] getWithoutJwtSecurityApi() {
        StringBuffer sb = new StringBuffer();
        sb.append("/favicon.ico,");
        String withoutApi = jwtSecurityProperty.getWithoutApi();
        if (StringUtils.isNotBlank(withoutApi)) {
            sb.append(withoutApi.trim());
        }
        String[] apis = sb.toString().split(",");
        return CollectionUtils.unique(apis).toArray(new String[]{});
    }

    /**
     * 创建jwt refresh token
     *
     * @param deviceId                   登录设备ID，用于同设备反复登录不被限制
     * @param jwtAuthData                JWT认证权限数据
     * @param refreshTokenValidityInDays refreshToken有效期，以天为单位
     * @param mutexTypeEnum              登录互斥类型
     * @param appKeyAndSecret            组成格式：appKey:appSecret，为空则采用默认密钥生成签名key
     * @return refresh token
     */
    public JwtToken createRefreshToken(String deviceId, JwtAuthData jwtAuthData, Integer refreshTokenValidityInDays, MutexTypeEnum mutexTypeEnum, String appKeyAndSecret) {
        if (mutexTypeEnum == null) {
            // 为空，就不做互斥限制
            mutexTypeEnum = MutexTypeEnum.none;
        }
        ChannelEnum channel = jwtAuthData.getChannel();
        if (mutexTypeEnum.equals(MutexTypeEnum.front_all) || mutexTypeEnum.equals(MutexTypeEnum.back_all)) {
            // 不考虑渠道，那么把渠道值修正为忽略，以便统一标识
            channel = ChannelEnum.IGNORE;
        }
        String appKey = StringUtils.isBlank(appKeyAndSecret) ? DEFAULT_APP : appKeyAndSecret.substring(0, appKeyAndSecret.indexOf(CommonConstants.COLON));
        String awardRefreshTokenIdKey = getAwardRefreshTokenIdKey(appKey, channel, jwtAuthData.getType(), jwtAuthData.getId());
        String oldRefreshTokenId = redisService.get(awardRefreshTokenIdKey);
        if (StringUtils.isNotBlank(oldRefreshTokenId) && mutexTypeEnum.isFrontProtected()) {
            // 只有保护先登录的场景下需要判断登录是否来源于同一个认证标识
            // 如果是，就放行，不是就拒绝
            boolean refuseLogin = true;
            if (StringUtils.isNotBlank(deviceId)) {
                if (getCredentialLoginId(appKey, channel, jwtAuthData.getType(), jwtAuthData.getId(), deviceId).equals(redisService.get(getRefreshTokenId2CredentialKey(oldRefreshTokenId)))) {
                    // 同一个设备，放行
                    refuseLogin = false;
                }
            }
            if (refuseLogin) {
                // 先登录有效，拒绝后登录
                throw BusinessException.of(IBaseResponseStatus.HAS_LOGIN);
            }
        }
        // token ID 每次采用一个唯一ID，以便后期通过该ID对相关token执行失效处理
        String newRefreshTokenId = StringUtils.getUuid();
        Date date = new Date();
        Date expiration = DateUtils.addDays(date, refreshTokenValidityInDays != null ? refreshTokenValidityInDays : jwtSecurityProperty.getRefreshTokenValidityInDays());
        String rtk = Jwts.builder()
                // token ID
                .setId(newRefreshTokenId)
                // 签发时间
                .setIssuedAt(date)
                // 签发者
                .setIssuer(jwtSecurityProperty.getIssuer())
                // 刷新token主题
                .setSubject(REFRESH_TOKEN_SUBJECT)
                .claim(AUTH_OBJECT_KEY, JsonUtils.object2Json(jwtAuthData, objectMapper))
                // 过期时间
                .setExpiration(expiration)
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(loadSigningKey(appKeyAndSecret), SignatureAlgorithm.HS512)
                .compact();
        Boolean kickPreviousLogin = false;
        String kickDeviceId = "";
        String awardRefreshTokenIdsKey = getAwardRefreshTokenIdsKey(appKey, jwtAuthData.getType(), jwtAuthData.getId());
        if (mutexTypeEnum.isBackProtected()) {
            if (StringUtils.isNotBlank(oldRefreshTokenId)) {
                String credentialLoginId = redisService.get(getRefreshTokenId2CredentialKey(oldRefreshTokenId));
                // 删除旧的refresh token记录
                removeRefreshToken(appKey, channel, jwtAuthData.getType(), jwtAuthData.getId(), oldRefreshTokenId);
                boolean sameDevice = false;
                if (StringUtils.isNotBlank(credentialLoginId) && StringUtils.isNotBlank(deviceId)) {
                    if (getCredentialLoginId(appKey, channel, jwtAuthData.getType(), jwtAuthData.getId(), deviceId).equals(credentialLoginId)) {
                        sameDevice = true;
                    }
                }
                if (!sameDevice) {
                    // 不是相同设备才提示踢掉线
                    kickPreviousLogin = true;
                    if (StringUtils.isNotBlank(credentialLoginId)) {
                        kickDeviceId = loadDeviceIdByCredentialLoginId(credentialLoginId);
                    }
                }
            }
        }
        long expire = DateUtils.secondsBetween(date, expiration);
        if (!mutexTypeEnum.equals(MutexTypeEnum.none)) {
            // 只有需要考虑互斥，才需要记录每个渠道登录对应的refresh token id
            redisService.set(awardRefreshTokenIdKey, newRefreshTokenId, expire, TimeUnit.SECONDS);
            if (StringUtils.isNotBlank(deviceId)) {
                // 记录refresh token id对应的认证标识
                redisService.set(getRefreshTokenId2CredentialKey(newRefreshTokenId), getCredentialLoginId(appKey, channel, jwtAuthData.getType(), jwtAuthData.getId(), deviceId), expire, TimeUnit.SECONDS);
            }
        }
        // 将每个认证对象在每个应用颁发的refresh token进行记录，以方便后续踢下线
        redisSortedSetService.zadd(awardRefreshTokenIdsKey, date.getTime(), newRefreshTokenId);
        redisService.set(getRefreshTokenId2ValueKey(newRefreshTokenId), rtk, expire, TimeUnit.SECONDS);
        return new JwtToken(newRefreshTokenId, rtk, expiration.getTime(), kickPreviousLogin, kickDeviceId);
    }

    /**
     * 通过refresh token换取access token
     *
     * @param refreshToken           refresh token
     * @param tokenValidityInSeconds accessToken有效期，以秒为单位
     * @param appKeyAndSecret        组成格式：appKey:appSecret，为空则采用默认密钥生成签名key
     * @return access token
     */
    public JwtToken loadTokenByRefreshToken(String refreshToken, Integer tokenValidityInSeconds, String appKeyAndSecret) {
        refreshToken = convert2RefreshTokenValue(refreshToken);
        Jws<Claims> jwt = getJwtFromToken(refreshToken, appKeyAndSecret);
        Claims claims = jwt.getBody();
        String authObject = getJwtAuthDataFromToken(claims, REFRESH_TOKEN_SUBJECT);
        String appKey = StringUtils.isBlank(appKeyAndSecret) ? DEFAULT_APP : appKeyAndSecret.substring(0, appKeyAndSecret.indexOf(CommonConstants.COLON));
        JwtAuthData jwtAuthData = JsonUtils.json2Object(authObject, JwtAuthData.class, objectMapper);
        String id = claims.getId();
        if (!validateRefreshToken(appKey, jwtAuthData, id)) {
            // 从token中解析权限数据已经验证了有效期，所以这里只有可能强制下线会导致token不存在
            throw new DisposedJwtException("The jwt has been disposed.");
        }
        return createToken(id, authObject, tokenValidityInSeconds, appKeyAndSecret);
    }

    /**
     * 获取认证信息
     *
     * @param token
     * @param appKey
     * @return 认证信息
     */
    public Authentication getAuthentication(String token, String appKey) {
        token = convert2AccessTokenValue(token);
        if (nonStandardJwtParserService != null) {
            // 优先采用非标准JWT解析服务
            Authentication authentication = nonStandardJwtParserService.getAuthentication(token);
            if (authentication != null) {
                return authentication;
            }
        }
        String appKeyAndSecret = null;
        if (StringUtils.isNotBlank(appKey) && appSecretLoaderService != null) {
            String appSecret = appSecretLoaderService.loadAppSecret(appKey);
            appKeyAndSecret = appKey + CommonConstants.COLON + appSecret;
        }
        if (StringUtils.isBlank(appKeyAndSecret) && appKeyAndSecretLoaderService != null) {
            appKeyAndSecret = appKeyAndSecretLoaderService.loadAppKeyAndSecret();
        }
        return getAuthenticationByStandardJwtParserService(token, appKeyAndSecret);
    }

    /**
     * 解析token，获取认证权限基础数据
     *
     * @param token
     * @param appKeyAndSecret 组成格式：appKey:appSecret，为空则采用默认密钥生成签名key
     * @return 认证权限基础数据
     */
    public JwtAuthData getJwtAuthData(String token, String appKeyAndSecret) {
        Jws<Claims> jwt = getJwtFromToken(token, appKeyAndSecret);
        Claims claims = jwt.getBody();
        String authObject = getJwtAuthDataFromToken(claims, ACCESS_TOKEN_SUBJECT);
        return JsonUtils.json2Object(authObject, JwtAuthData.class, objectMapper);
    }

    /**
     * 采用标准JWT解析服务
     *
     * @param token
     * @param appKeyAndSecret 组成格式：appKey:appSecret，为空则采用默认密钥生成签名key
     * @return 认证主体
     */
    private Authentication getAuthenticationByStandardJwtParserService(String token, String appKeyAndSecret) {
        AccessTokenAuthData accessTokenAuthData = getJwtAuthDataFromAccessToken(token, appKeyAndSecret);
        switch (accessTokenAuthData.getJwtAuthData().getType()) {
            case JwtAuthData.USER:
                return loadUserAuthentication(accessTokenAuthData.getAuthObject(), accessTokenAuthData.getId());
            case JwtAuthData.CLIENT:
                return loadClientAuthentication(accessTokenAuthData.getAuthObject(), accessTokenAuthData.getId());
            case JwtAuthData.DEVICE:
            default:
                throw new UnsupportedJwtException("Unsupported jwt.");
        }
    }

    /**
     * 获取认证对象
     *
     * @param authObject 认证应用信息
     * @param id         token id
     * @return 认证对象
     */
    private Authentication loadClientAuthentication(String authObject, String id) {
        // 该对象传递三个参数
        // 第一个是认证对象权限完整信息 principal
        // 第二个是认证对象凭证标识 credentials
        // 第三个是持有权限的当事人权力标识符，一般用角色名称 authorities
        JwtAuthData jwtAuthData = JsonUtils.json2Object(authObject, JwtAuthData.class, objectMapper);
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(jwtAuthData.getGrantedAuthorityList())) {
            authorities.addAll(jwtAuthData.getGrantedAuthorityList());
        }
        return new UsernamePasswordAuthenticationToken(authObject, StringUtils.joinStr(id, CommonConstants.SEPARATOR, jwtAuthData.getId(), CommonConstants.SEPARATOR, jwtAuthData.getTenantId()), authorities);
    }

    /**
     * 获取认证对象
     *
     * @param authObject 认证用户信息
     * @param id         token id
     * @return 认证对象
     */
    private Authentication loadUserAuthentication(String authObject, String id) {
        // 该对象传递三个参数
        // 第一个是认证对象权限完整信息 principal
        // 第二个是认证对象凭证标识 credentials
        // 第三个是持有权限的当事人权力标识符，一般用角色名称 authorities
        JwtUser jwtUser = JsonUtils.json2Object(authObject, JwtUser.class, objectMapper);
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(jwtUser.getGrantedAuthorityList())) {
            authorities.addAll(jwtUser.getGrantedAuthorityList());
        }
        if (CollectionUtils.isNotEmpty(jwtUser.getRoles())) {
            authorities.addAll(jwtUser.getRoles());
        }
        return new UsernamePasswordAuthenticationToken(authObject, StringUtils.joinStr(id, CommonConstants.SEPARATOR, jwtUser.getId(), CommonConstants.SEPARATOR, jwtUser.getTenantId()), authorities);
    }

    /**
     * 创建认证授权token
     *
     * @param refreshTokenId         refresh token id
     * @param authObject             认证对象
     * @param tokenValidityInSeconds accessToken有效期，以秒为单位
     * @param appKeyAndSecret        组成格式：appKey:appSecret，为空则采用默认密钥生成签名key
     * @return token
     */
    private JwtToken createToken(String refreshTokenId, String authObject, Integer tokenValidityInSeconds, String appKeyAndSecret) {
        // token ID 每次采用一个唯一ID，以便后期通过该ID对相关token执行失效处理
        String id = StringUtils.getUuid();
        Date date = new Date();
        Date expiration = DateUtils.addSeconds(date, tokenValidityInSeconds != null ? tokenValidityInSeconds : jwtSecurityProperty.getTokenValidityInSeconds());
        String tk = Jwts.builder()
                // token ID
                .setId(id)
                // 签发时间
                .setIssuedAt(date)
                // 签发者
                .setIssuer(jwtSecurityProperty.getIssuer())
                // 访问token主题
                .setSubject(ACCESS_TOKEN_SUBJECT)
                .claim(AUTH_OBJECT_KEY, authObject)
                // 过期时间
                .setExpiration(expiration)
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(loadSigningKey(appKeyAndSecret), SignatureAlgorithm.HS512)
                .compact();
        recordToken(refreshTokenId, id, tk, tokenValidityInSeconds);
        return new JwtToken(id, tk, expiration.getTime(), false, "");
    }

    /**
     * 获取签名key
     *
     * @param appKeyAndSecret 组成格式：appKey:appSecret，为空则采用默认密钥生成签名key
     * @return 签名key
     */
    private Key loadSigningKey(String appKeyAndSecret) {
        Key signingKey = key;
        if (StringUtils.isNotBlank(appKeyAndSecret)) {
            byte[] keyBytes = Decoders.BASE64.decode(Base64.encode(appKeyAndSecret, Coder.DEFAULT_ENCODING));
            signingKey = Keys.hmacShaKeyFor(keyBytes);
        }
        return signingKey;
    }

    /**
     * 从token中提取出认证权限数据
     *
     * @param claims
     * @param subject
     * @return 以字符串模式返回完整的权限数据
     */
    public String getJwtAuthDataFromToken(Claims claims, String subject) {
        if (!subject.equals(claims.getSubject())) {
            throw new UnsupportedJwtException("Unsupported jwt subject.");
        }
        String id = claims.getId();
        if (StringUtils.isBlank(id)) {
            throw new UnsupportedJwtException("Unsupported jwt id.");
        }
        String issuer = claims.getIssuer();
        if (!org.apache.commons.lang3.StringUtils.equalsIgnoreCase(jwtSecurityProperty.getIssuer(), issuer)) {
            throw new UnsupportedJwtException("Unsupported jwt issuer.");
        }
        // 获取权限
        Object authority = claims.get(AUTH_OBJECT_KEY);
        if (authority == null) {
            throw new UnsupportedJwtException("Unsupported jwt.");
        }
        return authority.toString();
    }

    /**
     * 获取AccessToken权限数据
     *
     * @param token           jwt对应的加密串
     * @param appKeyAndSecret 组成格式：appKey:appSecret，为空则采用默认密钥生成签名key
     * @return AccessToken权限数据
     */
    public AccessTokenAuthData getJwtAuthDataFromAccessToken(String token, String appKeyAndSecret) {
        Jws<Claims> jwt = getJwtFromToken(token, appKeyAndSecret);
        Claims claims = jwt.getBody();
        String authObject = getJwtAuthDataFromToken(claims, ACCESS_TOKEN_SUBJECT);
        JwtAuthData jwtAuthData = JsonUtils.json2Object(authObject, JwtAuthData.class, objectMapper);
        String id = claims.getId();
        if (jwtAuthData.getIsStateful()) {
            String appKey = StringUtils.isBlank(appKeyAndSecret) ? DEFAULT_APP : appKeyAndSecret.substring(0, appKeyAndSecret.indexOf(CommonConstants.COLON));
            if (!validateToken(appKey, jwtAuthData, id)) {
                // 从token中解析权限数据已经验证了有效期，所以这里只有可能强制下线会导致token不存在
                throw new DisposedJwtException("The jwt has been disposed.");
            }
        }
        return new AccessTokenAuthData(id, authObject, jwtAuthData);
    }

    /**
     * 获取JWT，如果token过期，该方法会抛出ExpiredJwtException
     *
     * @param token           jwt对应的加密串
     * @param appKeyAndSecret 组成格式：appKey:appSecret，为空则采用默认密钥生成签名key
     * @return JWT
     */
    private Jws<Claims> getJwtFromToken(String token, String appKeyAndSecret) {
        try {
            return Jwts.parser()
                    .setSigningKey(loadSigningKey(appKeyAndSecret))
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            // token过期需要从redis里面移除
            Claims claims = e.getClaims();
            String id = claims.getId();
            switch (claims.getSubject()) {
                case REFRESH_TOKEN_SUBJECT:
                    // 一个应用可能为认证对象颁发多个refreshtoken，移除时要从池子中移除指定的token
                    String appKey = StringUtils.isBlank(appKeyAndSecret) ? DEFAULT_APP : appKeyAndSecret.substring(0, appKeyAndSecret.indexOf(CommonConstants.COLON));
                    String authObject = claims.get(AUTH_OBJECT_KEY).toString();
                    JwtAuthData jwtAuthData = JsonUtils.json2Object(authObject, JwtAuthData.class, objectMapper);
                    removeRefreshToken(appKey, jwtAuthData.getChannel(), jwtAuthData.getType(), jwtAuthData.getId(), id);
                    break;
                case ACCESS_TOKEN_SUBJECT:
                    // 删除过期的token
                    removeAccessToken(id);
                    break;
                default:
            }
            throw e;
        }
    }

    /**
     * token 是否未被强制失效
     *
     * @param appKey      应用键
     * @param jwtAuthData JWT认证权限基础数据，该数据由完整权限数据反序列化基础数据类获得，会丢失具体的权限明细
     * @param id          token ID
     * @return 是否未被强制失效
     */
    private boolean validateToken(String appKey, JwtAuthData jwtAuthData, String id) {
        String refreshTokenId = redisService.get(getAccessTokenIdToRefreshTokenIdKey(id));
        if (StringUtils.isBlank(refreshTokenId)) {
            return false;
        }
        return validateRefreshToken(appKey, jwtAuthData, refreshTokenId);
    }

    /**
     * 记录颁发过的Token
     *
     * @param refreshTokenId         refresh token ID
     * @param id                     access token ID
     * @param tk                     access token Value
     * @param tokenValidityInSeconds accessToken有效期，以秒为单位
     */
    private void recordToken(String refreshTokenId, String id, String tk, Integer tokenValidityInSeconds) {
        long expire = tokenValidityInSeconds != null ? tokenValidityInSeconds : jwtSecurityProperty.getTokenValidityInSeconds();
        redisService.set(getAccessTokenIdToRefreshTokenIdKey(id), refreshTokenId, expire, TimeUnit.SECONDS);
        redisService.set(getAccessTokenId2ValueKey(id), tk, expire, TimeUnit.SECONDS);
    }

    /**
     * refresh token 是否未被强制失效
     *
     * @param appKey      应用键
     * @param jwtAuthData JWT认证权限基础数据，该数据由完整权限数据反序列化基础数据类获得，会丢失具体的权限明细
     * @param id          refresh token ID
     * @return 是否未被强制失效
     */
    private boolean validateRefreshToken(String appKey, JwtAuthData jwtAuthData, String id) {
        String key = getAwardRefreshTokenIdsKey(appKey, jwtAuthData.getType(), jwtAuthData.getId());
        return redisSortedSetService.zscore(key, id) != null;
    }

    /**
     * 从认证标识中提取登录设备ID
     *
     * @param credentialLoginId 认证标识
     * @return 登录设备ID
     */
    private String loadDeviceIdByCredentialLoginId(String credentialLoginId) {
        return credentialLoginId.split(CommonConstants.DASHED)[2];
    }

    /**
     * 表示认证实体通过哪个设备哪个请求渠道登录哪个应用的认证标识
     * {type}-{id}-{deviceId}-{channel}-{appKey}
     *
     * @param appKey   应用键
     * @param channel  请求渠道，比如：PC，APP，WEB
     * @param type     认证对象类型
     * @param id       认证对象ID
     * @param deviceId 登录设备ID
     * @return {type}-{id}-{deviceId}-{channel}-{appKey}
     */
    private String getCredentialLoginId(String appKey, ChannelEnum channel, String type, Long id, String deviceId) {
        if (channel == null) {
            channel = ChannelEnum.UNSPECIFIED;
        }
        return StringUtils.joinStr(type, CommonConstants.DASHED, id, CommonConstants.DASHED, deviceId, CommonConstants.DASHED, channel.name(), CommonConstants.DASHED, appKey);
    }

    /**
     * 获取记录refresh token id与认证标识之间映射关系的键格式：
     * tk:refresh:id2credential:xx -> xx
     *
     * @param id refresh token id
     * @return refresh token id与认证标识之间映射关系的键
     */
    private String getRefreshTokenId2CredentialKey(String id) {
        return StringUtils.joinStr(REFRESH_TOKEN_ID2CREDENTIAL_KEY_MARK, id);
    }

    /**
     * 获取记录某一个认证对象通过某个渠道在某个应用登录获得的refresh token id的键格式：
     * tk:refresh:credentials:{type}-{id}-{channel}-{appKey} -> xx
     *
     * @param appKey  应用键
     * @param channel 请求渠道，比如：PC，APP，WEB
     * @param type    认证对象类型
     * @param id      认证对象ID
     * @return 认证对象某次登录与refreshToken之间映射关系的键
     */
    private String getAwardRefreshTokenIdKey(String appKey, ChannelEnum channel, String type, Long id) {
        if (channel == null) {
            channel = ChannelEnum.UNSPECIFIED;
        }
        return StringUtils.joinStr(REFRESH_TOKEN_CREDENTIAL_ID_KEY_MARK, type, CommonConstants.DASHED, id, CommonConstants.DASHED, channel.name(), CommonConstants.DASHED, appKey);
    }

    /**
     * 获取记录某一个认证对象在某个应用获得的所有refresh token id的键格式：
     * tk:refresh:credentials:{type}-{id}-{appKey} -> xx,xx,xx
     *
     * @param appKey 应用键
     * @param type   认证对象类型
     * @param id     认证对象标识
     * @return 认证对象与refreshToken之间映射关系的键
     */
    private String getAwardRefreshTokenIdsKey(String appKey, String type, Long id) {
        return StringUtils.joinStr(REFRESH_TOKEN_CREDENTIAL_IDS_KEY_MARK, type, CommonConstants.DASHED, id, CommonConstants.DASHED, appKey);
    }

    /**
     * 获取记录access token与refresh token之间映射关系的键格式：
     * tk:access:id2rid:xx -> xx
     *
     * @param id access token id
     * @return access token id与refresh token id之间映射关系的键
     */
    private String getAccessTokenIdToRefreshTokenIdKey(String id) {
        return StringUtils.joinStr(ACCESS_TOKEN_ID2RID_KEY_MARK, id);
    }

    /**
     * 获取记录access token id与access token value之间映射关系的键格式：
     * tk:access:id2value:xx -> xx
     *
     * @param id access token id
     * @return access token id与access token value之间映射关系的键
     */
    private String getAccessTokenId2ValueKey(String id) {
        return StringUtils.joinStr(ACCESS_TOKEN_ID2VALUE_KEY_MARK, id);
    }

    /**
     * 获取记录refresh token id与refresh token value之间映射关系的键格式：
     * tk:refresh:id2value:xx -> xx
     *
     * @param id refresh token id
     * @return refresh token id与refresh token value之间映射关系的键
     */
    private String getRefreshTokenId2ValueKey(String id) {
        return StringUtils.joinStr(REFRESH_TOKEN_ID2VALUE_KEY_MARK, id);
    }

    /**
     * 判断token是否为token id，如果属于token id，则转换为对应token value返回；
     * 否则原样返回token value
     *
     * @param accessToken token值
     * @return 真实的token value值
     */
    private String convert2AccessTokenValue(String accessToken) {
        if (accessToken.length() == ID_LENGTH) {
            // 将token id还原为原始token value
            String value = redisService.get(getAccessTokenId2ValueKey(accessToken));
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return accessToken;
    }

    /**
     * 判断token是否为token id，如果属于token id，则转换为对应token value返回；
     * 否则原样返回token value
     *
     * @param refreshToken token值
     * @return 真实的token value值
     */
    private String convert2RefreshTokenValue(String refreshToken) {
        if (refreshToken.length() == ID_LENGTH) {
            // 将token id还原为原始token value
            String value = redisService.get(getRefreshTokenId2ValueKey(refreshToken));
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return refreshToken;
    }

    /**
     * 删除refresh token相关联的资源
     *
     * @param appKey  应用键
     * @param channel 请求渠道，比如：PC，APP，WEB
     * @param type    认证对象类型
     * @param id      认证对象ID
     * @param tokenId refresh token id
     */
    private void removeRefreshToken(String appKey, ChannelEnum channel, String type, Long id, String tokenId) {
        redisService.del(getRefreshTokenId2CredentialKey(tokenId));
        String awardRefreshTokenIdKey = getAwardRefreshTokenIdKey(appKey, channel, type, id);
        if (tokenId.equalsIgnoreCase(redisService.get(awardRefreshTokenIdKey))) {
            redisService.del(awardRefreshTokenIdKey);
        }
        String awardRefreshTokenIdsKey = getAwardRefreshTokenIdsKey(appKey, type, id);
        redisSortedSetService.zrem(awardRefreshTokenIdsKey, tokenId);
        redisService.del(getRefreshTokenId2ValueKey(tokenId));
    }

    /**
     * 删除access token相关联的资源
     *
     * @param tokenId access token id
     */
    private void removeAccessToken(String tokenId) {
        redisService.del(getAccessTokenIdToRefreshTokenIdKey(tokenId));
        redisService.del(getAccessTokenId2ValueKey(tokenId));
    }

    /**
     * 强制认证对象在某个应用下线
     *
     * @param appKey 应用键
     * @param type   认证对象类型
     * @param id     认证对象标识
     */
    public void kickedOffline(String appKey, String type, Long id) {
        // 将对应应用给用户颁发的refreshToken从容器中移除，使登录失效
        String awardRefreshTokenIdsKey = getAwardRefreshTokenIdsKey(appKey, type, id);
        List<String> tokenIds = redisSortedSetService.zrange(awardRefreshTokenIdsKey, 0, -1, String.class);
        for (String tokenId : tokenIds) {
            // 删除每个渠道登录对应的refresh token id记录，防止影响下一次登录
            for (ChannelEnum channel : ChannelEnum.values()) {
                removeRefreshToken(appKey, channel, type, id, tokenId);
            }
        }
    }

    /**
     * 注销
     * 将本次登录颁发的refresh token与access token从redis中移除
     *
     * @param appKey        应用键
     * @param channel       请求渠道，比如：PC，APP，WEB
     * @param type          认证对象类型
     * @param id            认证对象标识
     * @param accessTokenId 本次颁发的access token id
     */
    public void logout(String appKey, ChannelEnum channel, String type, Long id, String accessTokenId) {
        String key = getAccessTokenIdToRefreshTokenIdKey(accessTokenId);
        String refreshTokenId = redisService.get(key);
        if (StringUtils.isNotBlank(refreshTokenId)) {
            removeRefreshToken(appKey, channel, type, id, refreshTokenId);
        }
        removeAccessToken(accessTokenId);
    }

}
