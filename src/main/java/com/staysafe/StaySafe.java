package com.staysafe;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class StaySafe {
	static void main(String[] args) {
		new SpringApplicationBuilder(StaySafe.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}
}
