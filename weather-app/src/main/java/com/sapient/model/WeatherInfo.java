package com.sapient.model;

import java.util.List;

public class WeatherInfo {
	List<Double> highTemperature;
	List<Double> lowTemperature;
	String weatherMessage;

	public List<Double> getHighTemperature() {
		return highTemperature;
	}

	public void setHighTemperature(List<Double> highTemperature) {
		this.highTemperature = highTemperature;
	}

	public List<Double> getLowTemperature() {
		return lowTemperature;
	}

	public void setLowTemperature(List<Double> lowTemperature) {
		this.lowTemperature = lowTemperature;
	}

	public String getWeatherMessage() {
		return weatherMessage;
	}

	public void setWeatherMessage(String weatherMessage) {
		this.weatherMessage = weatherMessage;
	}

}
