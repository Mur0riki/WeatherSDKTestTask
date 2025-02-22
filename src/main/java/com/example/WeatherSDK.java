package com.example;

import com.example.exceptions.WeatherClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherSDK {

    private final String apiKey;
    private final Mode mode;
    private final WeatherClient client;
    private static final Map<String, WeatherSDK> instances = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Cache<String, CachedWeather> weatherDataCache;

    private WeatherSDK(String apiKey, Mode mode,String BASE_URL) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.client = new WeatherClientImpl(apiKey,BASE_URL);
        this.weatherDataCache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        if (mode == Mode.POLLING) {
            startPolling();
        }
    }
    private WeatherSDK(String apiKey, Mode mode) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.client = new WeatherClientImpl(apiKey);
        this.weatherDataCache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        if (mode == Mode.POLLING) {
            startPolling();
        }
    }
    @Override
    public String toString() {
        return "WeatherSDK{" +
                "apiKey='" + apiKey + '\'' +
                ", mode=" + mode +
                '}';
    }

    public static WeatherSDK addInstance(String apiKey, Mode mode) {
        if (instances.containsKey(apiKey))
            throw new IllegalArgumentException("Ошибка: экземпляр с ключом '" + apiKey + "' уже создан.");

        WeatherSDK sdk = new WeatherSDK(apiKey, mode);
        instances.put(apiKey, sdk);
        return sdk;
    }
    public static WeatherSDK addInstance(String apiKey, Mode mode,String BASE_URL) {
        if (instances.containsKey(apiKey))
            throw new IllegalArgumentException("Ошибка: экземпляр с ключом '" + apiKey + "' уже создан.");

        WeatherSDK sdk = new WeatherSDK(apiKey, mode,BASE_URL);
        instances.put(apiKey, sdk);
        return sdk;
    }

    public static void removeInstance(String apiKey) {
        if (!instances.containsKey(apiKey))
            throw new IllegalArgumentException("Ошибка: экземпляр с ключом '" + apiKey + "' не найден.");

        instances.remove(apiKey);
    }

    public WeatherData getWeather(String city) {
        if (weatherDataCache.asMap().containsKey(city)) {
            return weatherDataCache.getIfPresent(city).getData();
        }
        WeatherData data = null;
        try {
            data = client.fetchWeather(city);
        } catch (WeatherClientException e) {
            throw new RuntimeException(e);
        }
        updateCache(city, data);
        return data;
    }

    public String getWeatherAsJson(String city) {
        WeatherData data = getWeather(city);
        return serializeToJson(data);
    }

    private String serializeToJson(WeatherData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сериализации JSON", e);
        }
    }

    private void updateCache(String city, WeatherData data) {
        if(weatherDataCache.size() == 10){
            String theOldestCity = "";
            long time = -1;
            for(Map.Entry<String, CachedWeather> entry: weatherDataCache.asMap().entrySet()){
                if(time == -1){
                    time = entry.getValue().getTimestamp();
                }
                if(entry.getValue().getTimestamp() < time){
                    time = entry.getValue().getTimestamp();
                    theOldestCity = entry.getValue().getData().getName();
                }
            }
            weatherDataCache.invalidate(theOldestCity);
        }
        weatherDataCache.put(city,new CachedWeather(data));
    }

    private void startPolling() {
        scheduler.scheduleAtFixedRate(() -> {
            for (String city : weatherDataCache.asMap().keySet()) {
                WeatherData data = null;
                try {
                    data = client.fetchWeather(city);
                } catch (WeatherClientException e) {
                    throw new RuntimeException(e);
                }
                updateCache(city, data);
            }
        }, 0, 10, TimeUnit.MINUTES);
    }
}