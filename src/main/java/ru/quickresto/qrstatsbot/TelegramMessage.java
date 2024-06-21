package ru.quickresto.qrstatsbot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
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


public class TelegramMessage implements LongPollingSingleThreadUpdateConsumer {
    static String botToken = "6563408491:AAGRIvjmdIL_jqJ7QyEroCGeJQlJiFIo9eE";
    private final TelegramClient telegramClient = new OkHttpTelegramClient(botToken);
    private final StatService statService = new StatService();
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText();

            try {
                SendMessage message = null;
                if (receivedText.equals("/start")) {
                    message = new SendMessage(chatId, "Выберите действие:");
                    message.setReplyMarkup(createKeyboard());
                } else if (receivedText.equals("Статистика")) {
                    message = new SendMessage(chatId, statService.collectionStatistics());
                }
                telegramClient.execute(message);
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

