package com.angelbroking.smartapi.models;
import java.util.List;

public class ExchangeTokensDTO {
    private List<String> tokens;

    public List<String> getTokens() {
        return tokens;
    }

    public void setTokens(List<String> tokens) {
        this.tokens = tokens;
    }
}
