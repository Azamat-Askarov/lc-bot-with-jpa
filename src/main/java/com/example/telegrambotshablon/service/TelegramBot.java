package com.example.telegrambotshablon.service;

import com.example.telegrambotshablon.config.BotConfig;
import com.example.telegrambotshablon.dto.BotUser;
import com.example.telegrambotshablon.enums.UserStatus;
import com.example.telegrambotshablon.enums.UserStep;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    static Map<UserStep, String> questionMap = new HashMap<>();
    final BotConfig botConfig;
    private final BotUserService botUserService;

    public TelegramBot(BotConfig botConfig, BotUserService botUserService) {
        this.botConfig = botConfig;
        this.botUserService = botUserService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                User user = message.getFrom();
                BotUser currentUser = botUserService.getUserById(user.getId());
                if (currentUser == null) {
                    /** yangi user /start bosdi */
                    createUser(update); /** yangi user create qilish */
                    currentUser = botUserService.getUserById(user.getId());
                    setQuestionsMap();
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(currentUser.getTg_id());
                    sendMessage.setText(questionMap.get(UserStep.NAME));
                    sendMsg(sendMessage);/** NAME kiritish ni so'rash */
                } else {
                    /** eski user amal bajarayotganida... */
                    if (message.hasText() && message.getText().equals("/help")) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(user.getId());
                        sendMessage.setText("/help - qo'llanma\n" +
                                "/foydalanuvchi - foydalanuvchi parametrlari\n"
                                + "/elon_reklama - bot foydalanuvchiariga e'lon yoki reklama yuborish\n" +
                                "/shikoyat_taklif - adminga xabar yuborish");
                        sendMsg(sendMessage);
                    } else if (currentUser.getStep().equals(UserStep.END) && message.hasText() && message.getText().equals("/foydalanuvchi")) {
                        if (message.getFrom().getId().equals(5601022853L) || message.getChatId().equals(570695243L) || message.getChatId().equals(5952923848L)) {
                            BotUser adminUser = botUserService.getUserById(user.getId()); /** ADMIN STEPINI O'ZGARTIRISH */
                            adminUser.setStep(UserStep.ADMIN_GET_USER);
                            botUserService.updateUser(adminUser);
                            /** Admin foydalanuvchini get  qilish jarayoni */
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(currentUser.getTg_id());
                            sendMessage.setText("\uD83D\uDC69\uD83C\uDFFB\u200D\uD83D\uDCBBHurmatli admin, foydalanuvchining " +
                                    "telefon raqamini 998776665544 formatida kiriting\uD83D\uDC4C");
                            sendMsg(sendMessage);
                        } else {
                            BotUser botUser = botUserService.getUserById(user.getId());
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(currentUser.getTg_id());
                            sendMessage.setText("O'zingiz haqida ma'lumotlar.\n\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uD83D\uDE4B\uD83C\uDFFB\u200D♀  Ism: "
                                    + botUser.getName() + "\n\uD83D\uDCF1 Tel: " + botUser.getPhone_num() + "\n\uD83D\uDCCB Ro'yxatdan o'tgan vaqt: "
                                    + botUser.getCreateDate() + "\n\uD83D\uDCDA Qiziqish: " + botUser.getInterest() + "\n\uD83D\uDCE9 Kim taklif qilgan: "
                                    + botUser.getRef_user_phone() + "\n\uD83D\uDCB0 Bonuslar: " + botUser.getRef_count() * 1000 + " so'm");
                            sendMsg(sendMessage);
                        }
                    } else if (message.hasText() && currentUser.getStep().equals(UserStep.ADMIN_GET_USER)) {
                        BotUser botUser = botUserService.getUserByPhone(message.getText());
                        if (botUser != null) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(currentUser.getTg_id());
                            sendMessage.setText("\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uD83D\uDE4B\uD83C\uDFFB\u200D♀ Ism: " +
                                    botUser.getName() + "\n\uD83D\uDCF1 Tel: " + botUser.getPhone_num() +
                                    "\n\uD83D\uDCCB Ro'yxatdan o'tgan: " + botUser.getCreateDate() + "\n\uD83D\uDCDA Qiziqishi: "
                                    + botUser.getInterest() + "\n\uD83D\uDCE9 Kim taklif qilgan: " + botUser.getRef_user_phone() +
                                    "\n\uD83D\uDCB0 Bonuslari: " + botUser.getRef_count() * 1000 + " so'm");
                            sendMsg(sendMessage);
                        } else {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(currentUser.getTg_id());
                            sendMessage.setText("\uD83E\uDD37\uD83C\uDFFB\u200D♂Foydalanuvchi topilmadi.");
                            sendMsg(sendMessage);
                        }
                        currentUser.setStep(UserStep.END);
                        botUserService.updateUser(currentUser);
                    } else if (currentUser.getStep().equals(UserStep.END) && message.hasText() && message.getText().equals("/shikoyat_taklif")) {
                        BotUser botUser = botUserService.getUserById(currentUser.getTg_id());
                        botUser.setStep(UserStep.SHIKOYAT_TAKLIF);
                        botUserService.updateUser(botUser);
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(currentUser.getTg_id());
                        sendMessage.setText("✍ Hurmatli foydalanuvchi o'quv markazimiz ma'muriyatiga biror taklif " +
                                "yoki shikoyatingiz mavjud bo'lsa uni to'liq va aniq qilib bayon qiling va bizga yuboring." +
                                "Biz muammoni iloji boricha tezda hal qilishga harakat qilamiz yoki sizga aloqaga chiqamiz.");
                        sendMsg(sendMessage);
                    } else if (message.hasText() && currentUser.getStep().equals(UserStep.SHIKOYAT_TAKLIF)) {
                        BotUser botUser = botUserService.getUserById(currentUser.getTg_id());
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(5601022853L);
                        sendMessage.setText("\uD83D\uDCE9 Foydalanuvchidan xabar keldi\uD83D\uDC40\n" +
                                "\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uD83D\uDE4B\uD83C\uDFFB Ism: " + botUser.getName() +
                                "\n\uD83D\uDCF1 Tel: " + botUser.getPhone_num() + "\n\n\uD83D\uDC49\uD83C\uDFFB " + message.getText());
                        sendMsg(sendMessage);

                        /** AzamatGA SHIKOYAT/TAKLIF NI YUBORISH */
                        SendMessage azamatgaXabar = new SendMessage();
                        azamatgaXabar.setChatId(5952923848L);
                        azamatgaXabar.setText("\uD83D\uDCE9 Foydalanuvchidan xabar keldi\uD83D\uDC40\n" +
                                "\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uD83D\uDE4B\uD83C\uDFFB Ism: " + botUser.getName() +
                                "\n\uD83D\uDCF1 Tel: " + botUser.getPhone_num() + "\n\n\uD83D\uDC49\uD83C\uDFFB " + message.getText());
                        sendMsg(azamatgaXabar);
                        /** ---------------------------------- */

                        botUser.setStep(UserStep.END);
                        botUserService.updateUser(botUser);
                        SendMessage sendMessage1 = new SendMessage();
                        sendMessage1.setChatId(currentUser.getTg_id());
                        sendMessage1.setText("Xabar yuborildi. Adminlarimiz tezda xabaringizni ko'rib chiqishadi.");
                        sendMsg(sendMessage1);
                    } else if (currentUser.getStep().equals(UserStep.END) && message.hasText() && message.getText().equals("/elon_reklama")) {
                        if (message.getChatId().equals(5601022853L)) {
                            BotUser botAdmin = botUserService.getUserById(currentUser.getTg_id());
                            botAdmin.setStep(UserStep.REKLAMA);
                            botUserService.updateUser(botAdmin);
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(currentUser.getTg_id());

                            sendMessage.setText("♻\uFE0F Hurmatli admin reklamangiz yoki e'loningizni xabar ko'rinishida menga "
                                    + "yuboring va men uni botdagi barcha foydalanuvchilarga yuboraman.");
                            sendMsg(sendMessage);
                        } else {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(currentUser.getTg_id());
                            sendMessage.setText("\uD83E\uDD79 Siz admin emassiz. E'lon yoki reklama joylay olmaysiz.\n"
                                    + "Lekin reklama bermoqchi bo'lsangiz adminlarimiz bilan bog'lanishingiz mumkin\uD83D\uDE09\n\uD83D\uDC49\uD83C\uDFFB /shikoyat_taklif");
                            sendMsg(sendMessage);
                        }
                    } else if (currentUser.getStep().equals(UserStep.REKLAMA)) {
                        SendMessage reklama = new SendMessage();
                        message.getMessageId();
                        reklama.setMessageThreadId(message.getMessageThreadId());
                        BotUser botUser = botUserService.getUserById(currentUser.getTg_id());
                        botUser.setStep(UserStep.REKLAMA_SENDING);
                        botUserService.updateUser(botUser);
                        createAdvertisingButton(currentUser);
                    } else if (currentUser.getStep().equals(UserStep.REKLAMA_SENDING)) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(currentUser.getTg_id());
                        if (message.getText().equals("✅ Tasdiqlash")) {
                            sendAdvertising(message, currentUser);
                            BotUser botUser = botUserService.getUserById(currentUser.getTg_id());
                            botUser.setStep(UserStep.END);
                            botUserService.updateUser(botUser);
                            sendMessage.setText("✅ E'lon/Reklama barcha foydalanuvchilarga yuborildi");
                        } else if (message.getText().equals("❌ Bekor qilish")) {
                            BotUser botUser = botUserService.getUserById(currentUser.getTg_id());
                            botUser.setStep(UserStep.END);
                            botUserService.updateUser(botUser);
                            sendMessage.setText("❌ E'lon/Reklama yuborish bekor qilindi");
                        }
                        ReplyKeyboardRemove removeButton = new ReplyKeyboardRemove(); /** keyboard button yashirish */
                        removeButton.setSelective(true);
                        removeButton.setRemoveKeyboard(true);
                        sendMessage.setReplyMarkup(removeButton);
                        sendMsg(sendMessage); /** button yashirildi va totalMessage yuborildi */
                    } else if (message.hasText() && message.getText().equals("/start")) {
                        /** ixtiyoriy holatda user /start yuborganida bosganda... */
                        if (currentUser.getStep().equals(UserStep.NAME)) {
                            setQuestionsMap();
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(currentUser.getTg_id());
                            sendMessage.setText(questionMap.get(UserStep.NAME));
                            sendMsg(sendMessage);
                        } else if (currentUser.getStep().equals(UserStep.PHONE_NUM)) {
                            getContactFromUser(currentUser);
                        } else if (currentUser.getStep().equals(UserStep.INTEREST)) {
                            getInterestFromUser(currentUser);
                        } else if (message.hasText() && currentUser.getStep().equals(UserStep.END)) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(currentUser.getTg_id());
                            sendMessage.setText("\uD83D\uDD17Sizning referral linkingiz:\nhttps://t.me/" + getBotUsername() +
                                    "?start=" + currentUser.getTg_id() + "\n\uD83D\uDE80Ushbu linkni do'stlaringizga uzating." +
                                    "\n\n\uD83D\uDCB0Link orqali ro'yxatdan o'tgan har bir do'stingiz uchun sizga 1.000 so'm chegirma qo'shiladi."
                                    + "\n\uD83D\uDCB0Sizda hozir " + currentUser.getRef_count() * 1000 + " so'm bonus mavjud.");
                            sendMsg(sendMessage);

                        }
                    } else if (currentUser.getStep().equals(UserStep.PHONE_NUM) && message.hasText() && !message.hasContact()) {
                        /** phone kiritish kk, lekin text yuborilsa... */
                        getContactFromUser(currentUser);
                    } else if (message.hasText() && currentUser.getStep().equals(UserStep.NAME)) {
                        /** NAME ni set qilish */
                        BotUser botUser = botUserService.getUserById(currentUser.getTg_id());
                        botUser.setName(message.getText());
                        botUser.setStep(UserStep.PHONE_NUM);
                        botUserService.updateUser(botUser);
                        /** telefon raqam kiritish jarayoni */
                        getContactFromUser(currentUser);
                    } else if (message.hasContact() && currentUser.getStep().equals(UserStep.PHONE_NUM)) {
                        /** PHONE_NUM set qilish */
                        BotUser botUser = botUserService.getUserById(currentUser.getTg_id());
                        if (checkValidityContact(message.getContact(), botUser)) {
                            botUser.setStep(UserStep.INTEREST);
                            botUserService.updateUser(botUser);
                        } else {
                            getContactFromUser(currentUser);
                        }
                        /** interest(qiziqish)ni kiritish jarayoni*/
                        getInterestFromUser(currentUser);
                    } else if (message.hasText() && currentUser.getStep().equals(UserStep.INTEREST)) {
                        if (!message.getText().equals("Ingliz tili") && !message.getText().equals("Rus tili") && !message.getText().equals("Kompyuter savodxonligi")
                                && !message.getText().equals("Web dasturlash") && !message.getText().equals("IELTS") && !message.getText().equals("Multilevel (CEFR)")) {
                            /** interest kiritish kk lekin boshqa text kiritilsa... */
                            getInterestFromUser(currentUser);
                        } else {
                            /** interest(qiziqish)ni set qilish */
                            BotUser botUser = botUserService.getUserById(currentUser.getTg_id());
                            botUser.setInterest(message.getText());
                            botUser.setStep(UserStep.END);
                            botUser.setStatus(UserStatus.ACTIVE);
                            botUserService.updateUser(botUser);

                            /** yangi user haqida xabar qilish */
                            SendMessage admin1gaXabar = new SendMessage();
                            admin1gaXabar.setChatId(5601022853L);
                            admin1gaXabar.setText("➕Yangi foydalanuvchi.\n\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uFE0F\uD83D\uDE4B\uD83C\uDFFB\u200D♀\uFE0F Ismi : " +
                                    botUser.getName() + "\n\uD83D\uDCF1 Tel : " + botUser.getPhone_num()+"\n\uD83D\uDCDA "+botUser.getInterest());
                            sendMsg(admin1gaXabar); /** yangi user haqida admin1 ga xabar yuborish */
                            /**----------------------------------------------- */

                            SendMessage admin2gaXabar = new SendMessage();
                            admin2gaXabar.setChatId(659565242L);
                            admin2gaXabar.setText("➕Yangi foydalanuvchi.\n\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uFE0F\uD83D\uDE4B\uD83C\uDFFB\u200D♀\uFE0F Ismi : " +
                                    botUser.getName() + "\n\uD83D\uDCF1 Tel : " + botUser.getPhone_num()+"\n\uD83D\uDCDA "+botUser.getInterest());
                            sendMsg(admin2gaXabar); /** yangi user haqida admin2 ga xabar yuborish */
                            /** -------------------------------------------------- */

                            SendMessage azamatgaXabar = new SendMessage();
                            azamatgaXabar.setChatId(5952923848L);
                            azamatgaXabar.setText("➕Yangi foydalanuvchi.\n\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uFE0F\uD83D\uDE4B\uD83C\uDFFB\u200D♀\uFE0F Ismi : " +
                                    botUser.getName() + "\n\uD83D\uDCF1 Tel : " + botUser.getPhone_num()+"\n\uD83D\uDCDA "+botUser.getInterest());
                            sendMsg(azamatgaXabar); /** yangi user haqida admin2 ga xabar yuborish */

                            BotUser refUser = botUserService.getUserByPhone(currentUser.getRef_user_phone());
                            if (refUser != null) {
                                /** ref_user ning ref_count qiymatini 1 taga oshirish */
                                refUser.setRef_count(refUser.getRef_count() + 1);
                                botUserService.updateUser(refUser);
                                if (refUser.getRef_count() % 5 == 0) {
                                    SendMessage sendMessage = new SendMessage();
                                    sendMessage.setChatId(659565242L);
                                    sendMessage.setText("\uD83D\uDE09Yangi faol foydalanuvchi topildi\n" +
                                            "\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uFE0F\uD83D\uDE4B\uD83C\uDFFB\u200D♀\uFE0F Ism : " + refUser.getName() + "\n\uD83D\uDCF1Tel: " + refUser.getPhone_num() + "\n\uD83D\uDCB0Bonuslari:" + refUser.getRef_count() * 1000);
                                    sendMsg(sendMessage);
                                    SendMessage sendMessage2 = new SendMessage();
                                    sendMessage2.setChatId(5952923848L);
                                    sendMessage2.setText(("\uD83D\uDE09Yangi faol foydalanuvchi topildi\n" +
                                            "\uD83D\uDE4B\uD83C\uDFFB\u200D♂\uFE0F\uD83D\uDE4B\uD83C\uDFFB\u200D♀\uFE0F Ism : " + refUser.getName() + "\n\uD83D\uDCF1Tel: " + refUser.getPhone_num() + "\n\uD83D\uDCB0Bonuslari:" + refUser.getRef_count() * 1000));
                                    sendMsg(sendMessage2);
                                }
                                /** ref_user ga xabar yuborish */
                                SendMessage sendRefUserMessage = new SendMessage();
                                sendRefUserMessage.setChatId(refUser.getTg_id());
                                sendRefUserMessage.setText("\uD83E\uDD73Sizning referral linkingiz orqali yana bir foydalanuvchi ro'yxatdan o'tdi." +
                                        "\n\uD83D\uDCB0Bonuslaringiz " + botUserService.getUserById(refUser.getTg_id()).getRef_count() * 1000 + " so'm");
                                sendMsg(sendRefUserMessage);
                            }
                            SendMessage totalResultMessage = new SendMessage();
                            totalResultMessage.setChatId(currentUser.getTg_id());
                            totalResultMessage.setText("✅To'liq ro'yxatdan o'tdingiz.\n" + "\uD83D\uDD17Sizning referral linkingiz:\nhttps://t.me/" +
                                    getBotUsername() + "?start=" + currentUser.getTg_id() + "\n\uD83D\uDE80Ushbu linkni do'stlaringizga uzating." +
                                    "\n\n\uD83D\uDCB0Link orqali ro'yxatdan o'tgan har bir do'stingiz uchun sizga 1000 so'm bonus qo'shiladi.\n\uD83D\uDCB0 Hozir sizda 10.000 so'm bonus mavjud");
                            //------------------------------------//
                            ReplyKeyboardRemove removeButton = new ReplyKeyboardRemove(); /** keyboard button yashirish */
                            removeButton.setSelective(true);
                            removeButton.setRemoveKeyboard(true);
                            totalResultMessage.setReplyMarkup(removeButton);
                            sendMsg(totalResultMessage); /** button yashirildi va totalMessage yuborildi */
                        }
                    } else if (message.hasText() && currentUser.getStep().equals(UserStep.END)) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(currentUser.getTg_id());
                        sendMessage.setText("\uD83D\uDD17Sizning referral linkingiz:\nhttps://t.me/" + getBotUsername() + "?start=" +
                                currentUser.getTg_id() + "\n\uD83D\uDE80Ushbu linkni do'stlaringizga uzating." +
                                "\n\n\uD83D\uDCB0Link orqali ro'yxatdan o'tgan har bir do'stingiz uchun sizga 1000 so'm bonus qo'shiladi."
                                + "\n\uD83C\uDF81Sizda hozir " + currentUser.getRef_count() * 1000 + " so'm bonus mavjud.");
                        sendMsg(sendMessage);
                    }
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    public void createUser(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            BotUser currentUser = new BotUser();
            currentUser.setTg_id(message.getChatId()); /** userga tg_id set qilish */
            currentUser.setRef_user_phone("0");
            if (message.getText().startsWith("/start") && message.getText().length() >= 7 && message.getText().charAt(6) == ' ') {
                /** biror referral ssilka orqali kirsa */
                String refUserLink = message.getText().substring(7); /** referral link oxiridagi raqamlarni qirqib olish */
                Boolean isLinkDigit = true;
                /** ...keyin esa ularni raqamlikka check qilish */
                for (int i = 0; i < refUserLink.length(); i++) {
                    char symbol = refUserLink.charAt(i);
                    if (!Character.isDigit(symbol)) {
                        isLinkDigit = false;
                        break;
                    }
                }
                Long refUserId = null; /** link oxiridagi ref_id */
                if (isLinkDigit) {
                    refUserId = Long.valueOf(refUserLink);
                }
                if (botUserService.getUserById(refUserId) != null
                        && botUserService.getUserById(refUserId).getStatus().equals(UserStatus.ACTIVE)) {
                    /** mavjud id orqali */
                    currentUser.setRef_user_phone(botUserService.getUserById(refUserId).getPhone_num());
                    /** currentUserga xabar yuborish */
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(currentUser.getTg_id());
                    sendMessage.setText("✅Siz referral link orqali botga kirdingiz.\n");
                    sendMsg(sendMessage);
                } else if (currentUser.getTg_id().equals(refUserId)) {
                    /** o'z id si orqali referral qilsa */
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(currentUser.getTg_id());
                    sendMessage.setText("⚠\uFE0FO'zingizga referral bo'la olmaysiz.\n");
                    sendMsg(sendMessage);
                } else if (botUserService.getUserById(refUserId) == null ||
                        botUserService.getUserById(refUserId).getStatus().equals(UserStatus.NO_ACTIVE)) {
                    /** mavjudmas id orqali kirsa */
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(currentUser.getTg_id());
                    sendMessage.setText("⚠\uFE0FNoto'g'ri referral link orqali botga kirdingiz.\n");
                    sendMsg(sendMessage);
                }
            }
            botUserService.addUser(currentUser);/** yangi userni DB ga qo'shish */
        }
    }

    public void getContactFromUser(BotUser botUser) {
        KeyboardButton contactButton = new KeyboardButton("\uD83D\uDCF2Kontaktni yuborish");
        contactButton.setRequestContact(true);

        KeyboardRow row = new KeyboardRow();
        row.add(contactButton);

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setSelective(true);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(botUser.getTg_id());
        sendMessage.setText("\uD83D\uDCF1Telefon kontaktingizni yuboring");
        sendMessage.setReplyMarkup(markup);
        sendMsg(sendMessage);
    }

    public void createAdvertisingButton(BotUser botUser) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("✅ Tasdiqlash");
        row1.add("❌ Bekor qilish");

        keyboard.add(row1);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setSelective(true);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(botUser.getTg_id());
        sendMessage.setText("Xabar barcha bot foydalanuvchilariga yuborilishini tasdiqlaysizmi ?");
        sendMessage.setReplyMarkup(keyboardMarkup);
        sendMsg(sendMessage);
    }


    public Boolean checkValidityContact(Contact contact, BotUser currentUser) {
        if (contact.getPhoneNumber().length() == 13) {
            if (!contact.getPhoneNumber().startsWith("+998")) {
                return null;
            }
            currentUser.setPhone_num(contact.getPhoneNumber().substring(1));
        } else if (contact.getPhoneNumber().length() == 12) {
            if (!contact.getPhoneNumber().startsWith("998")) {
                return null;
            }
            currentUser.setPhone_num(contact.getPhoneNumber());
        }
        botUserService.updateUser(currentUser);
        return true;
    }

    public ReplyKeyboardMarkup createInterestButtons() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        keyboardMarkup.setSelective(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Ingliz tili");
        row1.add("Rus tili");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Kompyuter savodxonligi");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Web dasturlash");

        KeyboardRow row4 = new KeyboardRow();
        row4.add("IELTS");
        row4.add("Multilevel (CEFR)");

        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    public synchronized void getInterestFromUser(BotUser botUser) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(botUser.getTg_id());
        sendMessage.setText("\uD83D\uDCDAQaysi yo'nalishga qiziqasiz ?");
        /** Keyboard tugmalarni yaratish */
        sendMessage.setReplyMarkup(createInterestButtons());
        sendMsg(sendMessage);
    }


    public void sendAdvertising(Message message, BotUser admin) {
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setChatId(admin.getTg_id()); // Admin's chat ID
        List<Long> usersChatIds = botUserService.getAllTgId();
        for (Long userChatId : usersChatIds) {
            forwardMessage.setFromChatId(admin.getTg_id());
            forwardMessage.setChatId(userChatId);
            forwardMessage.setMessageId(message.getMessageId() - 2);
            try {
                execute(forwardMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }


    public static void setQuestionsMap() {
        questionMap.put(UserStep.NAME, "✍\uD83C\uDFFBIsmingizni kiriting.");
        questionMap.put(UserStep.PHONE_NUM, "Telefon raqamni ulashish.");
        questionMap.put(UserStep.INTEREST, "\uD83D\uDCDAQaysi yo'nalishga qiziqasiz ?");
    }

    public void sendMsg(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

}
