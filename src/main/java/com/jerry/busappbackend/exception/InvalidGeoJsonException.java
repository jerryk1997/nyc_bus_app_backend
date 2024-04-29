package com.jerry.busappbackend.exception;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;

public class InvalidGeoJsonException extends Exception {
    public InvalidGeoJsonException() {
        super();
    }

    public InvalidGeoJsonException(Set<ValidationMessage> validationMessages, JsonNode invalidNode) {
        super(buildExceptionMessage(validationMessages, invalidNode));
    }

    private static String buildExceptionMessage(Set<ValidationMessage> validationMessages, JsonNode invalidNode) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("\n============================ Invalid GeoJson ============================\n\n");
        messageBuilder.append(invalidNode + "\n\n\n");
        for (ValidationMessage validationMessage : validationMessages) {
            messageBuilder.append(validationMessage).append("\n");
        }
        
        messageBuilder.append("\n==========================================================================");
        
        return messageBuilder.toString();
    }
}
