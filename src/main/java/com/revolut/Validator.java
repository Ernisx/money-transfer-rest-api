package com.revolut;

import java.math.BigDecimal;
import java.util.Currency;

import static com.revolut.ErrorMessage.INCORRECT_CURRENCY_CODE;
import static com.revolut.ErrorMessage.NEGATIVE_ACCOUNT_BALANCE;

public class Validator {

    public static void validateAccountBalance(BigDecimal balance) {
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(NEGATIVE_ACCOUNT_BALANCE.getErrorMessage());
        }
    }

    public static void validateCurrencyCode(String currency) {
        try {
            Currency.getInstance(currency);
        } catch (Exception e) {
            throw new IllegalArgumentException(INCORRECT_CURRENCY_CODE.getErrorMessage());
        }
    }

    public static void validateBalanceToMakeTransfer(BigDecimal balance, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
}
