package com.jmdecerio.spring_ai_embabel;

import com.embabel.agent.config.annotation.EnableAgents;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAgents
@SpringBootApplication
public class SpringAiEmbabelApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiEmbabelApplication.class, args);
	}

}
