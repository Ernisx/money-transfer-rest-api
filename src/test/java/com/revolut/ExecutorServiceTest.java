package com.revolut;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.impl.AccountServiceImpl;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorServiceTest {

    private final int NUMBER_ACCOUNTS = 11;
    private final int NUM_THREADS = 8;
    private final int MAX_TRANSFER = 10;


    @BeforeAll
    static void setUp() {
        new AccountController(new AccountServiceImpl());
        Spark.awaitInitialization();
    }

    @AfterAll
    static void tearDown() {
        Spark.stop();
    }

    @Test
    public void executeTest() throws IOException {
        runThreads();

        ApiTestUtils.TestResponse res = ApiTestUtils.request("GET", "/accounts", null);
        JSONObject object = new JSONObject(res.json());
        Object data = object.get("data");
        List<Account> accounts = new ObjectMapper().readValue(data.toString(), new TypeReference<List<com.revolut.Account>>() {
        });

        for(Account account:accounts){
            System.out.println(account);
        }
    }


    public void runThreads() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<?>> futures = new ArrayList<>(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS; ++i) {
            Runnable task;
            task = new Runnable() {
                private final PairAccount pairAccount = getRandomPairAccount(initAccounts());

                @Override
                public void run() {
                    Transaction transaction = new Transaction(
                            pairAccount.getFromAccount().getId(),
                            pairAccount.getToAccount().getId(),
                            new BigDecimal((int) (Math.random() * MAX_TRANSFER)));
                    ApiTestUtils.request("POST", "/transfer", transaction);
                    ApiTestUtils.TestResponse res = ApiTestUtils.request("GET", "/accounts", null);
                }

            };

            futures.add(executorService.submit(task));
        }

        for (int i = 0; i < NUM_THREADS; ++i) {
            try {
                futures.get(i).get();
            } catch (InterruptedException e) {
                System.out.println("Transfer interrupted");
                executorService.shutdown();
                return;
            } catch (ExecutionException e) {
                System.out.println("Transfer execution interrupted");
                executorService.shutdown();
                return;
            }
        }
        executorService.shutdown();
    }

    private List<Account> initAccounts() throws IOException {

        for (int count = 1; count <= NUMBER_ACCOUNTS; count++) {
            Account account = createAccount(String.valueOf(count), new BigDecimal(1000), "EUR");
            ApiTestUtils.request("POST", "/account", account);
        }

        ApiTestUtils.TestResponse res = ApiTestUtils.request("GET", "/accounts", null);
        JSONObject object = new JSONObject(res.json());
        Object data = object.get("data");
        return new ObjectMapper().readValue(data.toString(), new TypeReference<List<com.revolut.Account>>() {
        });
    }

    private Account createAccount(String accountNumber, BigDecimal balance, String currency) {
        Account account = new Account();
        account.setId(accountNumber);
        account.setBalance(balance);
        account.setCurrency(currency);
        return account;
    }

    private PairAccount getRandomPairAccount(List<Account> accounts) {
        return new PairAccount(accounts.get((int) (Math.random() * NUMBER_ACCOUNTS)), accounts.get((int) (Math.random() * NUMBER_ACCOUNTS)));
    }

    class PairAccount {
        private final Account fromAccount;
        private final Account toAccount;

        public PairAccount(Account fromAccount, Account toAccount) {
            this.fromAccount = fromAccount;
            this.toAccount = toAccount;
        }

        public Account getFromAccount() {
            return fromAccount;
        }

        public Account getToAccount() {
            return toAccount;
        }
    }
}
