package com.angelbroking.smartapi.models;

import java.util.List;
import java.util.Map;

public class MarketDataDTO {

    private String mode;
    private Map<String, List<String>> exchangeTokens;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Map<String, List<String>> getExchangeTokens() {
        return exchangeTokens;
    }

    public void setExchangeTokens(Map<String, List<String>> exchangeTokens) {
        this.exchangeTokens = exchangeTokens;
    }
}
