package com.example.telegrambotshablon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.telegrambotshablon.repository")
public class TelegramBotShablonApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotShablonApplication.class, args);
    }

}
