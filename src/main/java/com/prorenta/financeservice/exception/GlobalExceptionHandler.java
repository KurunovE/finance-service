package com.prorenta.financeservice.exception;

import com.prorenta.financeservice.model.dto.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZonedDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            CategoryNotFoundException.class,
            CurrencyNotFoundException.class
    })
    public ResponseEntity<ErrorDto> handleNotFoundException(RuntimeException ex) {
        log.warn("Ресурс не найден: {}", ex.getMessage());
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(ex.getMessage())
                .zonedDateTime(ZonedDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleAllUnhandledExceptions(Exception ex) {
        log.error("Внутренняя ошибка сервера: ", ex);
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Произошла непредвиденная ошибка")
                .zonedDateTime(ZonedDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }
}
