package com.kinnarastudio.kecakplugins.heatmapreportmenu;

public class RestApiException extends Exception {
    private final int httpErrorCode;

    public RestApiException(int errorCode, String message) {
        super(message);
        this.httpErrorCode = errorCode;
    }

    public int getErrorCode() {
        return httpErrorCode;
    }
}
