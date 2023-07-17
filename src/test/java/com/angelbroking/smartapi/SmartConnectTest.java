package com.angelbroking.smartapi;

import com.angelbroking.smartapi.http.SmartAPIRequestHandler;
import com.angelbroking.smartapi.http.exceptions.SmartAPIException;
import com.angelbroking.smartapi.http.response.HttpResponse;
import com.angelbroking.smartapi.models.MarketDataDTO;
import com.angelbroking.smartapi.routes.Routes;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.angelbroking.smartapi.utils.Constants.IO_EXCEPTION_ERROR_MSG;
import static com.angelbroking.smartapi.utils.Constants.IO_EXCEPTION_OCCURRED;
import static com.angelbroking.smartapi.utils.Constants.JSON_EXCEPTION_ERROR_MSG;
import static com.angelbroking.smartapi.utils.Constants.JSON_EXCEPTION_OCCURRED;
import static com.angelbroking.smartapi.utils.Constants.SMART_API_EXCEPTION_ERROR_MSG;
import static com.angelbroking.smartapi.utils.Constants.SMART_API_EXCEPTION_OCCURRED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Slf4j
public class SmartConnectTest {

    @Mock
    private SmartAPIRequestHandler smartAPIRequestHandler;

    @Mock
    private SmartConnect smartConnect;

    @Mock
    private Routes routes;


    @Before
    public void setup() {
        // Set up any necessary configurations or dependencies
    }

    @Test
    public void testMarketData_Success() throws SmartAPIException, IOException {
        // Arrange
        MarketDataDTO params = new MarketDataDTO();
        HttpResponse expectedResponse = createMockHttpResponse();

        when(smartConnect.marketData(params)).thenReturn(expectedResponse); // Configure the mock to return the expected response
        // Act
        HttpResponse actualResponse = smartConnect.marketData(params);

        // Assert
        assertEquals(200, actualResponse.getStatusCode());
        assertEquals("application/json", actualResponse.getHeaders().get("content-type").get(0));

        // Assert response body
        assertNotNull(actualResponse.getBody());
    }

    private HttpResponse createMockHttpResponse() {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(200);

        Map<String, List<String>> headers = new HashMap<>();
        headers.put("access-control-allow-headers", Collections.singletonList("Content-type,X-Requested-With,Origin,accept,Authorization,X-SourceID,X-ClientLocalIP,X-ClientPublicIP,X-MACaddress,X-PrivateKey,X-UserType,Access-Control-Allow-Origin"));
        headers.put("access-control-allow-methods", Collections.singletonList("POST, GET, OPTIONS, DELETE"));
        headers.put("access-control-allow-origin", Collections.singletonList("*"));
        headers.put("access-control-max-age", Collections.singletonList("3600"));
        headers.put("content-length", Collections.singletonList("165"));
        headers.put("content-type", Collections.singletonList("application/json"));
        headers.put("date", Collections.singletonList("Thu, 13 Jul 2023 09:26:55 GMT"));
        headers.put("server", Collections.singletonList("nginx/1.14.1"));

        httpResponse.setHeaders(headers);
        httpResponse.setBody("{\"status\":true,\"message\":\"SUCCESS\",\"errorcode\":\"\",\"data\":{\"fetched\":[{\"exchange\":\"NSE\",\"tradingSymbol\":\"SBIN-EQ\",\"symbolToken\":\"3045\",\"ltp\":585.65}],\"unfetched\":[]}}");

        return httpResponse;
    }

    @Test(expected = IOException.class)
    public void testMarketData_IOException() throws SmartAPIException, IOException {
        // Arrange
        MarketDataDTO params = new MarketDataDTO();
        IOException expectedException = new IOException("Simulated IOException");
        when(smartConnect.marketData(params)).thenThrow(expectedException); // Configure the mock to throw an IOException
        // Act
        try {
            smartConnect.marketData(params);
        } catch (IOException ex) {
            log.error("{} while getting market Data  {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s in getting market data %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} while getting market Data  {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
            throw new JSONException(String.format("%s in getting market data %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (SmartAPIException ex) {
            log.error("{} while getting market Data  {}", SMART_API_EXCEPTION_OCCURRED, ex.toString());
            throw new SmartAPIException(String.format("%s in getting market data %s", SMART_API_EXCEPTION_ERROR_MSG, ex));

        }
    }

    @Test(expected = JSONException.class)
    public void testMarketData_JSONException() throws SmartAPIException, IOException {
        // Arrange
        MarketDataDTO params = new MarketDataDTO();
        JSONException expectedException = new JSONException("Simulated JSONException");

        when(smartConnect.marketData(params)).thenThrow(expectedException); // Configure the mock to throw a JSONException

        // Act
        try {
            smartConnect.marketData(params);
        } catch (IOException ex) {
            log.error("{} while getting market Data  {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s in getting market data %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} while getting market Data  {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
            throw new JSONException(String.format("%s in getting market data %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (SmartAPIException ex) {
            log.error("{} while getting market Data  {}", SMART_API_EXCEPTION_OCCURRED, ex.toString());
            throw new SmartAPIException(String.format("%s in getting market data %s", SMART_API_EXCEPTION_ERROR_MSG, ex));

        }
    }

    @Test(expected = SmartAPIException.class)
    public void testMarketData_SmartAPIException() throws SmartAPIException, IOException {
        // Arrange
        MarketDataDTO params = new MarketDataDTO();
        SmartAPIException expectedException = new SmartAPIException("Simulated SmartAPIException");

        when(smartConnect.marketData(params)).thenThrow(expectedException); // Configure the mock to throw a SmartAPIException

        // Act
        try {
            smartConnect.marketData(params);
        } catch (IOException ex) {
            log.error("{} while getting market Data  {}", IO_EXCEPTION_OCCURRED, ex.getMessage());
            throw new IOException(String.format("%s in getting market data %s", IO_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (JSONException ex) {
            log.error("{} while getting market Data  {}", JSON_EXCEPTION_OCCURRED, ex.getMessage());
            throw new JSONException(String.format("%s in getting market data %s", JSON_EXCEPTION_ERROR_MSG, ex.getMessage()));
        } catch (SmartAPIException ex) {
            log.error("{} while getting market Data  {}", SMART_API_EXCEPTION_OCCURRED, ex.toString());
            throw new SmartAPIException(String.format("%s in getting market data %s", SMART_API_EXCEPTION_ERROR_MSG, ex));

        }

    }

}