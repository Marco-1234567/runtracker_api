package com.marco_laasonen.runtracker_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * RestTemplate is used by StravaService to make HTTP calls
     * to the Strava API. Declared as a bean so Spring can inject it.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}