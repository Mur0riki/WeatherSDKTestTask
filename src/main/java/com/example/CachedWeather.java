package com.example;

public class CachedWeather {
    private final WeatherData data;
    private final long timestamp;

    public CachedWeather(WeatherData data) {
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp(){
        return timestamp;
    }

    public WeatherData getData() {
        return data;
    }
}
