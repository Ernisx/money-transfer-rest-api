package com.revolut;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revolut.api.ExchangeService;
import com.revolut.impl.AccountServiceImpl;
import com.revolut.impl.ExchangeServiceImpl;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.revolut.AccountControllerTest.Currency.EUR;
import static com.revolut.AccountControllerTest.Currency.USD;
import static com.revolut.ErrorMessage.INCORRECT_CURRENCY_CODE;
import static com.revolut.ErrorMessage.NEGATIVE_ACCOUNT_BALANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountControllerTest {

    private final ExchangeService exchangeService = new ExchangeServiceImpl();

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
    public void addAccountTest() throws Exception {
        Account account = createAccount("1", new BigDecimal(10), "EUR");
        Account account2 = createAccount("2", new BigDecimal(10), "EUR");
        ApiTestUtils.request("POST", "/account", account);
        ApiTestUtils.request("POST", "/account", account2);
        ApiTestUtils.TestResponse res = ApiTestUtils.request("GET", "/accounts", null);

        JSONObject object = new JSONObject(res.json());
        Object data = object.get("data");
        List<Account> accounts = new ObjectMapper().readValue(data.toString(), new TypeReference<List<Account>>() {
        });

        String responseStatus = object.get("status").toString();
        assertTrue(StatusResponse.SUCCESS.getStatus().equalsIgnoreCase(responseStatus));
        assertEquals(2, accounts.size());
    }

    @Test
    public void transferEurToUsdTest() throws Exception {
        String accountNumber = "1";
        String accountNumber2 = "2";
        BigDecimal exchangeRate = exchangeService.getExchangeRate(EUR.getCurrency(), USD.getCurrency());

        Account account = createAccount(accountNumber, new BigDecimal(10), EUR.getCurrency());
        Account account2 = createAccount(accountNumber2, new BigDecimal(0), USD.getCurrency());
        ApiTestUtils.request("POST", "/account", account);
        ApiTestUtils.request("POST", "/account", account2);

        Transaction transaction = new Transaction(accountNumber, accountNumber2, new BigDecimal(10));
        ApiTestUtils.request("POST", "/transfer", transaction);
        ApiTestUtils.TestResponse res = ApiTestUtils.request("GET", "/accounts", null);

        JSONObject object = new JSONObject(res.json());
        Object data = object.get("data");
        List<Account> accounts = new ObjectMapper().readValue(data.toString(), new TypeReference<List<Account>>() {
        });

        String responseStatus = object.get("status").toString();
        assertTrue(StatusResponse.SUCCESS.getStatus().equalsIgnoreCase(responseStatus));

        isEqualToComparingAccountFieldByField(accounts.get(0), new Account(accountNumber, new BigDecimal(0), EUR.getCurrency()));
        isEqualToComparingAccountFieldByField(accounts.get(1), new Account(
                accountNumber2,
                makeTransfer(account2.getBalance(), transaction.getAmount(), exchangeRate),
                USD.getCurrency()));
    }

    @Test
    public void transferUsdToEurTest() throws Exception {
        String accountNumber = "1";
        String accountNumber2 = "2";

        BigDecimal exchangeRate = exchangeService.getExchangeRate(USD.getCurrency(), EUR.getCurrency());

        Account account = createAccount(accountNumber, new BigDecimal(0), EUR.getCurrency());
        Account account2 = createAccount(accountNumber2, new BigDecimal(10), USD.getCurrency());
        ApiTestUtils.request("POST", "/account", account);
        ApiTestUtils.request("POST", "/account", account2);

        Transaction transaction = new Transaction(accountNumber2, accountNumber, new BigDecimal(10));
        ApiTestUtils.request("POST", "/transfer", transaction);
        ApiTestUtils.TestResponse res = ApiTestUtils.request("GET", "/accounts", null);

        JSONObject object = new JSONObject(res.json());
        Object data = object.get("data");
        List<Account> accounts = new ObjectMapper().readValue(data.toString(), new TypeReference<List<Account>>() {
        });

        String responseStatus = object.get("status").toString();
        assertTrue(StatusResponse.SUCCESS.getStatus().equalsIgnoreCase(responseStatus));

        isEqualToComparingAccountFieldByField(accounts.get(1), new Account(accountNumber2, new BigDecimal(0), USD.getCurrency()));
        isEqualToComparingAccountFieldByField(accounts.get(0), new Account(
                accountNumber,
                makeTransfer(account.getBalance(), transaction.getAmount(), exchangeRate),
                EUR.getCurrency()));
    }

    @Test
    public void addAccountWithNegativeBalanceTest() {
        Account account = createAccount("1", new BigDecimal(-10), USD.getCurrency());
        ApiTestUtils.TestResponse res = ApiTestUtils.request("POST", "/account", account);

        String responseStatus = res.json().get("status");
        String errorMessage = res.json().get("message");

        assertTrue(StatusResponse.ERROR.getStatus().equalsIgnoreCase(responseStatus));
        assertEquals(NEGATIVE_ACCOUNT_BALANCE.getErrorMessage(), errorMessage);
    }

    @Test
    public void addAccountWithIncorrectCurrencyCodeTest() {
        Account account = createAccount("1", new BigDecimal(10), "XSASDAXX");
        ApiTestUtils.TestResponse res = ApiTestUtils.request("POST", "/account", account);

        String responseStatus = res.json().get("status");
        String errorMessage = res.json().get("message");

        assertTrue(StatusResponse.ERROR.getStatus().equalsIgnoreCase(responseStatus));
        assertEquals(errorMessage, INCORRECT_CURRENCY_CODE.getErrorMessage());
    }

    private void isEqualToComparingAccountFieldByField(Account actualAccount, Account expectedAccount) {
        assertEquals(expectedAccount.getId(), actualAccount.getId());
        assertEquals(expectedAccount.getBalance(), actualAccount.getBalance());
        assertEquals(expectedAccount.getCurrency(), actualAccount.getCurrency());
    }

    private BigDecimal makeTransfer(BigDecimal balance, BigDecimal amount, BigDecimal exchangeRate) {
        return balance.add(amount.multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP));
    }

    private Account createAccount(String accountNumber, BigDecimal balance, String currency) {
        Account account = new Account();
        account.setId(accountNumber);
        account.setBalance(balance);
        account.setCurrency(currency);
        return account;
    }

    protected enum Currency {
        USD("USD"),
        EUR("EUR");

        private String currency;

        Currency(String currency) {
            this.currency = currency;
        }

        public String getCurrency() {
            return currency;
        }
    }
}