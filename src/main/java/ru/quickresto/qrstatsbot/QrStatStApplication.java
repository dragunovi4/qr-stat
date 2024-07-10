package ru.quickresto.qrstatsbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QrStatStApplication {


	public static void main(String[] args) {
		SpringApplication.run(QrStatStApplication.class, args);
	}
}
