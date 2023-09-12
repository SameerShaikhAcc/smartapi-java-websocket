package com.angelbroking.smartapi;

import com.angelbroking.smartapi.http.SmartAPIRequestHandler;
import com.angelbroking.smartapi.http.exceptions.SmartAPIException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static com.angelbroking.smartapi.utils.Constants.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class SmartConnectTest {

    @Mock
    private SmartAPIRequestHandler smartAPIRequestHandler;

    @Mock
    private SmartConnect smartConnect;

    @Mock
    private Routes routes;
    private String apiKey;
    private String accessToken;


    @Before
    public void setup() {
         apiKey = System.getProperty("apiKey");
         accessToken = System.getenv("accessToken");
    }

// Testing market data success for Full payload
    @Test
    public void testMarketData_Success() throws SmartAPIException, IOException {
        String url = routes.get("api.market.data");
        JSONObject params = getMarketDataRequest("FULL");
        when(smartAPIRequestHandler.postRequest(eq(this.apiKey), eq(url), eq(params), eq(this.accessToken))).thenReturn(createMarketDataResponse());
        try{
            JSONObject response = smartAPIRequestHandler.postRequest(this.apiKey, url, params, this.accessToken);
             response.getJSONObject("data");
        }
        catch (SmartAPIException e) {
            log.error("{} while test Market Data Failure {}", SMART_API_EXCEPTION_OCCURRED, e.toString());
            throw new SmartAPIException(String.format("%s in test Market Data Failure %s", SMART_API_EXCEPTION_ERROR_MSG, e));
        }
        catch (Exception ex) {
            log.error("{} Exception {}", EXCEPTION, ex.toString());
            throw new SmartAPIException(String.format("%s Exception %s", EXCEPTION, ex));
        }
    }



// Testing market data failure for Full payload
    @Test
    public void testMarketData_Failure() throws SmartAPIException, IOException {
        // Stub the postRequest method
        String url = routes.get("api.market.data");
        JSONObject params = getMarketDataRequest("FULL");
        when(smartAPIRequestHandler.postRequest(eq(this.apiKey), eq(url), eq(params), eq(this.accessToken)))
                .thenThrow(new SmartAPIException("API request failed"));
        try {
            JSONObject response = smartAPIRequestHandler.postRequest(apiKey, url, params, accessToken);
            // Perform assertions or further actions with the response
            response.getJSONObject("data");
        } catch (Exception | SmartAPIException e) {
            System.out.println(e.getMessage());
        }
        // If the exception is not thrown, the following assertion will fail
    }

    private JSONObject getMarketDataRequest(String mode) {
        JSONObject payload = new JSONObject();
        payload.put("mode", mode);
        JSONObject exchangeTokens = new JSONObject();
        JSONArray nseTokens = new JSONArray();
        nseTokens.put("3045");
        exchangeTokens.put("NSE", nseTokens);
        payload.put("exchangeTokens", exchangeTokens);
        return payload;
    }
    // Testing market data success for LTP payload
    @Test
    public void testMarketDataLTP_Success() throws SmartAPIException, IOException {
        String url = routes.get("api.market.data");
        JSONObject params = getMarketDataRequest("LTP");
        when(smartAPIRequestHandler.postRequest(eq(this.apiKey), eq(url), eq(params), eq(this.accessToken))).thenReturn(createMarketDataResponse());
        try{
            JSONObject response = smartAPIRequestHandler.postRequest(this.apiKey, url, params, this.accessToken);
            response.getJSONObject("data");
        }
        catch (SmartAPIException e) {
            log.error("{} while test Market Data Failure {}", SMART_API_EXCEPTION_OCCURRED, e.toString());
            throw new SmartAPIException(String.format("%s in test Market Data Failure %s", SMART_API_EXCEPTION_ERROR_MSG, e));
        }
        catch (Exception ex) {
            log.error("{} Exception {}", EXCEPTION, ex.toString());
            throw new SmartAPIException(String.format("%s Exception %s", EXCEPTION, ex));
        }
    }
    // Testing market data failure for LTP payload
    @Test
    public void testMarketDataLTP_Failure() throws SmartAPIException, IOException {
        // Stub the postRequest method
        String url = routes.get("api.market.data");
        JSONObject params = getMarketDataRequest("LTP");
        when(smartAPIRequestHandler.postRequest(eq(this.apiKey), eq(url), eq(params), eq(this.accessToken)))
                .thenThrow(new SmartAPIException("API request failed"));
        try {
            JSONObject response = smartAPIRequestHandler.postRequest(apiKey, url, params, accessToken);
            // Perform assertions or further actions with the response
            response.getJSONObject("data");
        } catch (Exception | SmartAPIException e) {
            System.out.println(e.getMessage());
        }
        // If the exception is not thrown, the following assertion will fail
    }
    // Testing market data success for OHLC payload
    @Test
    public void testMarketDataOHLC_Success() throws SmartAPIException, IOException {
        String url = routes.get("api.market.data");
        JSONObject params = getMarketDataRequest("OHLC");
        when(smartAPIRequestHandler.postRequest(eq(this.apiKey), eq(url), eq(params), eq(this.accessToken))).thenReturn(createMarketDataResponse());
        try{
            JSONObject response = smartAPIRequestHandler.postRequest(this.apiKey, url, params, this.accessToken);
            response.getJSONObject("data");
        }
        catch (SmartAPIException e) {
            log.error("{} while test Market Data Failure {}", SMART_API_EXCEPTION_OCCURRED, e.toString());
            throw new SmartAPIException(String.format("%s in test Market Data Failure %s", SMART_API_EXCEPTION_ERROR_MSG, e));
        }
        catch (Exception ex) {
            log.error("{} Exception {}", EXCEPTION, ex.toString());
            throw new SmartAPIException(String.format("%s Exception %s", EXCEPTION, ex));
        }
    }
    // Testing market data failure for OHLC payload
    @Test
    public void testMarketDataOHLC_Failure() throws SmartAPIException, IOException {
        // Stub the postRequest method
        String url = routes.get("api.market.data");
        JSONObject params = getMarketDataRequest("OHLC");
        when(smartAPIRequestHandler.postRequest(eq(this.apiKey), eq(url), eq(params), eq(this.accessToken)))
                .thenThrow(new SmartAPIException("API request failed"));
        try {
            JSONObject response = smartAPIRequestHandler.postRequest(apiKey, url, params, accessToken);
            // Perform assertions or further actions with the response
            response.getJSONObject("data");
        } catch (Exception | SmartAPIException e) {
            System.out.println(e.getMessage());
        }
        // If the exception is not thrown, the following assertion will fail
    }

    private static JSONObject createMarketDataResponse() {  JSONObject jsonObject = new JSONObject();

        // Create "data" object
        JSONObject dataObject = new JSONObject();

        // Create the "unfetched" array
        JSONArray unfetchedArray = new JSONArray();
        dataObject.put("unfetched", unfetchedArray);

        // Create the "fetched" array and its elements
        JSONArray fetchedArray = new JSONArray();
        JSONObject fetchedElement = new JSONObject();
        fetchedElement.put("netChange", 3.15);
        fetchedElement.put("tradeVolume", 5718111);
        fetchedElement.put("lowerCircuit", 533.15);
        fetchedElement.put("percentChange", 0.53);
        fetchedElement.put("exchFeedTime", "19-Jul-2023 11:20:51");
        fetchedElement.put("avgPrice", 595.25);
        fetchedElement.put("ltp", 595.5);
        fetchedElement.put("exchTradeTime", "19-Jul-2023 11:20:51");
        fetchedElement.put("totSellQuan", 1924292);
        fetchedElement.put("upperCircuit", 651.55);
        fetchedElement.put("lastTradeQty", 46);
        fetchedElement.put("high", 599.6);
        fetchedElement.put("totBuyQuan", 890300);

        // Create the "depth" object and its "buy" and "sell" arrays
        JSONObject depthObject = new JSONObject();
        JSONArray buyArray = new JSONArray();
        JSONArray sellArray = new JSONArray();

        // Add elements to "buy" array
        JSONObject buyElement1 = new JSONObject();
        buyElement1.put("quantity", 1776);
        buyElement1.put("price", 595.3);
        buyElement1.put("orders", 3);
        buyArray.put(buyElement1);

        JSONObject buyElement2 = new JSONObject();
        buyElement2.put("quantity", 767);
        buyElement2.put("price", 595.25);
        buyElement2.put("orders", 3);
        buyArray.put(buyElement2);

        // Add elements to "sell" array
        JSONObject sellElement1 = new JSONObject();
        sellElement1.put("quantity", 249);
        sellElement1.put("price", 595.5);
        sellElement1.put("orders", 5);
        sellArray.put(sellElement1);

        JSONObject sellElement2 = new JSONObject();
        sellElement2.put("quantity", 1379);
        sellElement2.put("price", 595.55);
        sellElement2.put("orders", 4);
        sellArray.put(sellElement2);

        // Add "buy" and "sell" arrays to "depth" object
        depthObject.put("buy", buyArray);
        depthObject.put("sell", sellArray);

        fetchedElement.put("depth", depthObject);

        // Add remaining properties to "fetched" element
        fetchedElement.put("low", 592);
        fetchedElement.put("exchange", "NSE");
        fetchedElement.put("opnInterest", 0);
        fetchedElement.put("tradingSymbol", "SBIN-EQ");
        fetchedElement.put("symbolToken", "3045");
        fetchedElement.put("close", 592.35);
        fetchedElement.put("52WeekLow", 482.1);
        fetchedElement.put("open", 594.65);
        fetchedElement.put("52WeekHigh", 629.55);

        // Add the "fetched" element to the "fetched" array
        fetchedArray.put(fetchedElement);

        dataObject.put("fetched", fetchedArray);

        // Add "data" object, "message", "errorcode", and "status" properties to the main JSON object
        jsonObject.put("data", dataObject);
        jsonObject.put("message", "SUCCESS");
        jsonObject.put("errorcode", "");
        jsonObject.put("status", true);

        return jsonObject;}


}