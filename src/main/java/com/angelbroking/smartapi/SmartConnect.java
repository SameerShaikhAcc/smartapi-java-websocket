package com.angelbroking.smartapi;

import com.angelbroking.smartapi.dto.StockHistoryRequestDTO;
import com.angelbroking.smartapi.dto.TradeRequestDTO;
import com.angelbroking.smartapi.http.SmartAPIRequestHandler;
import com.angelbroking.smartapi.http.exceptions.SmartAPIException;
import com.angelbroking.smartapi.http.response.HttpResponse;
import com.angelbroking.smartapi.http.response.UserResponseDTO;
import com.angelbroking.smartapi.models.GttParams;
import com.angelbroking.smartapi.models.GttRuleParams;
import com.angelbroking.smartapi.models.OrderParams;
import com.angelbroking.smartapi.models.SmartConnectParams;
import com.angelbroking.smartapi.models.User;
import com.angelbroking.smartapi.routes.Routes;
import com.angelbroking.smartapi.smartstream.models.LTPParams;
import com.angelbroking.smartapi.utils.ResponseParser;
import com.angelbroking.smartapi.utils.Utils;
import com.angelbroking.smartapi.utils.Validators;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;

import static com.angelbroking.smartapi.utils.Constants.IO_EXCEPTION_ERROR_MSG;
import static com.angelbroking.smartapi.utils.Constants.IO_EXCEPTION_OCCURRED;
import static com.angelbroking.smartapi.utils.Constants.JSON_EXCEPTION_ERROR_MSG;
import static com.angelbroking.smartapi.utils.Constants.JSON_EXCEPTION_OCCURRED;
import static com.angelbroking.smartapi.utils.Constants.SMART_API_EXCEPTION_ERROR_MSG;
import static com.angelbroking.smartapi.utils.Constants.SMART_API_EXCEPTION_OCCURRED;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_CANDLE_DATA;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_GTT_CANCEL;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_GTT_CREATE;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_GTT_DETAILS;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_GTT_LIST;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_GTT_MODIFY;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_LTP_DATA;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_BOOK;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_CANCEL;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_MODIFY;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_PLACE;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_RMS_DATA;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_RMS_HOLDING;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_RMS_POSITION;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_RMS_POSITION_CONVERT;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_ORDER_TRADE_BOOK;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_USER_LOGOUT;
import static com.angelbroking.smartapi.utils.Constants.SMART_CONNECT_API_USER_PROFILE;


@Slf4j
public class SmartConnect {

    private static final Routes routes = new Routes();
    private SmartAPIRequestHandler smartAPIRequestHandler;
    private SmartConnectParams smartConnectParams;


    public SmartConnect(String apiKey,Proxy proxy,long timeOutInMillis) {
        this.smartConnectParams = new SmartConnectParams(apiKey);
        this.smartAPIRequestHandler = new SmartAPIRequestHandler(proxy, timeOutInMillis);

    }

    public SmartConnect(String apiKey, String accessToken, String refreshToken,Proxy proxy,long timeOutInMillis) {
        this.smartConnectParams = new SmartConnectParams(apiKey, accessToken, refreshToken);
        this.smartAPIRequestHandler = new SmartAPIRequestHandler(proxy, timeOutInMillis);
    }


    /**
     * Generates a session for the given client with the provided credentials and TOTP.
     *
     * @param clientCode the client code for which the session needs to be generated
     * @param password   the password of the client account
     * @param totp       the TOTP generated by the client's TOTP device
     * @return a User object representing the client's session, or null if an error occurs
     * @throws SmartAPIException if an error occurs while making the API request
     * @throws IOException       if an I/O error occurs while making the API request
     * @throws JSONException     if there is an error while parsing the JSON response
     */
    public User generateSession(String clientCode, String password, String totp) throws SmartAPIException, IOException {
        User user;
        HttpResponse httpResponse;
        try {
            httpResponse = smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), routes.getLoginUrl(), Utils.createLoginParams(clientCode, password, totp));
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
        UserResponseDTO responseDTO = new ObjectMapper().readValue(httpResponse.getBody(), UserResponseDTO.class);
        String url = routes.get(SMART_CONNECT_API_USER_PROFILE);
        try {
            user = ResponseParser.parseResponse(smartAPIRequestHandler.getRequest(smartConnectParams.getApiKey(), url,responseDTO.getData().getJwtToken()));
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
        user.setAccessToken(responseDTO.getData().getJwtToken());
        user.setRefreshToken(responseDTO.getData().getRefreshToken());
        user.setFeedToken(responseDTO.getData().getFeedToken());
        return user;

    }


    /**
     * Get the profile details of the use.
     *
     * @return Profile is a POJO which contains profile related data.
     */
    public User getProfile() throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_USER_PROFILE);
            return ResponseParser.parseResponse(smartAPIRequestHandler.getRequest(smartConnectParams.getApiKey(), url, smartConnectParams.getAccessToken()));

        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Places an order.
     *
     * @param orderParams is Order params.
     * @param variety     variety="regular". Order variety can be bo, co, amo,
     *                    regular.
     * @return Order contains only orderId.
     */
    public HttpResponse placeOrder(OrderParams orderParams, String variety) throws SmartAPIException, IOException {
        try {
            Validators validator = new Validators();
            orderParams.setVariety(variety);
            validator.orderValidator(orderParams);
            String url = routes.get(SMART_CONNECT_API_ORDER_PLACE);
            return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(orderParams), smartConnectParams.getAccessToken());
            } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Modifies an open order.
     *
     * @param orderParams is Order params.
     * @param variety     variety="regular". Order variety can be bo, co, amo,
     *                    regular.
     * @param orderId     order id of the order being modified.
     * @return Order object contains only orderId.
     */
    public HttpResponse modifyOrder(String orderId, OrderParams orderParams, String variety) throws SmartAPIException, IOException {
        try {
            orderParams.setVariety(variety);
            orderParams.setOrderId(orderId);
            Validators validator = new Validators();
            validator.modifyOrderValidator(orderParams);
            String url = routes.get(SMART_CONNECT_API_ORDER_MODIFY);
            return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(orderParams), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Cancels an order.
     *
     * @param orderId order id of the order to be cancelled.
     * @param variety [variety="regular"]. Order variety can be bo, co, amo,
     *                regular.
     * @return Order object contains only orderId.
     */
    public HttpResponse cancelOrder(String orderId, String variety) throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_ORDER_CANCEL);
            OrderParams orderParams = new OrderParams();
            orderParams.setVariety(variety);
            orderParams.setOrderId(orderId);
            return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(orderParams), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Returns list of different stages an order has gone through.
     *
     * @return List of multiple stages an order has gone through in the system.
     * @throws SmartAPIException is thrown for all Smart API trade related errors.
     */
    @SuppressWarnings({})
    public HttpResponse getOrderHistory() throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_ORDER_BOOK);
            return smartAPIRequestHandler.getRequest(smartConnectParams.getApiKey(), url, smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Retrieves last price. User can either pass exchange with tradingsymbol or
     * instrument token only. For example {NSE:NIFTY 50, BSE:SENSEX} or {256265,
     * 265}.
     *
     * @return Map of String and LTPQuote.
     */
    public HttpResponse getLTP(String exchange, String tradingSymbol, String symboltoken) throws SmartAPIException, IOException {
        try {
            LTPParams ltpParams = new LTPParams();
            ltpParams.setExchange(exchange);
            ltpParams.setTradingSymbol(tradingSymbol);
            ltpParams.setSymbolToken(symboltoken);
            String url = routes.get(SMART_CONNECT_API_LTP_DATA);
            return  smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(ltpParams), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Retrieves list of trades executed.
     *
     * @return List of trades.
     */
    public HttpResponse getTrades() throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_ORDER_TRADE_BOOK);
            return smartAPIRequestHandler.getRequest(smartConnectParams.getApiKey(), url, smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Retrieves RMS.
     *
     * @return Object of RMS.
     * @throws SmartAPIException is thrown for all Smart API trade related errors.
     * @throws JSONException     is thrown when there is exception while parsing
     *                           response.
     * @throws IOException       is thrown when there is connection error.
     */
    public HttpResponse getRMS() throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_ORDER_RMS_DATA);
            return  smartAPIRequestHandler.getRequest(smartConnectParams.getApiKey(), url, smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Retrieves Holding.
     *
     * @return Object of Holding.
     */
    public HttpResponse getHolding() throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_ORDER_RMS_HOLDING);
            return smartAPIRequestHandler.getRequest(smartConnectParams.getApiKey(), url, smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Retrieves position.
     *
     * @return Object of position.
     */
    public HttpResponse getPosition() throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_ORDER_RMS_POSITION);
            return smartAPIRequestHandler.getRequest(smartConnectParams.getApiKey(), url, smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Retrieves conversion.
     *
     * @return Object of conversion.
     * @throws SmartAPIException is thrown for all Smart API trade related errors.
     * @throws JSONException     is thrown when there is exception while parsing
     *                           response.
     * @throws IOException       is thrown when there is connection error.
     */
    public HttpResponse convertPosition(TradeRequestDTO params) throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_ORDER_RMS_POSITION_CONVERT);
            return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(params), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Create a Gtt Rule.
     *
     * @param gttParams is gtt Params.
     * @return Gtt contains only orderId.
     */

    public HttpResponse gttCreateRule(GttParams gttParams) throws SmartAPIException, IOException {
        try {
            Validators validator = new Validators();
            validator.gttParamsValidator(gttParams);
            String url = routes.get(SMART_CONNECT_API_GTT_CREATE);
           return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(gttParams), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }

    }

    /**
     * Modify a Gtt Rule.
     *
     * @param gttParams is gtt Params.
     * @return Gtt contains only orderId.
     */

    public HttpResponse gttModifyRule(Integer id, GttParams gttParams) throws SmartAPIException, IOException {
        try {
            Validators validator = new Validators();
            gttParams.setId(id);
            validator.gttModifyRuleValidator(gttParams);
            String url = routes.get(SMART_CONNECT_API_GTT_MODIFY);
           return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(gttParams), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }


    }

    /**
     * Cancel a Gtt Rule.
     *
     * @param exchange,id,symboltoken is gtt Params.
     * @return Gtt contains only orderId.
     */

    public HttpResponse gttCancelRule(Integer id, String symboltoken, String exchange) throws SmartAPIException, IOException {
        try {
            GttParams gttParams = new GttParams();
            gttParams.setId(id);
            gttParams.setSymbolToken(symboltoken);
            gttParams.setExchange(exchange);
            String url = routes.get(SMART_CONNECT_API_GTT_CANCEL);
           return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(gttParams), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Get Gtt Rule Details.
     *
     * @param id is gtt rule id.
     * @return returns the details of gtt rule.
     */

    public HttpResponse gttRuleDetails(Integer id) throws SmartAPIException, IOException {
        try {

            GttParams gttParams = new GttParams();
            gttParams.setId(id);

            String url = routes.get(SMART_CONNECT_API_GTT_DETAILS);
            return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url,new Gson().toJson(gttParams), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }

    }

    /**
     * Get Gtt Rule Details.
     *
     * @param status is list of gtt rule status.
     * @param page   is no of page
     * @param count  is the count of gtt rules
     * @return returns the detailed list of gtt rules.
     */
    public HttpResponse gttRuleList(List<String> status, Integer page, Integer count) throws SmartAPIException, IOException {
        try {
            GttRuleParams gttRuleParams = new GttRuleParams();
            gttRuleParams.setStatus(status);
            gttRuleParams.setPage(page);
            gttRuleParams.setCount(count);
            String url = routes.get(SMART_CONNECT_API_GTT_LIST);
            return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url,new Gson().toJson(gttRuleParams), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }

    }

    /**
     * Get Historic Data.
     *
     * @param params is historic data params.
     * @return returns the details of historic data.
     */
    public HttpResponse candleData(StockHistoryRequestDTO params) throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_CANDLE_DATA);
            return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url, new Gson().toJson(params), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }

    /**
     * Logs out user by invalidating the access token.
     *
     * @return HttpResponse which contains status
     */

    public HttpResponse logout() throws SmartAPIException, IOException {
        try {
            String url = routes.get(SMART_CONNECT_API_USER_LOGOUT);
            User user = new User();
            user.setUserId(smartConnectParams.getUserId());
            return smartAPIRequestHandler.postRequest(smartConnectParams.getApiKey(), url,new Gson().toJson(user), smartConnectParams.getAccessToken());
        } catch (SmartAPIException ex) {
            log.error("{} {}", SMART_API_EXCEPTION_OCCURRED, ex.getMessage());
            throw new SmartAPIException(String.format("%s: %s", SMART_API_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (IOException ex) {
            log.error("{} {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s: %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));

        } catch (JSONException ex) {
            log.error("{} {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
           throw new JSONException(String.format("%s: %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        }
    }


    public void setAccessToken(String accessToken) {
        smartConnectParams.setAccessToken(accessToken);
    }

    public void setUserId(String userId) {
        smartConnectParams.setUserId(userId);
    }

}
