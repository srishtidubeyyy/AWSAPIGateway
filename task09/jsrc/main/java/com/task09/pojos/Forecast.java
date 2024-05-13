package com.task09.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@SuppressWarnings("unused")
@DynamoDbBean
public class Forecast {
    private float elevation;
    @JsonProperty("generationtime_ms")
    private double generationtimeMs;
    private double latitude;
    private double longitude;
    private String timezone;
    @JsonProperty("timezone_abbreviation")
    private String timezoneAbbreviation;
    @JsonProperty("utc_offset_seconds")
    private int utcOffsetSeconds;
    @JsonProperty("hourly_units")
    private HourlyUnits hourlyUnits;
    private Hourly hourly;

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    @DynamoDbAttribute("generationtime_ms")
    public double getGenerationtimeMs() {
        return generationtimeMs;
    }
    public void setGenerationtimeMs(double generationtimeMs) {
        this.generationtimeMs = generationtimeMs;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    @DynamoDbAttribute("timezone_abbreviation")
    public String getTimezoneAbbreviation() {
        return timezoneAbbreviation;
    }

    public void setTimezoneAbbreviation(String timezoneAbbreviation) {
        this.timezoneAbbreviation = timezoneAbbreviation;
    }
    @DynamoDbAttribute("utc_offset_seconds")
    public int getUtcOffsetSeconds() {
        return utcOffsetSeconds;
    }

    public void setUtcOffsetSeconds(int utcOffsetSeconds) {
        this.utcOffsetSeconds = utcOffsetSeconds;
    }
    @DynamoDbAttribute("hourly_units")
    public HourlyUnits getHourlyUnits() {
        return hourlyUnits;
    }

    public void setHourlyUnits(HourlyUnits hourlyUnits) {
        this.hourlyUnits = hourlyUnits;
    }

    public Hourly getHourly() {
        return hourly;
    }

    public void setHourly(Hourly hourly) {
        this.hourly = hourly;
    }
}
