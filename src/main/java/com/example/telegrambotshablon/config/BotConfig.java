package com.example.telegrambotshablon.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.yml")
@Data
public class BotConfig {
    @Value("${bot.name}")
    String username;
    @Value("${bot.token}")
    String token;

}
