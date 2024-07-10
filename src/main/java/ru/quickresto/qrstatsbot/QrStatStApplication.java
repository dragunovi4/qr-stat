package ru.quickresto.qrstatsbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QrStatStApplication {


	public static void main(String[] args) {
		SpringApplication.run(QrStatStApplication.class, args);
		SendMessageAtSpecifiedTime sendMessageAtSpecifiedTime = new SendMessageAtSpecifiedTime();
		sendMessageAtSpecifiedTime.sendMessageAtSpecifiedTime();
	}
}
