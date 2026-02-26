package com.staysafe.response;

public record SimpleResponseWithMessage(boolean status, int statusCode, String message) {
}
