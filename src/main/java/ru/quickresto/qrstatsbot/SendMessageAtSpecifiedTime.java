package ru.quickresto.qrstatsbot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static ru.quickresto.qrstatsbot.TelegramMessage.botToken;

public class SendMessageAtSpecifiedTime {

    private final StatService statService = new StatService();
    private final TelegramClient telegramClient = new OkHttpTelegramClient(botToken);
    public void sendMessageAtSpecifiedTime() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            try {
                String chatId = "-1002201257139";

                SendMessage message = new SendMessage(chatId, statService.collectionStatistics());
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        };

        long initialDelay = computeInitialDelay();
        long period = TimeUnit.DAYS.toMillis(1);

        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private long computeInitialDelay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 37);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date scheduledTime = calendar.getTime();
        Date currentTime = new Date();

        if (scheduledTime.before(currentTime)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            scheduledTime = calendar.getTime();
        }

        return scheduledTime.getTime() - currentTime.getTime();
    }
}
