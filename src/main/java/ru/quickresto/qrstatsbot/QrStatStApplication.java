package ru.quickresto.qrstatsbot;

import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class QrStatStApplication {
	public static void main(String[] args) {
		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			botsApplication.registerBot(TelegramMessage.botToken, new TelegramMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
