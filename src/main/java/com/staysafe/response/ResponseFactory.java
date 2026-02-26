package com.staysafe.response;

import org.springframework.http.HttpStatus;

public class ResponseFactory {
    public SimpleResponseWithMessage getBadRequestResponse(String message) {
        message = null != message ? message : HttpStatus.BAD_REQUEST.getReasonPhrase();
        return new SimpleResponseWithMessage(false, HttpStatus.BAD_REQUEST.value(), message);
    }

    public SimpleResponseWithMessage getForbiddenResponse(String message) {
        message = null != message ? message : HttpStatus.FORBIDDEN.getReasonPhrase();
        return new SimpleResponseWithMessage(false, HttpStatus.FORBIDDEN.value(), message);
    }

    public SimpleResponseWithMessage getSuccessResponse(String message) {
        message = null != message ? message : HttpStatus.OK.getReasonPhrase();
        return new SimpleResponseWithMessage(false, HttpStatus.OK.value(), message);
    }
}
