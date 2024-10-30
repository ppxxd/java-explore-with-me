package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class ErrorHandler {
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(FORMAT);

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflictException(final ConflictException ex) {
        return new ErrorResponse(
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Integrity constraint has been violated.",
                ex.getMessage(),
                LocalDateTime.now().format(dateTimeFormatter)
        );
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleObjectNotFoundException(final ObjectNotFoundException ex) {
        return new ErrorResponse(
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "The required object was not found.",
                ex.getMessage(),
                LocalDateTime.now().format(dateTimeFormatter)
        );
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(final ValidationException ex) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Incorrectly made request.",
                ex.getMessage(),
                LocalDateTime.now().format(dateTimeFormatter)
        );
    }
}
