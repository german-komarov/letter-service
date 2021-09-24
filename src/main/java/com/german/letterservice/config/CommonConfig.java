package com.german.letterservice.config;

import com.german.letterservice.services.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class CommonConfig {


    private final RoleService roleService;



    @Autowired
    public CommonConfig(RoleService roleService) {
        this.roleService = roleService;
    }


    @Bean
    public RestTemplate getRestTemplateBean(){
        return new RestTemplate();
    }


    @Bean
    public BCryptPasswordEncoder getBCryptPasswordEncoderBean(){
        return new BCryptPasswordEncoder();
    }




    @EventListener(ApplicationReadyEvent.class)
    public void afterApplicationIsReady() {

        log.info("Filling database with roles (if required) started");

        this.roleService.fillDatabaseWithRoles("ROLE_USER","ROLE_ADMIN");

        log.info("Filling database with roles (if required) finished");
    }




}
