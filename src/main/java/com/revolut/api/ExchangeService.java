package com.revolut.api;

import java.math.BigDecimal;

public interface ExchangeService {

    BigDecimal getExchangeRate(String sourceCurrency, String targetCurrency);
}
