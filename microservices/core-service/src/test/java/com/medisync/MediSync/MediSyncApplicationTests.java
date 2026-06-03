package com.medisync.MediSync;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class MediSyncApplicationTests {
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;


	@Test
	void contextLoads() {
	}

}


