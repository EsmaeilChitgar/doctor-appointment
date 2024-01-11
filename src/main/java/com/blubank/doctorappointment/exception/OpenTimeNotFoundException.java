package com.blubank.doctorappointment.exception;

public class OpenTimeNotFoundException extends RuntimeException {
    public OpenTimeNotFoundException(String message) {
        super(message);
    }
}