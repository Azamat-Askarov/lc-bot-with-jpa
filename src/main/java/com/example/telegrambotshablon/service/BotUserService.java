package com.example.telegrambotshablon.service;

import com.example.telegrambotshablon.dto.BotUser;
import com.example.telegrambotshablon.entity.BotUserEntity;
import com.example.telegrambotshablon.repository.BotUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BotUserService {
    private final BotUserRepository botUserRepository;

    public BotUserService(BotUserRepository botUserRepository) {
        this.botUserRepository = botUserRepository;
    }

    public synchronized void addUser(BotUser botUser) {
        BotUserEntity botUserEntity = new BotUserEntity();
        botUserEntity.setTgId(botUser.getTg_id());
        botUserEntity.setRefUserPhone(botUser.getRef_user_phone());
        botUserRepository.save(botUserEntity);
    }

    public synchronized void updateUser(BotUser botUser) {
        BotUserEntity botUserEntity = botUserRepository.getBotUserEntityByTgId(botUser.getTg_id());
        if (botUser.getName() != null) {
            botUserEntity.setName(botUser.getName());
        }
        if (botUser.getPhone_num() != null) {
            botUserEntity.setPhoneNum(botUser.getPhone_num());
        }
        if (botUser.getInterest() != null) {
            botUserEntity.setInterest(botUser.getInterest());
        }
        if (botUser.getRef_count() != 0) {
            botUserEntity.setRefCount(botUser.getRef_count());
        }
        if (botUser.getRef_user_phone() != null) {
            botUserEntity.setRefUserPhone(botUser.getRef_user_phone());
        }
        if (botUser.getStep() != null) {
            botUserEntity.setStep(botUser.getStep());
        }
        if (botUser.getStatus() != null) {
            botUserEntity.setStatus(botUser.getStatus());
        }
        botUserRepository.save(botUserEntity);
    }

    public synchronized List<Long> getAllTgId() {
        return botUserRepository.existsAllByTgId();
    }

    public synchronized BotUser getUserById(Long id) {
        try {
            BotUserEntity botUserEntity = botUserRepository.getBotUserEntityByTgId(id);
            return BotUser.builder()
                    .createDate(botUserEntity.getCreateDate())
                    .tg_id(botUserEntity.getTgId())
                    .bot_id(botUserEntity.getBotId())
                    .name(botUserEntity.getName())
                    .interest(botUserEntity.getInterest())
                    .phone_num(botUserEntity.getPhoneNum())
                    .ref_count(botUserEntity.getRefCount())
                    .ref_user_phone(botUserEntity.getRefUserPhone())
                    .status(botUserEntity.getStatus())
                    .step(botUserEntity.getStep())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized BotUser getUserByPhone(String phone_num) {
        try {
            BotUserEntity botUserEntity = botUserRepository.getBotUserEntityByPhoneNum(phone_num);
            return BotUser.builder()
                    .createDate(botUserEntity.getCreateDate())
                    .tg_id(botUserEntity.getTgId())
                    .bot_id(botUserEntity.getBotId())
                    .name(botUserEntity.getName())
                    .interest(botUserEntity.getInterest())
                    .phone_num(botUserEntity.getPhoneNum())
                    .ref_count(botUserEntity.getRefCount())
                    .ref_user_phone(botUserEntity.getRefUserPhone())
                    .status(botUserEntity.getStatus())
                    .step(botUserEntity.getStep())
                    .build();
        } catch (Exception e) {
            return null;
        }

    }
}
