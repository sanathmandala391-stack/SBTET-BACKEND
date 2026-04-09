package com.sbtetAttendance.sbtet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class SbtetApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbtetApplication.class, args);
	}

}
