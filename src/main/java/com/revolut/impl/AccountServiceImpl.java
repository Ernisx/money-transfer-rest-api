package com.revolut.impl;

import com.revolut.Account;
import com.revolut.api.AccountService;
import com.revolut.api.ExchangeService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.revolut.ErrorMessage.INSUFFICIENT_FUND;

public class AccountServiceImpl implements AccountService {

    private ExchangeService exchangeService = new ExchangeServiceImpl();

    private HashMap<String, Account> accountMap;

    public AccountServiceImpl() {
        accountMap = new HashMap<>();
    }

    @Override
    public void createAccount(Account account) {
        accountMap.put(account.getId(), account);
    }

    @Override
    public Collection<Account> getAccounts() {
        return accountMap.values();
    }

    @Override
    public Account getAccount(String id) {
        return null;
    }

    @Override
    public synchronized void transfer(Account debitAccount, Account creditAccount, BigDecimal amount) {
        debitAccount.setLock(new ReentrantLock());
        creditAccount.setLock(new ReentrantLock());
        boolean fromLock = false;
        boolean toLock = false;

        try {
            while (!fromLock || !toLock) {
                fromLock = debitAccount.getLock().tryLock(
                        (long) (Math.random() * 1000), TimeUnit.MILLISECONDS);
                toLock = creditAccount.getLock().tryLock((long) (Math.random() * 1000),
                        TimeUnit.MILLISECONDS);
            }
            BigDecimal exchangeRate = exchangeService.getExchangeRate(
                    debitAccount.getCurrency(),
                    creditAccount.getCurrency());

            if (debit(debitAccount, amount)) {
                credit(creditAccount, amount, exchangeRate);
            }
            System.out.println(creditAccount.getBalance());
        } catch (InterruptedException e) {
            System.out.println("Transfer from account #" + debitAccount.getId()
                    + " to account #" + creditAccount.getId() + " was interrupted");
        } finally {
            debitAccount.getLock().unlock();
            creditAccount.getLock().unlock();
        }
    }

    private boolean debit(Account debitAccount, BigDecimal amount) {
        if (debitAccount.getBalance().compareTo(amount) >= 0) {
            debitAccount.setBalance(debitAccount.getBalance().subtract(amount));
            return true;
        }
        return false;
        //throw new RuntimeException(INSUFFICIENT_FUND.getErrorMessage());
    }

    private void credit(Account creditAccount, BigDecimal amount, BigDecimal exchangeRate) {
        creditAccount.setBalance(creditAccount.getBalance().add(amount).multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP));

    }
}
