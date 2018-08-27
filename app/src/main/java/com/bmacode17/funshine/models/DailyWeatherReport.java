package com.bmacode17.funshine.models;

/**
 * Created by User on 24-Apr-18.
 */

public class DailyWeatherReport {
    private String city;
    private String country;
    private int temp;
    private int tempMin;
    private int tempMax;
    private String weatherType;
    private String rawDate;

    public DailyWeatherReport(String city, String country, int temp, int tempMin, int tempMax, String weatherType, String rawDate) {
        this.city = city;
        this.country = country;
        this.temp = temp;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.weatherType = weatherType;
        this.rawDate = rawDateToPretty(rawDate);
    }

    private String rawDateToPretty(String rawDate){
        return rawDate;
    }

    public String getCityName() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public int getTemp() {
        return temp;
    }

    public int getTempMin() {
        return tempMin;
    }

    public int getTempMax() {
        return tempMax;
    }

    public String getWeatherType() {
        return weatherType;
    }

    public String getRawDate() {
        return rawDate;
    }
}
