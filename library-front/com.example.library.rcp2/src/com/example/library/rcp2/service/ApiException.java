package com.example.library.rcp2.service;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
