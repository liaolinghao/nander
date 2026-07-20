package wang.bigbird.domain.framework.server.core.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 服务配置
 *
 * @author Bigbird
 */
@Slf4j
@Configuration
@ComponentScan("wang.bigbird.domain.framework.server")
public class CoreConfiguration {

    @PostConstruct
    public void init() {
        log.info("init server framework.");
    }

}
