package com.angelbroking.smartapi.http;

import com.angelbroking.smartapi.http.exceptions.SmartAPIException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class SmartAPIRequestHandlerTest {

    private static MockWebServer server;

    private SmartAPIRequestHandler requestHandler;

    @BeforeAll
    public static void setUp() throws IOException {
        // Start a mock web server
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        // Shutdown the mock web server
        server.shutdown();
    }

    @BeforeEach
    void init() {
        requestHandler = new SmartAPIRequestHandler(Proxy.NO_PROXY);
    }

    @Test
    void testGetRequest() throws IOException, SmartAPIException, JSONException, InterruptedException {
        // Set up the mock web server response
        JSONObject response = new JSONObject();
        response.put("status", "true");
        response.put("errorcode", "");
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .addHeader("X-API-KEY", "test-api-key")
                .addHeader("Authorization", "Bearer test-access-token")
                .setBody(response.toString());
        server.enqueue(mockResponse);

        // Send a GET request to the server
        String apiKey = "test-api-key";
        String url = server.url("/test-url").toString();
        String accessToken = "test-access-token";
        JSONObject responseJson = requestHandler.getRequest(apiKey, url, accessToken);
        // Verify the response
        RecordedRequest recordedRequest = server.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/test-url", recordedRequest.getPath());
    }
    @Test
    void testApiHeaders() throws JSONException {
        JSONObject headers = requestHandler.apiHeaders();
        assertNotNull(headers);
        assertEquals("USER",headers.getString("userType"));
        assertEquals("WEB", headers.getString("sourceID"));
        assertNotNull(headers.getString("clientLocalIP") );
        assertNotNull(headers.getString("clientPublicIP"));
        assertNotNull(headers.getString("macAddress"));
        assertEquals("application/json",headers.getString("accept"));
    }

    @Test
    void testPostRequest() throws JSONException, SmartAPIException, InterruptedException, IOException {
        // set the response for the server
        JSONObject response = new JSONObject();
        response.put("status", true);
        response.put("errorcode", "");
        MockResponse mockResponse = new MockResponse()
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(response.toString());
        server.enqueue(mockResponse);

        // send a POST request to the server
        String apiKey = "test-api-key";
        String url = server.url("/test-url").toString();
        JSONObject params = new JSONObject().put("param1", "value1");
        JSONObject responseJson = requestHandler.postRequest(apiKey, url, params);

        // verify the response
        assertEquals(response.toString(), responseJson.toString());
        assertNotNull(responseJson);

        // verify the request
        RecordedRequest request  = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/test-url", request.getPath());
        assertEquals("application/json; charset=utf-8", request.getHeader("Content-Type"));
        assertEquals(params.toString(), request.getBody().readUtf8());
    }
}