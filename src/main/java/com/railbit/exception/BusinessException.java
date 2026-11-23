package com.railbit.exception;

/** A business rule was violated; the message is safe to show to the user. */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
