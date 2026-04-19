package wang.bigbird.domain.framework.server.web.ws.service.base;

import wang.bigbird.domain.framework.server.web.ws.support.client.TargetWsClient;

/**
 * 目标WS连接赋权服务
 *
 * @author Bigbird
 */
public interface ITargetWsAuthService {

    /**
     * 为目标 WS 客户端添加鉴权请求头
     *
     * @param client 目标 WS 客户端
     */
    void addAuthHeaders(TargetWsClient client);

}
