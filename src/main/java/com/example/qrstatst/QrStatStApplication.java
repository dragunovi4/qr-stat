package com.example.qrstatst;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@SpringBootApplication
public class QrStatStApplication {
	public static void main(String[] args) {
		StatService statService = new StatService();
		statService.collectionStatistics();
		String token = TelegramMessage.botToken;
		try {
			TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
			botsApplication.registerBot(token, new TelegramMessage());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
