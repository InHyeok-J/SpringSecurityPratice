package com.security.jwt.config;

import com.security.jwt.config.security.JwtAccessDeniedHandler;
import com.security.jwt.config.security.JwtAuthenticationEntryPoint;
import com.security.jwt.config.security.JwtSecurityConfig;
import com.security.jwt.config.security.filter.FormLoginCustomFilter;
import com.security.jwt.config.security.handler.FormLoginFailureHandler;
import com.security.jwt.config.security.handler.FormLoginSuccessHandler;
import com.security.jwt.config.security.provider.FormLoginProvider;
import com.security.jwt.util.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final FormLoginSuccessHandler formLoginSuccessHandler;
  private final FormLoginFailureHandler formLoginFailureHandler;
  private final FormLoginProvider provider;
  private final JwtProvider jwtProvider;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  public AuthenticationManager getAuthenticationManger() throws Exception {
    return super.authenticationManagerBean();
  }

  protected FormLoginCustomFilter formLoginCustomFilter() throws Exception {
    FormLoginCustomFilter filter = new FormLoginCustomFilter(new AntPathRequestMatcher("/api/login",
        HttpMethod.POST.name()),
        formLoginSuccessHandler,
        formLoginFailureHandler
    );
    filter.setAuthenticationManager(super.authenticationManagerBean());
    return filter;
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth
        .authenticationProvider(this.provider);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .csrf().disable()
        //exception Handler
        .exceptionHandling()
        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .accessDeniedHandler(jwtAccessDeniedHandler)
        //Jwt 방식을 사용할거라서 Session OFF
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        .and()
        .authorizeRequests()
        .antMatchers("/api/user").permitAll() // root 조회랑 회원가입운 열어둠
        .antMatchers("/api/login").permitAll()
        .anyRequest().authenticated()

        .and()
        .apply(new JwtSecurityConfig(jwtProvider));
    //Form Login 필터 등록
    http.addFilterBefore(formLoginCustomFilter(), UsernamePasswordAuthenticationFilter.class);
  }

}
