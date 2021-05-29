package com.sapient.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sapient.exception.WeatherForecastException;
import com.sapient.model.WeatherForecast;
import com.sapient.model.WeatherInfo;
import com.sapient.service.IWeatherForecastService;

@RestController
public class WeatherForecastController {

	@Autowired
	private IWeatherForecastService weatherForecastService;

	@GetMapping(path = "weather/{city}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getWeatherForecast(@PathVariable("city") String city) {
		try {
			WeatherForecast weatherForecast = weatherForecastService.getWeatherForecast(city);
			WeatherInfo weatherInformation = weatherForecastService.getWeatherInformation(weatherForecast);
			return ResponseEntity.ok(weatherInformation);
		} catch (WeatherForecastException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
}
