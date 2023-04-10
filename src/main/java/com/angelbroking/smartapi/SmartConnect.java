package com.angelbroking.smartapi;

import com.angelbroking.smartapi.http.SessionExpiryHook;
import com.angelbroking.smartapi.http.SmartAPIRequestHandler;
import com.angelbroking.smartapi.http.exceptions.CustomException;
import com.angelbroking.smartapi.http.exceptions.SmartAPIException;
import com.angelbroking.smartapi.models.*;
import com.angelbroking.smartapi.utils.Constants;
import com.angelbroking.smartapi.utils.ResponseParser;
import com.angelbroking.smartapi.utils.Validators;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;

@Data
public class SmartConnect {

    private static final Logger logger = LoggerFactory.getLogger(SmartConnect.class);
    public static SessionExpiryHook sessionExpiryHook = null;
    public static boolean enableLogging = false;
    private static final Routes routes = new Routes();
    private static final Proxy proxy = Proxy.NO_PROXY;
    private SmartAPIRequestHandler smartAPIRequestHandler;
    private String apiKey;
    private String orderID = "orderid";
    private String accessToken;
    private String refreshToken;
    private String userId;


    public SmartConnect(String apiKey) {
        this.apiKey = apiKey;
    }

    public SmartConnect(String apiKey, String accessToken, String refreshToken) {
        this.apiKey = apiKey;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }


    /**
     * Returns apiKey of the App.
     *
     * @return String apiKey is returned.
     * @throws CustomException if _apiKey is not found.
     */
    public String getApiKey() throws CustomException {
        if (apiKey != null) return apiKey;
        else throw new CustomException("The API key is missing.");
    }


    /**
     * Returns accessToken.
     *
     * @return String access_token is returned.
     * @throws CustomException if accessToken is null.
     */
    public String getAccessToken() throws CustomException {
        if (accessToken != null) return accessToken;
        else throw new CustomException("The Access Token key is missing.");
    }


    /**
     * Returns userId.
     *
     * @return String userId is returned.
     * @throws CustomException if userId is null.
     */
    public String getUserId() {
        return Optional.ofNullable(userId).orElseThrow(() -> new CustomException("The user ID is missing."));

    }


    /**
     * Returns publicToken.
     *
     * @return String public token is returned.
     * @throws CustomException if publicToken is null.
     */
    public String getPublicToken() throws CustomException {
        if (refreshToken != null) {
            return refreshToken;
        } else {
            throw new CustomException("The Public Token key is missing.");
        }
    }


    /**
     * Retrieves login url
     *
     * @return String loginUrl is returned.
     */
    public String getLoginURL() throws CustomException {
        String baseUrl = routes.getLoginUrl();
        if (baseUrl != null) {
            return baseUrl;
        } else {
            throw new CustomException("The Login URL key is missing.");
        }
    }

    /**
     * Do the token exchange with the `request_token` obtained after the login flow,
     * and retrieve the `access_token` required for all subsequent requests.
     *
     * @param clientCode received from login process.
     * @return User is the user model which contains user and session details.
     */
    public User generateSession(String clientCode, String password, String totp) {
        User user;
        JSONObject loginResultObject;
        try {
            smartAPIRequestHandler = new SmartAPIRequestHandler(proxy);

            loginResultObject = smartAPIRequestHandler.postRequest(this.apiKey, routes.getLoginUrl(), createLoginParams(clientCode, password, totp));

        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }

        if(logger.isDebugEnabled()) {
            logger.debug(String.valueOf(loginResultObject));
        }
        String jwtToken = loginResultObject.getJSONObject(Constants.SMART_CONNECT_DATA).getString(Constants.SMART_CONNECT_JWT_TOKEN);
        String refreshTokenLocal = loginResultObject.getJSONObject(Constants.SMART_CONNECT_DATA).getString(Constants.SMART_CONNECT_REFRESH_TOKEN);
        String feedToken = loginResultObject.getJSONObject(Constants.SMART_CONNECT_DATA).getString(Constants.SMART_CONNECT_FEED_TOKEN);
        String url = routes.get(Constants.SMART_CONNECT_API_USER_PROFILE);
        try {
            user = ResponseParser.parseResponse(smartAPIRequestHandler.getRequest(this.apiKey, url, jwtToken));
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
        user.setAccessToken(jwtToken);
        user.setRefreshToken(refreshTokenLocal);
        user.setFeedToken(feedToken);

        return user;

    }

    private JSONObject createLoginParams(String clientCode, String password, String totp) {
        // Create JSON params object needed to be sent to api.

        JSONObject params = new JSONObject();
        params.put(Constants.SMART_CONNECT_CLIENT_CODE, clientCode);
        params.put(Constants.SMART_CONNECT_PASSWORD, password);
        params.put(Constants.SMART_CONNECT_TOTP, totp);
        return params;
    }

    /**
     * Hex encodes sha256 output for android support.
     *
     * @param str is the String that has to be encrypted.
     * @return Hex encoded String.
     */
    public String sha256Hex(String str) {
        byte[] a = DigestUtils.sha256(str);
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /**
     * Get the profile details of the use.
     *
     * @return Profile is a POJO which contains profile related data.
     */
    public User getProfile() {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_USER_PROFILE);
            return ResponseParser.parseResponse(smartAPIRequestHandler.getRequest(this.apiKey, url, accessToken));

        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
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
    public Order placeOrder(OrderParams orderParams, String variety) {


        try {

            Validators validator = new Validators();
            validator.orderValidator(orderParams);

            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_PLACE);

            JSONObject params = new JSONObject();
            params.put(Constants.EXCHANGE, orderParams.exchange);
            params.put(Constants.TRADING_SYMBOL, orderParams.tradingSymbol);
            params.put(Constants.TRANSACTION_TYPE, orderParams.transactionType);
            params.put(Constants.QUANTITY, orderParams.quantity);
            params.put(Constants.PRICE, orderParams.price);
            params.put(Constants.PRODUCT_TYPE, orderParams.productType);
            params.put(Constants.ORDER_TYPE, orderParams.orderType);
            params.put(Constants.DURATION, orderParams.duration);
            params.put(Constants.SYMBOL_TOKEN, orderParams.symbolToken);
            params.put(Constants.SQUARE_OFF, orderParams.squareOff);
            params.put(Constants.STOP_LOSS, orderParams.stopLoss);
            params.put(Constants.TRIGGER_PRICE, orderParams.triggerPrice);
            params.put(Constants.VARIETY, variety);

            JSONObject jsonObject = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(jsonObject));
            }

            Order order = new Order();
            order.orderId = jsonObject.getJSONObject(Constants.SMART_CONNECT_DATA).getString(orderID);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(order));
            }
            return order;
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
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
    public Order modifyOrder(String orderId, OrderParams orderParams, String variety) {
        try {
            Validators validator = new Validators();
            validator.modifyOrderValidator(orderParams);

            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_MODIFY);

            JSONObject params = new JSONObject();


            params.put(Constants.EXCHANGE, orderParams.exchange);
            params.put(Constants.TRADING_SYMBOL, orderParams.tradingSymbol);
            params.put(Constants.SYMBOL_TOKEN, orderParams.symbolToken);
            params.put(Constants.QUANTITY, orderParams.quantity);
            params.put(Constants.PRICE, orderParams.price);
            params.put(Constants.PRODUCT_TYPE, orderParams.productType);
            params.put(Constants.ORDER_TYPE, orderParams.orderType);
            params.put(Constants.DURATION, orderParams.duration);
            params.put(Constants.VARIETY, variety);
            params.put(Constants.ORDER_ID, orderId);


            JSONObject jsonObject = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(jsonObject));
            }
            Order order = new Order();
            order.orderId = jsonObject.getJSONObject(Constants.SMART_CONNECT_DATA).getString(orderID);
            return order;
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
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
    public Order cancelOrder(String orderId, String variety) {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_CANCEL);
            JSONObject params = new JSONObject();
            params.put(Constants.VARIETY, variety);
            params.put(Constants.ORDER_ID, orderId);

            JSONObject jsonObject = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(jsonObject));
            }

            Order order = new Order();
            order.orderId = jsonObject.getJSONObject(Constants.SMART_CONNECT_DATA).getString(orderID);
            return order;
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Returns list of different stages an order has gone through.
     *
     * @return List of multiple stages an order has gone through in the system.
     * @throws SmartAPIException is thrown for all Smart API trade related errors.
     */
    @SuppressWarnings({})
    public JSONObject getOrderHistory() {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_BOOK);
            JSONObject response = smartAPIRequestHandler.getRequest(this.apiKey, url, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }
            return response;
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Retrieves last price. User can either pass exchange with tradingsymbol or
     * instrument token only. For example {NSE:NIFTY 50, BSE:SENSEX} or {256265,
     * 265}.
     *
     * @return Map of String and LTPQuote.
     */
    public JSONObject getLTP(String exchange, String tradingSymbol, String symboltoken) {
        try {
            JSONObject params = new JSONObject();
            params.put(Constants.EXCHANGE, exchange);
            params.put(Constants.TRADING_SYMBOL, tradingSymbol);
            params.put(Constants.SYMBOL_TOKEN, symboltoken);


            String url = routes.get(Constants.SMART_CONNECT_API_LTP_DATA);
            JSONObject response = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }
            return response.getJSONObject(Constants.SMART_CONNECT_DATA);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Retrieves list of trades executed.
     *
     * @return List of trades.
     */
    public JSONObject getTrades() {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_TRADE_BOOK);
            JSONObject response = smartAPIRequestHandler.getRequest(this.apiKey, url, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }
            return response;
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
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
    public JSONObject getRMS() {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_RMS_DATA);
            JSONObject response = smartAPIRequestHandler.getRequest(this.apiKey, url, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }
            return response.getJSONObject(Constants.SMART_CONNECT_DATA);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Retrieves Holding.
     *
     * @return Object of Holding.
     */
    public JSONObject getHolding() {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_RMS_HOLDING);
            JSONObject response = smartAPIRequestHandler.getRequest(this.apiKey, url, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }
            return response;
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Retrieves position.
     *
     * @return Object of position.
     */
    public JSONObject getPosition() {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_RMS_POSITION);
            JSONObject response = smartAPIRequestHandler.getRequest(this.apiKey, url, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }
            return response;
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
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
    public JSONObject convertPosition(JSONObject params) {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_ORDER_RMS_POSITION_CONVERT);
            return smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Create a Gtt Rule.
     *
     * @param gttParams is gtt Params.
     * @return Gtt contains only orderId.
     */

    public String gttCreateRule(GttParams gttParams) {
        try {

            Validators validator = new Validators();

            validator.gttParamsValidator(gttParams);

            String url = routes.get(Constants.SMART_CONNECT_API_GTT_CREATE);

            JSONObject params = new JSONObject();
            params.put(Constants.TRADING_SYMBOL, gttParams.tradingSymbol);
            params.put(Constants.SYMBOL_TOKEN, gttParams.symbolToken);
            params.put(Constants.EXCHANGE, gttParams.exchange);
            params.put(Constants.TRANSACTION_TYPE, gttParams.transactionType);
            params.put(Constants.PRODUCT_TYPE, gttParams.productType);
            params.put(Constants.PRICE, gttParams.price);
            params.put(Constants.QTY, gttParams.qty);
            params.put(Constants.TRIGGER_PRICE, gttParams.triggerPrice);
            params.put(Constants.DISCLOSED_QTY, gttParams.disclosedQty);
            params.put(Constants.TIME_PERIOD, gttParams.timePeriod);

            JSONObject jsonObject = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            int gttId = jsonObject.getJSONObject(Constants.SMART_CONNECT_DATA).getInt("id");
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(gttId));
            }
            return String.valueOf(gttId);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }

    }

    /**
     * Modify a Gtt Rule.
     *
     * @param gttParams is gtt Params.
     * @return Gtt contains only orderId.
     */

    public String gttModifyRule(Integer id, GttParams gttParams) {
        try {
            Validators validator = new Validators();
            validator.gttModifyRuleValidator(gttParams);


            String url = routes.get(Constants.SMART_CONNECT_API_GTT_MODIFY);

            JSONObject params = new JSONObject();
            params.put(Constants.SYMBOL_TOKEN, gttParams.symbolToken);
            params.put(Constants.EXCHANGE, gttParams.exchange);
            params.put(Constants.PRICE, gttParams.price);
            params.put(Constants.QTY, gttParams.qty);
            params.put(Constants.TRIGGER_PRICE, gttParams.triggerPrice);
            params.put(Constants.DISCLOSED_QTY, gttParams.disclosedQty);
            params.put(Constants.TIME_PERIOD, gttParams.timePeriod);
            params.put(Constants.ID, id);

            JSONObject jsonObject = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);

            int gttId = jsonObject.getJSONObject(Constants.SMART_CONNECT_DATA).getInt("id");
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(gttId));
            }
            return String.valueOf(gttId);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }


    }

    /**
     * Cancel a Gtt Rule.
     *
     * @param exchange,id,symboltoken is gtt Params.
     * @return Gtt contains only orderId.
     */

    public Gtt gttCancelRule(Integer id, String symboltoken, String exchange) {
        try {
            JSONObject params = new JSONObject();
            params.put(Constants.ID, id);
            params.put(Constants.SYMBOL_TOKEN, symboltoken);
            params.put(Constants.EXCHANGE, exchange);


            String url = routes.get(Constants.SMART_CONNECT_API_GTT_CANCEL);
            JSONObject jsonObject = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            Gtt gtt = new Gtt();
            gtt.id = jsonObject.getJSONObject(Constants.SMART_CONNECT_DATA).getInt("id");
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(gtt));
            }
            return gtt;
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Get Gtt Rule Details.
     *
     * @param id is gtt rule id.
     * @return returns the details of gtt rule.
     */

    public JSONObject gttRuleDetails(Integer id) {
        try {

            JSONObject params = new JSONObject();
            params.put("id", id);

            String url = routes.get(Constants.SMART_CONNECT_API_GTT_DETAILS);
            JSONObject response = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }

            return response.getJSONObject(Constants.SMART_CONNECT_DATA);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
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
    public JSONArray gttRuleList(List<String> status, Integer page, Integer count) {
        try {
            JSONObject params = new JSONObject();
            params.put(Constants.STATUS, status);
            params.put(Constants.PAGE, page);
            params.put(Constants.COUNT, count);

            String url = routes.get(Constants.SMART_CONNECT_API_GTT_LIST);
            JSONObject response = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }
            return response.getJSONArray(Constants.SMART_CONNECT_DATA);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }

    }

    /**
     * Get Historic Data.
     *
     * @param params is historic data params.
     * @return returns the details of historic data.
     */
    public String candleData(JSONObject params) {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_CANDLE_DATA);
            JSONObject response = smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
            if(logger.isDebugEnabled()) {
                logger.debug(String.valueOf(response));
            }
            return response.getString(Constants.SMART_CONNECT_DATA);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Logs out user by invalidating the access token.
     *
     * @return JSONObject which contains status
     */

    public JSONObject logout() {
        try {
            String url = routes.get(Constants.SMART_CONNECT_API_USER_LOGOUT);
            JSONObject params = new JSONObject();
            params.put(Constants.SMART_CONNECT_CLIENT_CODE, this.userId);
            return smartAPIRequestHandler.postRequest(this.apiKey, url, params, accessToken);
        } catch (SmartAPIException ex) {
            logger.error(Constants.SMART_API_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (IOException ex) {
            logger.error(Constants.IO_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        } catch (JSONException ex) {
            logger.error(Constants.JSON_EXCEPTION_OCCURRED + ex.getMessage(), ex);
            return null;
        }
    }

}
