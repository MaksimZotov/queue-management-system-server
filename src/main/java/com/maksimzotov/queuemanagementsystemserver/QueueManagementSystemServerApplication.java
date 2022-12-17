package com.maksimzotov.queuemanagementsystemserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SpringBootApplication
public class QueueManagementSystemServerApplication {

	public static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

	public static void main(String[] args) {
		SpringApplication.run(QueueManagementSystemServerApplication.class, args);
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
