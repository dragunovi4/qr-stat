package ru.quickresto.qrstatsbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

@SpringBootApplication
public class QrStatStApplication {


	public static void main(String[] args) {
		SpringApplication.run(QrStatStApplication.class, args);
	}

	/*public static void main(String[] args) {

		System.out.println("QR Stat Bot starting!");

		try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
			botsApplication.registerBot(TelegramMessage.botToken, new TelegramMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
