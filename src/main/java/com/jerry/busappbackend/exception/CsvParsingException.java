package com.jerry.busappbackend.exception;

public class CsvParsingException extends RuntimeException {
    public CsvParsingException(Throwable e) {
        super("An error occurred during CSV parsing", e);
    }
}
