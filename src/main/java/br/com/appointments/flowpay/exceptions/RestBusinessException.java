package br.com.appointments.flowpay.exceptions;

public class RestBusinessException extends RuntimeException {

    public RestBusinessException(String message) {
        super(message);
    }
}
