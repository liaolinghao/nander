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
package wang.bigbird.domain.framework.data.mybatisplus.dynamic.config.configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.github.yulichang.injector.MPJSqlInjector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.transaction.TransactionManagerCustomizers;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import wang.bigbird.domain.framework.data.mybatisplus.dynamic.support.injector.MySqlInjector;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * MybatisPlus 配置
 *
 * @author Bigbird
 */
@EnableTransactionManagement
@Configuration
@Slf4j
@ComponentScan(basePackages = "wang.bigbird.domain.framework.data.mybatisplus.dynamic", excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {MPJSqlInjector.class})})
public class MybatisPlusConfiguration {

    private final DataSource dataSource;

    private final TransactionManagerCustomizers transactionManagerCustomizers;

    /**
     * 构造函数隐式注入，@Configuration类历史上不支持构造函数注入。
     * 但从4.3开始，它们允许在单构造函数场景中省略@Autowired进行注入，这时
     * 会从beanFactory中查找相关实例，但为了解决隐式注入的强依赖问题
     * （如果bean不存在，会发生异常），采用ObjectProvider改进
     * <p>
     * Spring Framework 4.3引入了ObjectProvider，它是现有ObjectFactory接口的扩展，
     * 具有方便的签名，例如getIfAvailable和getIfUnique，
     * 只有在它实际存在时才检索bean（可选支持）或者如果可以确定单个候选者（特别是：主要候选者，
     * 在多个匹配的bean的情况下）
     *
     * @param dataSource
     * @param transactionManagerCustomizers
     */
    public MybatisPlusConfiguration(DataSource dataSource,
                                    ObjectProvider<TransactionManagerCustomizers> transactionManagerCustomizers) {
        this.dataSource = dataSource;
        this.transactionManagerCustomizers = transactionManagerCustomizers.getIfAvailable();
    }

    @PostConstruct
    public void init() {
        log.info("Init dynamic mybatis plus framework.");
    }

    /**
     * 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    /**
     * 自定义SQL注入器，该注入器添加的特征会提升sql操作效率
     */
    @Bean
    @Primary
    public MySqlInjector sqlInjector() {
        return new MySqlInjector();
    }

    /**
     * 事务管理器
     */
    @Bean
    @Primary
    public DataSourceTransactionManager myTransactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(this.dataSource);
        if (this.transactionManagerCustomizers != null) {
            this.transactionManagerCustomizers.customize(transactionManager);
        }
        return transactionManager;
    }
}
