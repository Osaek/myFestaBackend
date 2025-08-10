package com.oseak.myFestaBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling   //배치 활성화
public class FestabackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FestabackendApplication.class, args);
	}

}
