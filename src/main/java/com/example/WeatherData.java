package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class WeatherData {
    @JsonProperty("weather")
    private List<Weather> weather;

    @JsonProperty("main")
    private Main main;

    @JsonProperty("visibility")
    private Long visibility;

    @JsonProperty("wind")
    private Wind wind;

    @JsonProperty("dt")
    private Long dt;

    @JsonProperty("sys")
    private Sys sys;

    @JsonProperty("timezone")
    private Long timezone;

    @JsonProperty("name")
    private String name;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    protected static class Weather{
        @JsonProperty("main")
        private String main;

        @JsonProperty("description")
        private String description;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    protected static class Main{
        @JsonProperty("temp")
        private Double temp;

        @JsonProperty("feels_like")
        private Double feels_like;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    protected static class Wind{
        @JsonProperty("speed")
        private Double speed;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    protected static class Sys{
        @JsonProperty("sunrise")
        private Long sunrise;

        @JsonProperty("sunset")
        private Long sunset;
    }
}