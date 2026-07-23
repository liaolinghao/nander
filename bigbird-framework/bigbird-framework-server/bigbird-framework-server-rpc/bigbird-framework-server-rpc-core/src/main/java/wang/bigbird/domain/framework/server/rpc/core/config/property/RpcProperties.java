package wang.bigbird.domain.framework.server.rpc.core.config.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RPC框架通用配置
 *
 * @author Bigbird
 */
@Data
@Component
@ConfigurationProperties(prefix = "bigbird.server.rpc.core")
public class RpcProperties {

    /**
     * kryo注册类所在的包列表
     */
    private List<String> scanKryoSerializablePackages;

}
