package ru.tbank.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.tbank.admin.exceptions.ChatNotFoundException;
import ru.tbank.admin.exceptions.InvalidFilterTypeException;
import ru.tbank.common.entity.FilterType;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class BadRequestRestControllerAdvice {

    private final MessageSource messageSource;

    @ExceptionHandler(InvalidFilterTypeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidFilterTypeException(
            @NotNull InvalidFilterTypeException exception,
            Locale locale
    ) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                getMessageByLocale("errors.400.title", locale));

        var errorMessage = getMessageByLocale(exception.getMessage(), locale)
                .formatted(Arrays.stream(FilterType.values())
                        .map(FilterType::getType)
                        .collect(Collectors.joining(", ")));

        problemDetail.setProperty("error", errorMessage);
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleChatNotFoundException(
            @NotNull ChatNotFoundException exception,
            Locale locale
    ) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                getMessageByLocale("errors.404.title", locale));

        var errorMessage = getMessageByLocale(exception.getMessage(), locale)
                .formatted(exception.getChatId());

        problemDetail.setProperty("error", errorMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }


    private String getMessageByLocale(String messageCode, Locale locale) {
        return messageSource.getMessage(messageCode, new Object[0],
                messageCode, locale);
    }
}
