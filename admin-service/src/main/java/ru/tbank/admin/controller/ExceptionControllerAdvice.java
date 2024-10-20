package ru.tbank.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.tbank.admin.exceptions.ChatNotFoundException;
import ru.tbank.admin.exceptions.InvalidFilterTypeException;
import ru.tbank.common.entity.FilterType;

import java.util.Locale;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionControllerAdvice {

    private final MessageSource messageSource;

    @ExceptionHandler(InvalidFilterTypeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidFilterTypeException(InvalidFilterTypeException exception,
                                                                          Locale locale) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                messageSource.getMessage("errors.400.title", new Object[0],
                        "errors.400.title", locale));

        var errorMessage = messageSource.getMessage(exception.getMessage(),
                new Object[]{FilterType.getAvailableTypes()}, exception.getMessage(), locale);

        problemDetail.setProperty("error", errorMessage);
        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleChatNotFoundException(ChatNotFoundException exception,
                                                                     Locale locale) {
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                messageSource.getMessage("errors.404.title", new Object[0],
                        "errors.404.title", locale));

        problemDetail.setProperty("error", messageSource.getMessage(exception.getMessage(),
                new Object[]{String.valueOf(exception.getChatId())}, exception.getMessage(), locale));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }
}
