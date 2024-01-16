package com.example.telegrambotshablon.repository;

import com.example.telegrambotshablon.entity.BotUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotUserRepository extends JpaRepository<BotUserEntity, Long> {
    @Query(value = "select tg_id from bot_users", nativeQuery = true)
    List<Long> existsAllByTgId(); /**  HQL code  */

    BotUserEntity getBotUserEntityByTgId(Long tgId);

    BotUserEntity getBotUserEntityByPhoneNum(String phone_num);
}
