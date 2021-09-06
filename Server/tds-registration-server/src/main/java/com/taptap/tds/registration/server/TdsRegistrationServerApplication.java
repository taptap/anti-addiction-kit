package com.taptap.tds.registration.server;

import com.taptap.tds.registration.server.configuration.PublicityProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@Log4j2
@RestController
@EnableConfigurationProperties(PublicityProperties.class)
@SpringBootApplication
public class TdsRegistrationServerApplication {
    public static void main(String[] args) {
        String envVars[] = {"LEANCLOUD_APP_PORT", "REDIS_URL_antiRedis", "MYSQL_PORT_antiMysql", "MYSQL_HOST_antiMysql",
                "MYSQL_ADMIN_USER_antiMysql", "MYSQL_ADMIN_PASSWORD_antiMysql"};
        for (String envVar : envVars) {
            System.out.println(envVar + "=" + System.getenv(envVar));
        }
        String port = System.getenv("LEANCLOUD_APP_PORT");
        SpringApplication app = new SpringApplication(TdsRegistrationServerApplication.class);
        if (null != port && port.length() > 0) {
            app.setDefaultProperties(Collections.singletonMap("server.port", port));
        }
        app.run(args);
    }
}