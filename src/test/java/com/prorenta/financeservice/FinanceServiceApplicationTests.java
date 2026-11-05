package com.prorenta.financeservice;

import com.prorenta.financeservice.config.CurrentUserTestConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(CurrentUserTestConfiguration.class)
class FinanceServiceApplicationTests {

    @Test
    @DisplayName("Контекст приложения загружается")
    void contextLoads() {
        // Given: конфигурация приложения и тестовый профиль заданы аннотациями

        // When: Spring загружает контекст перед вызовом теста

        // Then: контекст загружен без ошибок
    }
}
