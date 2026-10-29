package com.example.DATN.config;

import com.example.DATN.entity.Voucher;
import com.example.DATN.repository.VoucherRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
        @Bean
    CommandLineRunner createSampleVouchers(VoucherRepository voucherRepository) {
        return args -> {
            if (voucherRepository.count() == 0) {
                Voucher v1 = new Voucher();
                v1.setCode("GIAM10");
                v1.setDiscountType("PERCENT");
                v1.setDiscountValue(BigDecimal.valueOf(10));
                v1.setMinOrderAmount(BigDecimal.valueOf(500000));
                v1.setMaxDiscount(BigDecimal.valueOf(2000000));
                v1.setQuantity(100);
                v1.setStartDate(LocalDateTime.now());
                v1.setEndDate(LocalDateTime.now().plusMonths(6));
                voucherRepository.save(v1);

                Voucher v2 = new Voucher();
                v2.setCode("SALE200K");
                v2.setDiscountType("AMOUNT");
                v2.setDiscountValue(BigDecimal.valueOf(200000));
                v2.setMinOrderAmount(BigDecimal.valueOf(2000000));
                v2.setQuantity(50);
                v2.setStartDate(LocalDateTime.now());
                v2.setEndDate(LocalDateTime.now().plusMonths(6));
                voucherRepository.save(v2);
            }
        };
    }
}
