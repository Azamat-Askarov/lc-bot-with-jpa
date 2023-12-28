package com.example.telegrambotshablon.dto;


import com.example.telegrambotshablon.enums.UserStatus;
import com.example.telegrambotshablon.enums.UserStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BotUser {
    private LocalDateTime createDate;
    private int bot_id;
    private Long tg_id;
    private String name;
    private String phone_num;
    private String interest;
    private int ref_count;
    private String ref_user_phone;  //kim taklif qilgan
    private UserStep step;
    private UserStatus status;
}
