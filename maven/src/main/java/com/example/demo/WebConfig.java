package com.example.demo;

import com.example.demo.util.SecurityJsonHttpMessageConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebMvc
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("webconfig");
        converters.add(securityHttpMessageConverter());
//        WebMvcConfigurer.super.configureMessageConverters(converters);
    }

    @Bean
    public HttpMessageConverter<?> securityHttpMessageConverter() {

        return new SecurityJsonHttpMessageConverter();

    }

}
