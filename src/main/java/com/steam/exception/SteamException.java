package com.steam.exception;

public class SteamException extends Exception {
    private static final long serialVersionUID = 1L;

    public SteamException(String message) {
        super(message);
    }

    public SteamException(String message, Throwable cause) {
        super(message, cause);
    }
}
