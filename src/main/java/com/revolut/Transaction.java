package com.revolut;

import java.math.BigDecimal;

public class Transaction {

    private String debitAccountNumber;
    private String creditAccountNumber;
    private BigDecimal amount;

    public Transaction(String debitAccountNumber, String creditAccountNumber, BigDecimal amount) {
        this.debitAccountNumber = debitAccountNumber;
        this.creditAccountNumber = creditAccountNumber;
        this.amount = amount;
    }

    public String getDebitAccountNumber() {
        return debitAccountNumber;
    }

    public void setDebitAccountNumber(String debitAccountNumber) {
        this.debitAccountNumber = debitAccountNumber;
    }

    public String getCreditAccountNumber() {
        return creditAccountNumber;
    }

    public void setCreditAccountNumber(String creditAccountNumber) {
        this.creditAccountNumber = creditAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transaction{");
        sb.append("debitAccountNumber='").append(debitAccountNumber).append('\'');
        sb.append(", creditAccountNumber='").append(creditAccountNumber).append('\'');
        sb.append(", amount=").append(amount);
        sb.append('}');
        return sb.toString();
    }
}
