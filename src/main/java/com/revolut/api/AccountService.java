package com.revolut.api;

import com.revolut.Account;

import java.math.BigDecimal;
import java.util.Collection;

public interface AccountService {

    void createAccount(Account account);

    Account getAccount(String id);

    Collection<Account> getAccounts();

    void transfer(Account debit, Account credit, BigDecimal amount);
}
