package com.sapient.service;

import com.sapient.exception.WeatherForecastException;
import com.sapient.model.WeatherForecast;
import com.sapient.model.WeatherInfo;

public interface IWeatherForecastService {
	/** Get the weather forecast for a give city.
	 * @param city - city for which weather has to be retrived.
	 * @return WeatherForecast containing weather information about the city.
	 * @throws WeatherForecastException
	 */
	WeatherForecast getWeatherForecast(String city) throws WeatherForecastException;

	/**
	 * @param weatherForecast
	 * @return
	 * @throws WeatherForecastException
	 */
	WeatherInfo getWeatherInformation(WeatherForecast weatherForecast) throws WeatherForecastException;
}
