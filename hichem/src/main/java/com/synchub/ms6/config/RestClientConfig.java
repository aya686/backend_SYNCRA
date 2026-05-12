package com.synchub.ms6.config;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration(exclude = {
        org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration.class
})
public class RestClientConfig {
}