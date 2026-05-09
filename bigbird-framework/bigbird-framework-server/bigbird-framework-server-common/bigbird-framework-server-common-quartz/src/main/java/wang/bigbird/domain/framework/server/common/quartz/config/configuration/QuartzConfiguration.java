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
package wang.bigbird.domain.framework.server.common.quartz.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import wang.bigbird.domain.framework.server.common.quartz.support.factory.AutowiringSpringBeanJobFactory;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * WEB框架配置
 *
 * @author Bigbird
 */
@Slf4j
@ComponentScan("wang.bigbird.domain.framework.server.common.quartz")
@Configuration
public class QuartzConfiguration {

    @PostConstruct
    public void init() {
        log.info("init quartz framework.");
    }

    @Bean
    public SchedulerFactoryBean schedulerFactory(ApplicationContext applicationContext, DataSource dataSource) throws SchedulerException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        // 设置Quartz配置文件位置，从类路径下加载quartz.properties
        factory.setConfigLocation(new ClassPathResource("quartz.properties"));
        // 关键：强制使用 Spring 的数据源，彻底抛弃 quartz.properties 里的数据库配置
        factory.setDataSource(dataSource);
        // 配置Spring管理Job的工厂（如需依赖注入）
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        factory.setJobFactory(jobFactory);
        return factory;
    }

    @Bean
    public Scheduler scheduler(ApplicationContext applicationContext, DataSource dataSource) throws SchedulerException {
        return schedulerFactory(applicationContext, dataSource).getScheduler();
    }

}
