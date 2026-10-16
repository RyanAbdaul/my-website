package com.ryan.personal_website_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableAspectJAutoProxy
public class PersonalWebsiteBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PersonalWebsiteBackendApplication.class, args);
	}

}
