package com.prorenta.financeservice.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class UserFeignConfig {

    @Bean
    public Request.Options options() {
        return new Request.Options(
                Duration.ofSeconds(5),
                Duration.ofSeconds(10),
                true
        );
    }

    @Bean
    public Logger.Level level() {
        return Logger.Level.FULL;
    }
}
