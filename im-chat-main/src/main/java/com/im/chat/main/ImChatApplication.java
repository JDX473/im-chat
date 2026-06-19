package com.im.chat.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.im.chat")
@EntityScan("com.im.chat.infra.persistence.po")
@EnableJpaRepositories("com.im.chat.infra.persistence.repository")
public class ImChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImChatApplication.class, args);
    }
}
