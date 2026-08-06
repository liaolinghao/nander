package wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.fallback;

import com.github.lianjiatech.retrofit.spring.boot.degrade.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Response;
import wang.bigbird.domain.framework.message.sms.chinatelecom.domain.dto.out.YunyixinSendSmsResponseDTO;
import wang.bigbird.domain.framework.message.sms.chinatelecom.retrofit.YunyixinHttpClient;

import java.util.Map;

/**
 * 云翼信平台服务降级工厂
 *
 * @author Bigbird
 */
@Component
@Slf4j
public class YunyixinHttpDegradeFallbackFactory implements FallbackFactory<YunyixinHttpClient> {

    @Override
    public YunyixinHttpClient create(Throwable cause) {

        log.error("Fallback exception：{0} {1}", cause.getMessage(), cause);

        return new YunyixinHttpClient() {

            @Override
            public Response<YunyixinSendSmsResponseDTO> doSendMessage(Map<String, String> map) {
                return null;
            }

        };
    }

}
