package com.coresaken.mcserverlist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class McServerListApplication {

	public static void main(String[] args) {
		SpringApplication.run(McServerListApplication.class, args);
	}

}
