package com.dg.quotegenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class QuoteGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuoteGeneratorApplication.class, args);
	}
}
