package com.example;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherSDKTest {

    private MockWebServer mockWebServer;
    private WeatherSDK sdk;

    @BeforeEach
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        sdk = WeatherSDK.addInstance("52ebbe9942d6dd65419112ba6709088d", Mode.POLLING,mockWebServer.url("/").toString());
    }

    @AfterEach
    public void tearDown() throws Exception{
        WeatherSDK.removeInstance("52ebbe9942d6dd65419112ba6709088d");
        mockWebServer.shutdown();
    }

    @Test
    void addInstance_shouldCreateNewSDKInstance() {
        WeatherSDK sdk = WeatherSDK.addInstance("test_api_key_1", Mode.ON_DEMAND);
        assertNotNull(sdk);
    }

    @Test
    void addInstance_shouldThrowException_whenApiKeyAlreadyExists() {
        WeatherSDK.addInstance("test_api_key_2", Mode.ON_DEMAND);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> WeatherSDK.addInstance("test_api_key_2", Mode.ON_DEMAND));

        assertTrue(exception.getMessage().contains("Ошибка: экземпляр с ключом '" + "test_api_key_2" + "' уже создан."));
    }

    @Test
    void removeInstance_shouldDeleteInstance() {
        WeatherSDK.addInstance("test_api_key_3", Mode.ON_DEMAND);
        WeatherSDK.removeInstance("test_api_key_3");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> WeatherSDK.removeInstance("test_api_key_3"));

        assertTrue(exception.getMessage().contains("Ошибка: экземпляр с ключом '" + "test_api_key_3" + "' не найден."));
    }

    @Test
    void getWeather_shouldReturnCachedData_whenNotExpired() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"name\":\"London\", \"dt\":1630425600}")
                .addHeader("Content-Type", "application/json"));

        WeatherData data = sdk.getWeather("London");

        assertNotNull(data);
        assertEquals("London", data.getName());

        WeatherData cachedData = sdk.getWeather("London");

        assertNotNull(cachedData);
        assertEquals("London", cachedData.getName());
    }

    @Test
    void getWeather_shouldFetchNewData_whenCacheExpired() throws InterruptedException {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"name\":\"Paris\", \"dt\":1630425600}")
                .addHeader("Content-Type", "application/json"));

        WeatherData oldData = sdk.getWeather("Paris");

        Thread.sleep(1000);

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"name\":\"Paris\", \"dt\":1630426600}")
                .addHeader("Content-Type", "application/json"));

        WeatherData newData = sdk.getWeather("Paris");

        assertNotNull(newData);
        assertEquals("Paris", newData.getName());
        assertEquals(oldData.getDt(), newData.getDt());
    }

    @Test
    void getWeatherAsJson_shouldReturnValidJson() {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"name\":\"Berlin\", \"dt\":1630425600}")
                .addHeader("Content-Type", "application/json"));

        String json = sdk.getWeatherAsJson("Berlin");

        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"Berlin\""));
        assertTrue(json.contains("\"dt\""));
    }

    @Test
    void startPolling_shouldUpdateWeatherAutomatically() throws InterruptedException{
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"name\":\"Tokyo\", \"dt\":1630425600}")
                .addHeader("Content-Type", "application/json"));

        sdk.getWeather("Tokyo");

        Thread.sleep(500);

        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"name\":\"Tokyo\", \"dt\":1630426600}")
                .addHeader("Content-Type", "application/json"));

        WeatherData updatedData = sdk.getWeather("Tokyo");

        assertNotNull(updatedData);
        assertEquals("Tokyo", updatedData.getName());
    }
}