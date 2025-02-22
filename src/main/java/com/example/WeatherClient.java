package com.example;

import com.example.exceptions.WeatherClientException;

public interface WeatherClient {
    WeatherData fetchWeather(String cityName) throws WeatherClientException;
}
