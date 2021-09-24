package com.german.letterservice.config;

import com.german.letterservice.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {



    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionRegistryImpl sessionRegistryImpl;



    @Value("${letter-service.security.remember-me.key}")
    private String rememberMeKey;


    private final HttpMethod GET=HttpMethod.GET;
    private final HttpMethod POST=HttpMethod.POST;
    private final HttpMethod PUT=HttpMethod.PUT;
    private final HttpMethod DELETE=HttpMethod.DELETE;



    @Autowired
    public SecurityConfig(UserDetailsServiceImpl userDetailsServiceImpl, BCryptPasswordEncoder passwordEncoder, SessionRegistryImpl sessionRegistryImpl) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.passwordEncoder = passwordEncoder;
        this.sessionRegistryImpl = sessionRegistryImpl;
    }


    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(this.userDetailsServiceImpl).passwordEncoder(this.passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .authorizeRequests()
                    .antMatchers(GET,"/webjars/**","/js/**","/css/**").permitAll()
                    .antMatchers("/","/users/registration","/letters/external","/users/exists").permitAll()
                    .antMatchers("/actuator/**").hasAnyRole("ADMIN")
                    .anyRequest().authenticated()
                        .and()
                .formLogin()
                    .defaultSuccessUrl("/main")
                        .and()
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout","POST"))
                    .logoutSuccessUrl("/login")
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("SESSION","JSESSIONID","remember-me")
                        .and()
                .rememberMe()
                    .key(this.rememberMeKey)
                    .tokenValiditySeconds(-1)
                        .and()
                .csrf()
                    .disable()
                .sessionManagement()
                    .maximumSessions(-1)
                    .sessionRegistry(this.sessionRegistryImpl)
        ;


    }














}
