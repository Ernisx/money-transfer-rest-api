package com.revolut;

import com.google.gson.Gson;
import com.revolut.api.AccountService;

import java.util.Collection;
import java.util.Optional;

import static spark.Spark.get;
import static spark.Spark.post;

public class AccountController {

    public AccountController(AccountService accountService) {
        get("/accounts", (request, response) -> {
            response.type("application/json");
            return new Gson().toJson(new Response(StatusResponse.SUCCESS, new Gson().toJsonTree(accountService.getAccounts())));
        });

        get("/account/:id", (request, response) -> new Gson().toJson(
                new Response(StatusResponse.SUCCESS, new Gson().toJsonTree(accountService.getAccount(request.params(":id"))))));

        post("/account", (request, response) -> {
            try {
                response.type("application/json");
                Account account = new Gson().fromJson(request.body(), Account.class);
                Validator.validateAccountBalance(account.getBalance());
                Validator.validateCurrencyCode(account.getCurrency());
                accountService.createAccount(account);
                return new Gson().toJson(new Response(StatusResponse.SUCCESS));
            } catch (Exception e) {
                return new Gson().toJson(new Response(StatusResponse.ERROR, e.getMessage()));
            }
        });

        post("/transfer", (request, response) -> {
            response.type("application/json");
            Transaction transaction = new Gson().fromJson(request.body(), Transaction.class);
            Collection<Account> accounts = accountService.getAccounts();
            Optional<Account> matchingObject = accounts.stream().filter(account -> account.getId().equals(transaction.getCreditAccountNumber())).findFirst();
            Account creditAccount = matchingObject.get();
            matchingObject = accounts.stream().filter(account -> account.getId().equals(transaction.getDebitAccountNumber())).findFirst();
            Account debitAccount = matchingObject.get();
            accountService.transfer(debitAccount, creditAccount, transaction.getAmount());
            return new Gson().toJson(new Response(StatusResponse.SUCCESS));
        });
    }
}
