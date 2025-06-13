package ru.tbank.admin.controller;

import jakarta.validation.ConstraintViolationException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.tbank.admin.exceptions.ChatNotFoundException;
import ru.tbank.admin.exceptions.InvalidFilterTypeException;
import ru.tbank.admin.exceptions.SpamProtectionExclusionValidationException;
import ru.tbank.admin.exceptions.TextFilterExclusionValidationException;
import ru.tbank.admin.exceptions.UserNotFoundException;
import ru.tbank.admin.exceptions.UsernameNotFoundException;
import ru.tbank.common.entity.enums.FilterType;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionControllerAdvice {

    private static final String ERROR_400_TITLE = "errors.400.title";
    private static final String ERROR_401_TITLE = "errors.401.title";
    private static final String ERROR_404_TITLE = "errors.404.title";
    private static final String ERRORS_FIELD = "errors";
    private final MessageSource messageSource;

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolationException(
            @NonNull ConstraintViolationException exception,
            Locale locale
    ) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageSource.getMessage(ERROR_400_TITLE, new Object[0], ERROR_400_TITLE, locale)
        );

        problemDetail.setProperty(ERRORS_FIELD,
                exception.getConstraintViolations().stream()
                        .map(violation ->
                                messageSource.getMessage(
                                        violation.getMessage(),
                                        new Object[0],
                                        violation.getMessage(),
                                        locale
                                ))
                        .toList());

        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(@NonNull BindException exception, Locale locale) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                messageSource.getMessage(ERROR_400_TITLE, new Object[0], ERROR_400_TITLE, locale)
        );

        problemDetail.setProperty(
                ERRORS_FIELD,
                exception.getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .toList()
        );

        return ResponseEntity.badRequest()
                .body(problemDetail);
    }

    @ExceptionHandler(InvalidFilterTypeException.class)
    public ResponseEntity<ProblemDetail> handleInvalidFilterTypeException(
            @NonNull InvalidFilterTypeException exception,
            Locale locale
    ) {
        return createProblemDetailResponse(
                HttpStatus.BAD_REQUEST,
                ERROR_400_TITLE,
                exception.getMessage(),
                new Object[] {FilterType.getAvailableTypes()},
                locale
        );
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleChatNotFoundException(
            @NonNull ChatNotFoundException exception,
            Locale locale
    ) {
        return createProblemDetailResponse(
                HttpStatus.NOT_FOUND,
                ERROR_404_TITLE,
                exception.getMessage(),
                new Object[] {String.valueOf(exception.getChatId())},
                locale
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(
            @NonNull UsernameNotFoundException exception,
            Locale locale
    ) {
        return createProblemDetailResponse(
                HttpStatus.UNAUTHORIZED,
                ERROR_401_TITLE,
                exception.getMessage(),
                new Object[] {exception.getUsername()},
                locale
        );
    }

    @ExceptionHandler(TextFilterExclusionValidationException.class)
    public ResponseEntity<ProblemDetail> handleTextFilterExclusionValidationException(
            @NonNull TextFilterExclusionValidationException exception,
            Locale locale
    ) {
        return createProblemDetailResponse(
                HttpStatus.BAD_REQUEST,
                ERROR_400_TITLE,
                exception.getMessage(),
                new Object[] {exception.getExclusion()},
                locale
        );
    }

    @ExceptionHandler(SpamProtectionExclusionValidationException.class)
    public ResponseEntity<ProblemDetail> handleSpamProtectionExclusionValidationException(
            @NonNull SpamProtectionExclusionValidationException exception,
            Locale locale
    ) {
        return createProblemDetailResponse(
                HttpStatus.BAD_REQUEST,
                ERROR_400_TITLE,
                exception.getMessage(),
                new Object[] {exception.getExclusion()},
                locale
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(
            @NonNull UserNotFoundException exception,
            Locale locale
    ) {
        return createProblemDetailResponse(
                HttpStatus.NOT_FOUND,
                ERROR_404_TITLE,
                exception.getMessage(),
                new Object[] {String.valueOf(exception.getUserId())},
                locale
        );
    }

    private @NonNull ResponseEntity<ProblemDetail> createProblemDetailResponse(
            HttpStatus status,
            String titleCode,
            String errorMessageCode,
            Object[] errorMessageArgs,
            Locale locale
    ) {
        var problemDetail = ProblemDetail.forStatusAndDetail(
                status,
                messageSource.getMessage(titleCode, new Object[0], titleCode, locale)
        );

        var errorMessage = messageSource.getMessage(
                errorMessageCode,
                errorMessageArgs,
                errorMessageCode,
                locale
        );

        problemDetail.setProperty("error", errorMessage);
        return ResponseEntity.status(status).body(problemDetail);
    }
}
