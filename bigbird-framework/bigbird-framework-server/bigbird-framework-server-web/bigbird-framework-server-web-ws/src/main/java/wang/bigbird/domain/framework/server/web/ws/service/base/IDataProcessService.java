package wang.bigbird.domain.framework.server.web.ws.service.base;

/**
 * 数据加工服务
 *
 * @author Bigbird
 */
public interface IDataProcessService {

    /**
     * 加工数据
     *
     * @param appKey 应用键
     * @param token  认证token
     * @param data   原始消息数据
     * @return 加工后消息数据
     */
    String processData(String appKey, String token, String data);

}
