package ru.quickresto.qrstatsbot;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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

    public static final Logger log = LoggerFactory.getLogger(TelegramMessage.class);

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.group}")
    private String groupId;

    private final StatService statService;

    private TelegramClient telegramClient;

    public TelegramMessage(StatService statService) {
        this.statService = statService;
    }

    @PostConstruct
    public void postConstruct() {
        telegramClient = new OkHttpTelegramClient(botToken);

        TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
        try {
            botsApplication.registerBot(botToken, this);
        } catch (TelegramApiException e) {
            log.error("Error constructing telegram message service", e);
        }
    }

    @Scheduled(cron = "0 20 8 * * *")  // 8 часов на сервере = 13 нашим часам
    public void sendMessageAtSpecifiedTime() {
        sendMessageToGroup();
    }

    public void consume(Update update) {
        System.out.println("New telegram message: " + update.getMessage());

        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatIdBot = update.getMessage().getChatId().toString();
            String receivedText = update.getMessage().getText();

            try {
                SendMessage messageGroup = null;
                SendMessage messageBot = null;
                if (receivedText.equals("/start")) {
                    messageGroup = new SendMessage(groupId, "Выберите действие:");
                    messageBot = new SendMessage(chatIdBot, "Выберите действие:");
                    messageGroup.setReplyMarkup(createKeyboard());
                    messageBot.setReplyMarkup(createKeyboard());
                } else if (receivedText.equals("Статистика")) {
                    messageGroup = new SendMessage(groupId, statService.collectionStatistics());
                    messageBot = new SendMessage(chatIdBot, statService.collectionStatistics());
                }
                telegramClient.execute(messageGroup);
                telegramClient.execute(messageBot);
            } catch (TelegramApiException e) {
                log.error("Error processing message", e);
            }
        }
    }

    public void sendMessageToGroup() {
        try {
            SendMessage message = new SendMessage(groupId, statService.collectionStatistics());
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to group", e);
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

