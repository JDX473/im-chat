package com.im.chat.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.im.chat")
@EntityScan("com.im.chat.infrastructure.persistence.po")
@EnableJpaRepositories("com.im.chat.infrastructure.persistence.repository")
public class ImChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImChatApplication.class, args);
    }
}
