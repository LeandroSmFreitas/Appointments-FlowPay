package br.com.appointments.flowpay.exceptions;

import java.util.List;

public class ValidationError extends RuntimeException {

    private final List<RestFieldErrors> fieldErrors;

    public ValidationError(String message, List<RestFieldErrors> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public List<RestFieldErrors> getFieldErrors() {
        return fieldErrors;
    }
}
