package com.trecapps.admin.security;

import com.trecapps.auth.services.TrecAccountService;
import com.trecapps.auth.services.TrecSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Order(1)
public class SecurityConfig {

    @Autowired
    SecurityConfig(TrecAccountService trecAccountService1, TrecSecurityContext trecSecurityContext1)
    {
        //aadAuthProps.setRedirectUriTemplate("http://localhost:4200/api");
        trecAccountService = trecAccountService1;
        trecSecurityContext = trecSecurityContext1;
    }
    TrecAccountService trecAccountService;
    TrecSecurityContext trecSecurityContext;


    @Bean
    public SecurityFilterChain configure(HttpSecurity security) throws Exception
    {
        security.csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers("/Verify/admin/**", "/Access/**")
                .hasAnyAuthority("USER_ADMIN")
                .and()
                .authorizeHttpRequests()
                .anyRequest()
                .hasAuthority("EMAIL_VERIFIED")
                .and()
                .userDetailsService(trecAccountService)
                .securityContext().securityContextRepository(trecSecurityContext).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ;
        return security.build();
    }





}
