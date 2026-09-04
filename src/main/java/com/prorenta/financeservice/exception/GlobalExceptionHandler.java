package com.prorenta.financeservice.exception;

import com.prorenta.financeservice.model.dto.ErrorDto;
import com.prorenta.financeservice.model.dto.MappingErrorDto;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            CategoryNotFoundException.class,
            CurrencyNotFoundException.class,
            TransactionNotFoundException.class,
            FeignException.NotFound.class,
            UserNotFoundException.class,
            NoResourceFoundException.class
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

    @ExceptionHandler(LimitExceededException.class)
    public ResponseEntity<ErrorDto> handleLimitExceededException(LimitExceededException ex) {
        log.warn("Лимит исчерпан: {}", ex.getMessage());
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(FeignException.BadRequest.class)
    public ResponseEntity<ErrorDto> handleFeignBadRequestException(FeignException.BadRequest ex) {
        log.warn("Некорректный запрос к внешнему сервису: {}", ex.getMessage());
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class
    })
    public ResponseEntity<MappingErrorDto> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Ошибка валидации входящего запроса: {}", errors);
        MappingErrorDto errorDto = MappingErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Ошибка валидации данных")
                .zonedDateTime(ZonedDateTime.now())
                .details(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.warn("Ошибка конвертации параметра: {}", ex.getMessage());
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Неверный формат параметра: " + ex.getName())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("Некорректное тело входящего запроса: {}", ex.getMessage());
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Некорректное тело запроса: проверьте синтаксис JSON и переданные типы данных")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Нарушение уникальности имен: {}", ex.getMessage());
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.CONFLICT)
                .message("Конфликт данных: запись с такими параметрами уже существует")
                .zonedDateTime(ZonedDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    @ExceptionHandler(FeignException.FeignServerException.class)
    public ResponseEntity<ErrorDto> handleFeignServerException(FeignException.FeignServerException ex) {
        log.error("Ошибка на стороне внешнего сервиса", ex);
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.BAD_GATEWAY)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorDto);
    }

    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<ErrorDto> handleRetryableException(RetryableException ex) {
        log.error("Внешний сервис недоступен", ex);
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleAllUnhandledException(Exception ex) {
        log.error("Внутренняя ошибка сервера: {}", ex.getMessage(), ex);
        ErrorDto errorDto = ErrorDto.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .message("Произошла непредвиденная ошибка")
                .zonedDateTime(ZonedDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }
}
