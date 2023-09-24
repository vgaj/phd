package com.github.vgaj.phd.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PhoneHomeDetectorApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(PhoneHomeDetectorApplication.class, args);
	}
}