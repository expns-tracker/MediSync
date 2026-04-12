package com.medisync.MediSync;

import com.medisync.MediSync.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
public class MediSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediSyncApplication.class, args);
	}

}
