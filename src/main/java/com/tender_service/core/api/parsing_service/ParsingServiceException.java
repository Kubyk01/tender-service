package com.tender_service.core.api.parsing_service;

public class ParsingServiceException extends RuntimeException{
    public ParsingServiceException(final String message,final Throwable cause){
        super(message, cause);
    }
}
