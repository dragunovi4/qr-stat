package ru.quickresto.qrstatsbot;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;


@Service
public class TelegramMessage implements LongPollingSingleThreadUpdateConsumer {
    static String botToken = "7065012488:AAFAqTvmNjBZo4WuyCUH877UXRJweweGaFg";
    private final TelegramClient telegramClient = new OkHttpTelegramClient(botToken);


    @Autowired
    private StatService statService;


    @PostConstruct
    public void postConstruct() {
        TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        try {
            botsApplication.registerBot(botToken, this);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    public void consume(Update update) {

        System.out.println("New telegram message: " + update.getMessage());

        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatIdGroup = "-1002201257139";
            String chatIdBot = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText();

            try {
                SendMessage messageGroup = null;
                SendMessage messageBot = null;
                if (receivedText.equals("/start")) {
                    messageGroup = new SendMessage(chatIdGroup, "Выберите действие:");
                    messageBot = new SendMessage(chatIdBot, "Выберите действие:");
                    messageGroup.setReplyMarkup(createKeyboard());
                    messageBot.setReplyMarkup(createKeyboard());
                } else if (receivedText.equals("Статистика")) {
                    messageGroup = new SendMessage(chatIdGroup, statService.collectionStatistics());
                    messageBot = new SendMessage(chatIdBot, statService.collectionStatistics());
                }
                telegramClient.execute(messageGroup);
                telegramClient.execute(messageBot);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
    private ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder()
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();

        // Создание кнопок
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Статистика"));

        // Добавление кнопок на клавиатуру
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }
}

