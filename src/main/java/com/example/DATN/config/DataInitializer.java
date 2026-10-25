package com.example.DATN.config;

import com.example.DATN.entity.User;
import com.example.DATN.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createDefaultAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setEmail("admin@flarehub.local");
                admin.setFullName("Administrator");
                admin.setRole("ADMIN");
                admin.setIsActive(true);
                userRepository.save(admin);
                System.out.println(">>> Da tao tai khoan admin / admin123");
            }
        };
    }
}
