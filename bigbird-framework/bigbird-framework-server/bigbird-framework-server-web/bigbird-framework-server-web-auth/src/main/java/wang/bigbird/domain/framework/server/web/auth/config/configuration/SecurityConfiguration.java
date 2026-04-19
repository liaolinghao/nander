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
package wang.bigbird.domain.framework.server.web.auth.config.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import wang.bigbird.domain.framework.server.web.auth.support.evaluator.JwtPermissionEvaluator;
import wang.bigbird.domain.framework.server.web.auth.support.filter.JwtAuthenticationTokenFilter;
import wang.bigbird.domain.framework.server.web.auth.support.handler.JwtAccessDeniedHandler;
import wang.bigbird.domain.framework.server.web.auth.support.handler.JwtAuthenticationEntryPointHandler;
import wang.bigbird.domain.framework.server.web.auth.support.processor.JwtSecurityProcessor;

import javax.annotation.PostConstruct;

/**
 * Spring Security配置
 *
 * @author Bigbird
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPointHandler jwtAuthenticationEntryPointHandler;
    private final JwtSecurityProcessor jwtSecurityProcessor;

    public SecurityConfiguration(JwtAccessDeniedHandler jwtAccessDeniedHandler, JwtAuthenticationEntryPointHandler jwtAuthenticationEntryPointHandler, JwtSecurityProcessor jwtSecurityProcessor) {
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.jwtAuthenticationEntryPointHandler = jwtAuthenticationEntryPointHandler;
        this.jwtSecurityProcessor = jwtSecurityProcessor;
    }

    @PostConstruct
    public void init() {
        log.info("Init security config.");
    }

    /**
     * 注册jwt token验证过滤器
     *
     * @param filter
     * @return
     */
    @Bean
    public FilterRegistrationBean registration(JwtAuthenticationTokenFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * 注入自定义PermissionEvaluator
     */
    @Bean
    public DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler() {
        DefaultWebSecurityExpressionHandler handler = new DefaultWebSecurityExpressionHandler();
        handler.setPermissionEvaluator(new JwtPermissionEvaluator());
        return handler;
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // 禁用 CSRF，支持不同IP过来的请求
                .csrf().disable()
                // 只允许加载来源于网站域名自身的资源，未来存在跨域加载需要对应修改这里
                .headers().contentSecurityPolicy("default-src 'self';" +
                        "style-src 'self' 'unsafe-inline';" +
                        "img-src 'self' data: ;" +
                        "script-src 'self' 'unsafe-eval' 'unsafe-inline'")
                // 禁用缓存
                .and().cacheControl()
                // 开启MIME嗅探，对于返回的静态资源的Content-Type设置不正确的场景，浏览器会自动尝试识别其Content-Type
                // 禁用后会存在一定的安全风险
                .and().contentTypeOptions().disable()
                // 启用XSS过滤，如果检测到攻击，浏览器不会清除页面，而是阻止页面加载
                .xssProtection().block(true)
                // 响应内容是否允许在iframe中展示有以下两种种情况：
                // DENY
                // 表示该页面不允许在 frame 中展示，即便是在相同域名的页面中嵌套也不允许。
                // httpSecurity.headers().frameOptions().deny();
                // SAMEORIGIN
                // 表示该页面可以在相同域名页面的 frame 中展示。
                // httpSecurity.headers().frameOptions().sameOrigin();
                // 禁用，表示完全允许 iframe 中展示
                // httpSecurity.headers().frameOptions().disable();
                .and().frameOptions().sameOrigin()
                // 不创建会话
                .and()
                .sessionManagement()
                // always – 如果session不存在总是需要创建；
                // ifRequired – 仅当需要时，创建session（默认配置）；
                // never – 框架从不创建session，但如果已经存在，会使用该session；
                // stateless – Spring Security不会创建session或使用session；
                // 搭配JWT使用时，设置为stateless使每次请求成为无状态模式
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                // "migrateSession"，即认证时创建一个新http session，原session失效，属性从原session中拷贝过来；
                // "none"，原session保持有效；
                // "changeSessionId"，会话管理策略，它不创建新的会话，而是每次登录访问后更换现有的会话标识符（Session ID），但保持原有的会话数据不变；
                // "newSession"，新创建session且不从原session中拷贝任何属性。
                .sessionFixation().none();
        if (jwtSecurityProcessor.isEnableJwtSecurity()) {
            httpSecurity
                    // 授权异常
                    .exceptionHandling()
                    .authenticationEntryPoint(jwtAuthenticationEntryPointHandler)
                    .accessDeniedHandler(jwtAccessDeniedHandler)
                    .and()
                    .authorizeRequests()
                    // 放行静态资源
                    .antMatchers(
                            HttpMethod.GET,
                            "/*.html",
                            "/**/*.html",
                            "/**/*.css",
                            "/**/*.js",
                            "/webSocket/**"
                    ).permitAll()
                    // 放行swagger
                    .antMatchers("/swagger-ui.html").permitAll()
                    .antMatchers("/swagger-resources/**").permitAll()
                    .antMatchers("/webjars/**").permitAll()
                    .antMatchers("/*/api-docs").permitAll()
                    // 放行druid
                    .antMatchers("/druid/**").permitAll()
                    // 放行OPTIONS请求
                    .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    //允许匿名及登录用户访问的接口
                    .antMatchers(jwtSecurityProcessor.getWithoutJwtSecurityApi()).permitAll()
                    // 所有请求都需要认证
                    .anyRequest().authenticated()
                    // 添加JWT filter
                    .and().apply(new TokenConfigurer(jwtSecurityProcessor));
        } else {
            httpSecurity.authorizeRequests().anyRequest().permitAll();
        }
    }

    public class TokenConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

        private final JwtSecurityProcessor jwtSecurityProcessor;

        public TokenConfigurer(JwtSecurityProcessor jwtSecurityProcessor) {
            this.jwtSecurityProcessor = jwtSecurityProcessor;
        }

        @Override
        public void configure(HttpSecurity http) {
            JwtAuthenticationTokenFilter customFilter = new JwtAuthenticationTokenFilter(jwtSecurityProcessor);
            http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
        }
    }

}
