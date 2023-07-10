package com.angelbroking.smartapi.sample;

import com.angelbroking.smartapi.models.MarketDataDTO;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class LoginWithTOTPSample {

		public static void main(String[] args) {
			MarketDataDTO marketDataDTO = new MarketDataDTO();

			// Set the mode
			marketDataDTO.setMode("FULL");

			// Create a map for exchange tokens
			Map<String, List<String>> exchangeTokens = new HashMap<>();

			// Create a list of tokens for NSE exchange
			List<String> nseTokens = new ArrayList<>();
			nseTokens.add("3045");

			// Add the NSE tokens list to the exchangeTokens map
			exchangeTokens.put("NSE", nseTokens);

			// Set the exchangeTokens map
			marketDataDTO.setExchangeTokens(exchangeTokens);

			// Print the DTO object to verify the values
			System.out.println(marketDataDTO);
			// Print the DTO object
			System.out.println("marketDataDTO: "+marketDataDTO);
			System.out.println("json: "+new Gson().toJson(marketDataDTO));
	}

}
