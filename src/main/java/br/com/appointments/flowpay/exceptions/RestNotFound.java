package br.com.appointments.flowpay.exceptions;

public class RestNotFound extends RuntimeException {

    public RestNotFound(String message) {
        super(message);
    }
}
