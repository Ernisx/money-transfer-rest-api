package com.revolut;

import com.revolut.impl.AccountServiceImpl;

public class App {

    public static void main(String[] args) {
        new AccountController(new AccountServiceImpl());
    }

}
