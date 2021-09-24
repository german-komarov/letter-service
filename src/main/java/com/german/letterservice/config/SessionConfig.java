package com.german.letterservice.config;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
public class SessionConfig {

    @Bean
    public SessionRegistryImpl getSessionRegistryImplBean(){
        return new SessionRegistryImpl();
    }


    @Bean
    public HttpSessionEventPublisher getHttpSessionEventPublisherBean() {
        return new HttpSessionEventPublisher();
    }



    @Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> getServletListenerRegistrationBean() {
        return new ServletListenerRegistrationBean<>(this.getHttpSessionEventPublisherBean());
    }


}
