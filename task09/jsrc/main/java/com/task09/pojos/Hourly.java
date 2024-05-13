package com.task09.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@SuppressWarnings("unused")
@DynamoDbBean
public class Hourly {
    @JsonProperty("temperature_2m")
    private List<Float> temperature2m;
    private List<String> time;
    @DynamoDbAttribute("temperature_2m")
    public List<Float> getTemperature2m() {
        return temperature2m;
    }

    public void setTemperature2m(List<Float> temperature2m) {
        this.temperature2m = temperature2m;
    }

    public List<String> getTime() {
        return time;
    }

    public void setTime(List<String> time) {
        this.time = time;
    }
}
