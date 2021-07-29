package com.taptap.tds.registration.server;

import com.taptap.tds.registration.server.configuration.PublicityProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@EnableConfigurationProperties(PublicityProperties.class)
@SpringBootApplication
public class TdsRegistrationServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TdsRegistrationServerApplication.class, args);
    }
}