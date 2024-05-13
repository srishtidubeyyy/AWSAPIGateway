package com.task09.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@SuppressWarnings("unused")
@DynamoDbBean
public class HourlyUnits {
    @JsonProperty("temperature_2m")
    private String temperature2m;
    private String time;
    @DynamoDbAttribute("temperature_2m")
    public String getTemperature2m() {
        return temperature2m;
    }

    public void setTemperature2m(String temperature2m) {
        this.temperature2m = temperature2m;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
