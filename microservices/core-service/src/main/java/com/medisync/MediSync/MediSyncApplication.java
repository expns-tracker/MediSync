package com.medisync.MediSync;

import com.medisync.MediSync.config.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties.class)
@EnableFeignClients
public class MediSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediSyncApplication.class, args);
	}

}
