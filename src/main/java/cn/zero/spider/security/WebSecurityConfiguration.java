package cn.zero.spider.security;

import cn.zero.spider.security.handler.NovelAccessDeniedHandler;
import cn.zero.spider.security.handler.NovelAuthenticationEntryPoint;
import cn.zero.spider.security.handler.NovelAuthenticationFailureHandler;
import cn.zero.spider.security.handler.NovelAuthenticationSuccessHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .formLogin()
                .successHandler(novelAuthenticationSuccessHandler()) // 登录成功处理器
                .failureHandler(novelAuthenticationFailureHandler()) // 登录失败处理器
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(novelAuthenticationEntryPoint())
                .accessDeniedHandler(novelAccessDeniedHandler())
                .and()
                .authorizeRequests()
                .mvcMatchers("/synBookShelf").authenticated()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterBefore(new JWTAuthFilter(), UsernamePasswordAuthenticationFilter.class);

    }

    @Bean
    public NovelAuthenticationSuccessHandler novelAuthenticationSuccessHandler() {
        return new NovelAuthenticationSuccessHandler(objectMapper);
    }

    @Bean
    public NovelAuthenticationFailureHandler novelAuthenticationFailureHandler() {
        return new NovelAuthenticationFailureHandler(objectMapper);
    }

    @Bean
    public NovelAccessDeniedHandler novelAccessDeniedHandler() {
        return new NovelAccessDeniedHandler(objectMapper);
    }

    @Bean
    public NovelAuthenticationEntryPoint novelAuthenticationEntryPoint() {
        return new NovelAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
