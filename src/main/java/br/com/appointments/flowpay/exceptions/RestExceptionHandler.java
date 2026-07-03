package br.com.appointments.flowpay.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(RestNotFound.class)
    public ResponseEntity<RestErrorMessage> handleNotFound(RestNotFound ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(RestBusinessException.class)
    public ResponseEntity<RestErrorMessage> handleBusiness(RestBusinessException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), List.of());
    }

    @ExceptionHandler(ValidationError.class)
    public ResponseEntity<RestErrorMessage> handleValidationError(ValidationError ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), ex.getFieldErrors());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestErrorMessage> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<RestFieldErrors> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new RestFieldErrors(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RestErrorMessage> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<RestFieldErrors> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new RestFieldErrors(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RestErrorMessage> handleUnreadableMessage(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request body", request.getRequestURI(), List.of());
    }

    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ResponseEntity<Void> handleAsyncRequestTimeout() {
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleUnexpected(Exception ex, HttpServletRequest request) {
        if (isSseRequest(request)) {
            return ResponseEntity.noContent().build();
        }

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected internal error", request.getRequestURI(), List.of());
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);

        return request.getRequestURI().endsWith("/api/v1/dashboard/events")
                || MediaType.TEXT_EVENT_STREAM_VALUE.equals(request.getContentType())
                || (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE));
    }

    private ResponseEntity<RestErrorMessage> build(
            HttpStatus status,
            String message,
            String path,
            List<RestFieldErrors> fieldErrors
    ) {
        RestErrorMessage body = new RestErrorMessage(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        );

        return ResponseEntity.status(status).body(body);
    }
}
