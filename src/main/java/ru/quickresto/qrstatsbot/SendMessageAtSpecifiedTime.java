package ru.quickresto.qrstatsbot;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static ru.quickresto.qrstatsbot.TelegramMessage.botToken;

@Component
public class SendMessageAtSpecifiedTime {

    private final StatService statService = new StatService();
    private final TelegramClient telegramClient = new OkHttpTelegramClient(botToken);

    @Scheduled(cron = "0 20 8 * * ?")  // 8 часов на сервере = 13 нашим часам
    public void sendMessageAtSpecifiedTime() {
        try {
            String chatId = "-1002201257139";
            SendMessage message = new SendMessage(chatId, statService.collectionStatistics());
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
