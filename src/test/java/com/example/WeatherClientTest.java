package com.example;

import com.example.exceptions.WeatherClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class WeatherClientTest {

    private MockWebServer mockWebServer;
    private WeatherClientImpl weatherClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        weatherClient = new WeatherClientImpl("52ebbe9942d6dd65419112ba6709088d", mockWebServer.url("/").toString());

    }

    @AfterEach
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    public void testFetchWeather_Success() throws Exception {
        String weatherJson = "{\n" +
                "  \"weather\": [{\"main\": \"Clear\", \"description\": \"clear sky\"}],\n" +
                "  \"main\": {\"temp\": 20.5, \"feels_like\": 19.0},\n" +
                "  \"visibility\": 10000,\n" +
                "  \"wind\": {\"speed\": 5.1},\n" +
                "  \"dt\": 1618317045,\n" +
                "  \"sys\": {\"sunrise\": 1618301045, \"sunset\": 1618354200},\n" +
                "  \"timezone\": 10800,\n" +
                "  \"name\": \"London\"\n" +
                "}";

        WeatherData weatherData = objectMapper.readValue(weatherJson, WeatherData.class);
        WeatherClient spyClient = spy(weatherClient);
        doReturn(weatherData).when(spyClient).fetchWeather("London");
        WeatherData weatherData2 = spyClient.fetchWeather("London");

        assertNotNull(weatherData2);
        assertEquals(20.5, weatherData2.getMain().getTemp());
        assertEquals("clear sky", weatherData2.getWeather().get(0).getDescription());
    }

    @SneakyThrows
    @Test
    public void testFetchWeather_Failure() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(WeatherClientException.class, () -> weatherClient.fetchWeather("London"));
    }

    @Test
    public void testFetchWeather_NetworkError() throws Exception {
        mockWebServer.shutdown();

        assertThrows(WeatherClientException.class, () -> weatherClient.fetchWeather("London"));
    }
}
