package com.task09.pojos;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@SuppressWarnings("unused")
@DynamoDbBean
public class WeatherUnit {
    private String id;
    private Forecast forecast;

    public WeatherUnit(String id, Forecast forecast) {
        this.id = id;
        this.forecast = forecast;
    }

    public WeatherUnit() {
    }

    public WeatherUnit(String string, String forecast) {
    }

    @DynamoDbPartitionKey
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Forecast getForecast() {
        return forecast;
    }

    public void setForecast(Forecast forecast) {
        this.forecast = forecast;
    }
}
