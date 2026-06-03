package com.medisync.MediSync.config;

import com.medisync.MediSync.entity.User;
import com.medisync.MediSync.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.List;

@TestConfiguration
public class TestSecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .addFilterBefore((Filter) (request, response, chain) -> {
                // Dynamically get an existing user from the DB to avoid 404s/500s when controllers lookup the email
                String email = userRepository.findAll().stream()
                        .findFirst()
                        .map(User::getEmail)
                        .orElse("dummy@example.com");

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        email, 
                        "password123", 
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_DOCTOR"), new SimpleGrantedAuthority("ROLE_PATIENT")));
                SecurityContextHolder.getContext().setAuthentication(auth);
                chain.doFilter(request, response);
            }, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
