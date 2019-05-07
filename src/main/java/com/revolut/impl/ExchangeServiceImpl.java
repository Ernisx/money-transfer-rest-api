package com.revolut.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.revolut.api.ExchangeService;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExchangeServiceImpl implements ExchangeService {

    private static final String EXCHANGE_RATE_URL = "https://api.exchangerate-api.com/v4/latest/";
    private static final String RATES = "rates";

    public BigDecimal getExchangeRate(String debitCurrency, String creditCurrency) {

        try{
            URL url = new URL(EXCHANGE_RATE_URL + debitCurrency);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
            JsonObject jsonobj = root.getAsJsonObject();

            JsonElement rates = jsonobj.get(RATES);
            return new BigDecimal(rates.getAsJsonObject().get(creditCurrency).toString());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
