package com.example.telegrambotshablon.entity;

import com.example.telegrambotshablon.enums.UserStatus;
import com.example.telegrambotshablon.enums.UserStep;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "bot_users")
public class BotUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int botId;
    private LocalDateTime createDate = LocalDateTime.now();
    @NotNull
    private Long tgId;
    @Max(32)
    private String name;
    @Max(13)
    private String phoneNum;
    @Max(32)
    private String interest;
    private int refCount = 1;
    @Max(13)
    private String refUserPhone = "000";  //kim taklif qilgan
    @Max(9)
    private UserStep step= UserStep.valueOf("NAME");
    @Max(9)
    private UserStatus status = UserStatus.valueOf("NO_ACTIVE");
}
