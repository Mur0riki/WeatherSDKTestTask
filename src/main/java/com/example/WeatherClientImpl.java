package com.example;


import com.example.exceptions.WeatherClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class WeatherClientImpl implements WeatherClient {
    private String BASE_URL = "http://api.openweathermap.org/data/2.5/weather";
    private final String API_KEY;
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;


    public WeatherClientImpl(String API_KEY) {
        this.API_KEY = API_KEY;
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }
    public WeatherClientImpl(String API_KEY,String BASE_URL){
        this.API_KEY = API_KEY;
        this.okHttpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.BASE_URL = BASE_URL;
    }

    public WeatherData fetchWeather(String cityName) throws WeatherClientException {
        String url = String.format("%s?q=%s&appid=%s", BASE_URL, cityName, API_KEY);
        Request request = new Request.Builder().url(url).build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 401) {
                throw new WeatherClientException("Invalid API key");
            }
            if (response.code() == 404) {
                throw new WeatherClientException("City not found");
            }
            return objectMapper.readValue(response.body().string(), WeatherData.class);
        } catch (IOException e) {
            throw new WeatherClientException("Ошибка сети при получении данных о погоде", e);
        }
    }
}
