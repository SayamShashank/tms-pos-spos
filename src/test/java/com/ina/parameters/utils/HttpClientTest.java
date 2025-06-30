package com.ina.parameters.utils;

import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class HttpClientTest {

    @Mock
    private OkHttpClient mockOkHttpClient;

    @Mock
    private Call mockCall;

    @Mock
    private Response mockResponse;

    @Mock
    private ResponseBody mockResponseBody;

    @InjectMocks
    private  HttpClient httpClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        httpClient = new HttpClient(mockOkHttpClient);
    }

    @Test
    void testExchange_successfulResponse() throws Exception {
        String expectedResponse = "<response>success</response>";

        // Mock response
        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.code()).thenReturn(200);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.string()).thenReturn(expectedResponse);

        String result = httpClient.exchange("test-endpoint", "<request>data</request>");

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void testExchange_non200Response() throws Exception {
        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.code()).thenReturn(500);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockResponseBody.toString()).thenReturn("Internal Server Error");

        String result = httpClient.exchange("test-endpoint", "<request>data</request>");

        assertNull(result);
    }

    @Test
    void testExchange_throwsIOException() throws Exception {
        when(mockOkHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Network error"));

        String result = httpClient.exchange("test-endpoint", "<request>data</request>");

        assertNull(result);
    }
}
