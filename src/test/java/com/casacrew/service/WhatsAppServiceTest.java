package com.casacrew.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WhatsAppServiceTest {

    private WhatsAppService service;
    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        service = new WhatsAppService();
        httpClient = mock(HttpClient.class);
        ReflectionTestUtils.setField(service, "http", httpClient);
    }

    private void configure() {
        ReflectionTestUtils.setField(service, "accountSid", "AC123");
        ReflectionTestUtils.setField(service, "authToken", "secret");
        ReflectionTestUtils.setField(service, "fromNumber", "+31600000000");
    }


    @Test
    void isConfigured_allValuesSet_returnsTrue() {
        configure();
        assertThat(service.isConfigured()).isTrue();
    }

    @Test
    void isConfigured_missingAccountSid_returnsFalse() {
        ReflectionTestUtils.setField(service, "accountSid", "");
        ReflectionTestUtils.setField(service, "authToken", "secret");
        ReflectionTestUtils.setField(service, "fromNumber", "+31600000000");

        assertThat(service.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_missingAuthToken_returnsFalse() {
        ReflectionTestUtils.setField(service, "accountSid", "AC123");
        ReflectionTestUtils.setField(service, "authToken", "");
        ReflectionTestUtils.setField(service, "fromNumber", "+31600000000");

        assertThat(service.isConfigured()).isFalse();
    }

    @Test
    void isConfigured_missingFromNumber_returnsFalse() {
        ReflectionTestUtils.setField(service, "accountSid", "AC123");
        ReflectionTestUtils.setField(service, "authToken", "secret");
        ReflectionTestUtils.setField(service, "fromNumber", "");

        assertThat(service.isConfigured()).isFalse();
    }


    @Test
    void send_notConfigured_doesNotCallHttpClient() throws IOException, InterruptedException {
        ReflectionTestUtils.setField(service, "accountSid", "");
        ReflectionTestUtils.setField(service, "authToken", "");
        ReflectionTestUtils.setField(service, "fromNumber", "");

        assertDoesNotThrow(() -> service.send("+31612345678", "Hallo"));
        verify(httpClient, never()).send(any(), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void send_configuredWithBlankToNumber_doesNotCallHttpClient() throws IOException, InterruptedException {
        configure();

        assertDoesNotThrow(() -> service.send("  ", "Hallo"));
        verify(httpClient, never()).send(any(), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void send_configuredAndSuccessResponse_sendsRequestWithWhatsappPrefixedNumbers() throws IOException, InterruptedException {
        configure();
        HttpResponse<String> response = mockResponse(201, "ok");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        service.send("+31612345678", "Hallo daar");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(httpClient).send(captor.capture(), any(HttpResponse.BodyHandler.class));
        assertThat(captor.getValue().uri().toString()).contains("AC123");
    }

    @Test
    void send_httpClientThrowsIOException_isCaughtAndDoesNotPropagate() throws IOException, InterruptedException {
        configure();
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("network down"));

        assertDoesNotThrow(() -> service.send("+31612345678", "Hallo"));
    }

    @Test
    void send_nonSuccessStatusCode_doesNotThrow() throws IOException, InterruptedException {
        configure();
        HttpResponse<String> response = mockResponse(500, "server error");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        assertDoesNotThrow(() -> service.send("+31612345678", "Hallo"));
    }


    @Test
    void sendToAll_nullList_doesNotThrow() {
        assertDoesNotThrow(() -> service.sendToAll(null, "Hallo"));
    }

    @Test
    void sendToAll_skipsBlankAndNullEntries_notConfiguredSoNoHttpCalls() throws IOException, InterruptedException {
        assertDoesNotThrow(() -> service.sendToAll(java.util.Arrays.asList(null, "", "  "), "Hallo"));
        verify(httpClient, never()).send(any(), any(HttpResponse.BodyHandler.class));
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(statusCode);
        when(response.body()).thenReturn(body);
        return response;
    }
}
