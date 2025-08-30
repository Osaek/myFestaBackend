package com.oseak.myFestaBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
	servers = {
		@Server(url = "/api", description = "Default API"),
		@Server(url = "http://localhost:8080", description = "Local Dev"),
	}
)
@SpringBootApplication
@EnableScheduling   //배치 활성화
public class FestabackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FestabackendApplication.class, args);
	}

}
