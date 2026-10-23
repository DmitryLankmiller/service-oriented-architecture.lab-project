package ru.ifmo.se.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import java.time.OffsetDateTime;
import java.util.Comparator;

import org.openapitools.model.ErrorDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.ifmo.se.exception.FlatNotFoundException;
import ru.ifmo.se.exception.MissingHouseException;
import ru.ifmo.se.exception.InvalidQueryException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(InvalidQueryException.class)
    public ResponseEntity<ErrorDto> invalidQuery(InvalidQueryException exception) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), exception.getField());
    }

    @ExceptionHandler(FlatNotFoundException.class)
    public ResponseEntity<ErrorDto> notFound(FlatNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "FLAT_NOT_FOUND", exception.getMessage(), "id");
    }

    @ExceptionHandler(MissingHouseException.class)
    public ResponseEntity<ErrorDto> missingHouse(MissingHouseException exception) {
        return error(HttpStatus.CONFLICT, "MISSING_PARENT_OBJECT", exception.getMessage(), "house");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> constraintViolation(ConstraintViolationException exception) {
        ConstraintViolation<?> violation = exception.getConstraintViolations().stream()
                .sorted(Comparator.comparing(
                        item -> item.getPropertyPath().toString()))
                .findFirst()
                .orElseThrow();

        boolean methodParameter = false;
        for (var node : violation.getPropertyPath()) {
            methodParameter = node.getKind() == ElementKind.PARAMETER;
        }

        return error(methodParameter ? HttpStatus.BAD_REQUEST : HttpStatus.UNPROCESSABLE_CONTENT,
                methodParameter ? "BAD_REQUEST" : "VALIDATION_ERROR",
                violation.getMessage(),
                violation.getPropertyPath().toString());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> invalidArgument(MethodArgumentNotValidException exception) {
        var fieldError = exception.getBindingResult().getFieldError();
        return error(HttpStatus.UNPROCESSABLE_CONTENT,
                "VALIDATION_ERROR",
                fieldError == null ? "Некорректное тело запроса" : fieldError.getDefaultMessage(),
                fieldError == null ? null : fieldError.getField());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> dataIntegrityViolation() {
        return error(HttpStatus.UNPROCESSABLE_CONTENT,
                "VALIDATION_ERROR",
                "Данные нарушают ограничения базы данных",
                null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> unreadableBody() {
        return error(HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Некорректное XML-тело запроса",
                null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> invalidParameterType(MethodArgumentTypeMismatchException exception) {
        return error(HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Некорректный тип параметра",
                exception.getName());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorDto> unsupportedMediaType() {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED_MEDIA_TYPE",
                "Ожидался application/xml",
                null);
    }

    private ResponseEntity<ErrorDto> error(HttpStatus status, String code, String message, String field) {
        ErrorDto body = new ErrorDto(status.value(), code, message, OffsetDateTime.now()).field(field);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_XML)
                .body(body);
    }
}
