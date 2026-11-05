package com.prorenta.financeservice.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerValidationTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Ошибки полей и объекта возвращаются клиенту")
    void handlesBothFieldAndObjectErrors() throws Exception {
        // Given
        var binding = new BeanPropertyBindingResult(new Object(), "request");
        binding.addError(new FieldError("request", "amount", "Некорректная сумма"));
        binding.addError(new ObjectError("request", "Некорректный запрос"));
        var exception = new MethodArgumentNotValidException(
                new MethodParameter(GlobalExceptionHandler.class.getMethod(
                        "handleValidationException",
                        MethodArgumentNotValidException.class
                ), 0), binding
        );

        // When
        var response = handler.handleValidationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull().satisfies(body ->
                assertThat(body.details())
                        .containsEntry("amount", "Некорректная сумма")
                        .containsEntry("request", "Некорректный запрос"));
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 500})
    @DisplayName("Обработчик валидации сохраняет статус")
    void methodValidationUsesCompatibleHandlerAndPreservesStatus(int status) throws Exception {
        // Given
        var method = GlobalExceptionHandler.class.getMethod(
                "handleMethodValidationException",
                HandlerMethodValidationException.class
        );
        var parameter = new MethodParameter(method, status == 500 ? -1 : 0);
        var result = new ParameterValidationResult(parameter, null, List.of(
                new DefaultMessageSourceResolvable(new String[]{"invalid"}, "Некорректный запрос")),
                null, null, null, (error, sourceType) -> {
                    throw new IllegalArgumentException("Исходный объект ошибки не задан");
                });
        var exception = new HandlerMethodValidationException(
                MethodValidationResult.create(handler, method, List.of(result))
        );

        // When
        var resolved = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class)
                .resolveMethod(exception);
        var response = handler.handleMethodValidationException(exception);

        // Then
        assertThat(resolved).isNotNull().satisfies(resolvedMethod ->
                assertThat(resolvedMethod.getParameterTypes()[0]).isEqualTo(HandlerMethodValidationException.class));
        assertThat(response.getStatusCode().value()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull().satisfies(body -> {
            assertThat(body.status().value()).isEqualTo(status);
            assertThat(body.details()).containsValue("Некорректный запрос");
        });
    }
}
