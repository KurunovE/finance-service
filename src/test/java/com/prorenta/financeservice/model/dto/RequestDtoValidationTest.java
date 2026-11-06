package com.prorenta.financeservice.model.dto;

import com.prorenta.financeservice.model.enums.CategoryType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestDtoValidationTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 11);
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.byDefaultProvider().configure()
                .clockProvider(() -> Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC))
                .buildValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    @DisplayName("Пустое имя категории отклоняется")
    void categoryNameMustNotBeBlank(String name) {
        // Given: проверяются ограничения DTO

        // When
        var violations = validator.validate(new CreateCategoryRequestDto(name, CategoryType.EXPENSE));

        // Then
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("name");
    }

    @Test
    @DisplayName("Ограничения категории соответствуют БД")
    void categoryConstraintsMatchDatabase() {
        // Given: проверяются ограничения DTO

        // When
        var validNameViolations = validator.validate(new CreateCategoryRequestDto("a".repeat(30), CategoryType.EXPENSE));
        var longNameViolations = validator.validate(new CreateCategoryRequestDto("a".repeat(31), CategoryType.EXPENSE));
        var missingTypeViolations = validator.validate(new CreateCategoryRequestDto("Еда", null));

        // Then
        assertThat(validNameViolations).isEmpty();
        assertThat(longNameViolations).extracting(v -> v.getPropertyPath().toString()).contains("name");
        assertThat(missingTypeViolations).extracting(v -> v.getPropertyPath().toString()).contains("type");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.001", "1.001", "10000000000000.00"})
    @DisplayName("Некорректная сумма отклоняется")
    void invalidAmountsAreRejectedForCreateAndUpdate(String value) {
        // Given
        var amount = new BigDecimal(value);

        // When
        var createViolations = validator.validate(create(amount, null, null, TODAY));
        var updateViolations = validator.validate(new UpdateTransactionRequestDto(null, null, amount, null, null, null));

        // Then
        assertThat(createViolations).extracting(v -> v.getPropertyPath().toString()).contains("amount");
        assertThat(updateViolations).extracting(v -> v.getPropertyPath().toString()).contains("amount");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "9999999999999.99"})
    @DisplayName("Граничные суммы допустимы")
    void amountBoundariesAreAccepted(String value) {
        // Given
        var amount = new BigDecimal(value);

        // When
        var createViolations = validator.validate(create(amount, null, null, TODAY));
        var updateViolations = validator.validate(new UpdateTransactionRequestDto(null, null, amount, null, null, null));

        // Then
        assertThat(createViolations).isEmpty();
        assertThat(updateViolations).isEmpty();
    }

    @Test
    @DisplayName("Обязательные поля создания и обновления")
    void createRequiresFieldsButPatchAllowsOmittedFields() {
        // Given: проверяются ограничения DTO

        // When
        var createViolations = validator.validate(new CreateTransactionRequestDto(null, null, null, null, null, null));
        var updateViolations = validator.validate(new UpdateTransactionRequestDto(null, null, null, null, null, null));

        // Then
        assertThat(createViolations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("categoryId", "currencyId", "amount", "createdDate");
        assertThat(updateViolations).isEmpty();
    }

    @Test
    @DisplayName("Ограничения текста и даты транзакции")
    void transactionTextAndDateConstraintsApplyToCreateAndUpdate() {
        // Given
        var amount = new BigDecimal("1.00");
        var invalidCreate = create(amount, "a".repeat(51), "a".repeat(256), TODAY.plusDays(1));
        var invalidUpdate = new UpdateTransactionRequestDto(null, null, amount,
                "a".repeat(51), "a".repeat(256), TODAY.plusDays(1));

        // When
        var createViolations = validator.validate(create(amount, "a".repeat(50), "a".repeat(255), TODAY));
        var updateViolations = validator.validate(new UpdateTransactionRequestDto(null, null, amount,
                "a".repeat(50), "a".repeat(255), TODAY));
        var invalidViolations = new ArrayList<Set<ConstraintViolation<Object>>>();

        for (Object dto : new Object[]{invalidCreate, invalidUpdate}) {
            invalidViolations.add(validator.validate(dto));
        }

        // Then
        assertThat(createViolations).isEmpty();
        assertThat(updateViolations).isEmpty();
        assertThat(invalidViolations).allSatisfy(violations ->
                assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                        .containsExactlyInAnyOrder("bank", "description", "createdDate"));
    }

    @ParameterizedTest
    @CsvSource({"-1,10,page", "0,0,pageSize", "0,101,pageSize", "0,2147483647,pageSize"})
    @DisplayName("Некорректная пагинация отклоняется")
    void invalidPaginationIsRejected(int page, int pageSize, String field) {
        // Given: проверяются ограничения DTO

        // When
        var violations = validator.validate(new FilterTransactionRequestDto(null, null, null, page, pageSize, null, null));

        // Then
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains(field);
    }

    @Test
    @DisplayName("Значения по умолчанию и границы периода")
    void filterAcceptsDefaultsInclusivePeriodAndOneSidedPeriods() {
        // Given
        var defaults = new FilterTransactionRequestDto(null, null, null, null, null, null, null);

        // When
        var defaultViolations = validator.validate(defaults);
        var periodViolations = new ArrayList<Set<ConstraintViolation<FilterTransactionRequestDto>>>();

        for (var start : new LocalDate[]{null, TODAY}) {
            for (var end : new LocalDate[]{null, TODAY}) {
                periodViolations.add(validator.validate(
                        new FilterTransactionRequestDto(null, start, end, 0, 100, "DESC", "amount")));
            }
        }

        var invalidPeriodViolations = validator.validate(
                new FilterTransactionRequestDto(null, TODAY, TODAY.minusDays(1), null, null, null, null));

        // Then
        assertThat(defaultViolations).isEmpty();
        assertThat(defaults.page()).isZero();
        assertThat(defaults.pageSize()).isEqualTo(10);
        assertThat(periodViolations).allSatisfy(violations -> assertThat(violations).isEmpty());
        assertThat(invalidPeriodViolations).extracting(v -> v.getPropertyPath().toString()).contains("validPeriod");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "ascending"})
    @DisplayName("Некорректное направление сортировки")
    void invalidSortDirectionsAreRejected(String direction) {
        // Given: проверяются ограничения DTO

        // When
        var violations = validator.validate(new FilterTransactionRequestDto(null, null, null, null, null, direction, null));

        // Then
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("validSortDirection");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "unknown", "userId"})
    @DisplayName("Некорректное поле сортировки")
    void invalidSortFieldsAreRejected(String field) {
        // Given: проверяются ограничения DTO

        // When
        var violations = validator.validate(new FilterTransactionRequestDto(null, null, null, null, null, null, field));

        // Then
        assertThat(violations).extracting(v -> v.getPropertyPath().toString()).contains("validFieldSort");
    }

    private static CreateTransactionRequestDto create(BigDecimal amount, String bank, String description, LocalDate date) {
        return new CreateTransactionRequestDto(UUID.randomUUID(), UUID.randomUUID(), amount, bank, description, date);
    }
}
