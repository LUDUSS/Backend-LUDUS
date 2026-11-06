package com.ludus.api;

import com.ludus.api.security.JwtFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LudusApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LudusApiApplication.class, args);
    }

    @Bean
    public FilterRegistrationBean<JwtFilter> jwtFilterFilter(){
        FilterRegistrationBean<JwtFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtFilter());

        // Aplicamos el filtro a TODAS las rutas de Ludus
        registrationBean.addUrlPatterns("/ludus/*");

        return registrationBean;
    }
}