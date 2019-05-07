package com.revolut;

public enum ErrorMessage {

    NEGATIVE_ACCOUNT_BALANCE("Account balance cannot be negative"),
    INCORRECT_CURRENCY_CODE("Currency code does not exist"),
    INSUFFICIENT_FUND("Insufficiend fund");

    private String errorMessage;

    ErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
