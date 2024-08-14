package com.coresaken.mcserverlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class McServerListApplication {

	public static void main(String[] args) {
		SpringApplication.run(McServerListApplication.class, args);
	}
}
