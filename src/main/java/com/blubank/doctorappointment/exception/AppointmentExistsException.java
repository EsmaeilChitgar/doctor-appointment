package com.blubank.doctorappointment.exception;

public class AppointmentExistsException extends RuntimeException {
    public AppointmentExistsException(String message) {
        super(message);
    }
}
