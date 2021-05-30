package com.sapient.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapient.exception.WeatherForecastException;
import com.sapient.model.WeatherData;
import com.sapient.model.WeatherForecast;
import com.sapient.model.WeatherForecastClientError;
import com.sapient.model.WeatherInfo;

@Service
public class WeatherForecastServiceImpl implements IWeatherForecastService {
	@Value("${weather.forecast.url}")
	private String weatherForecastUrl;

	@Value("${weather.forecast.maxTemp}")
	private double maxTemperature;

	@Value("${weather.forecast.maxWind}")
	private double maxWind;

	private ObjectMapper mapper = new ObjectMapper();

	public WeatherForecast getWeatherForecast(String city) throws WeatherForecastException {
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet httpRequest = new HttpGet(String.format(weatherForecastUrl, city));
		ExponentialBackOff backOff = new ExponentialBackOff(100, 1.5);
		backOff.setMaxElapsedTime(10*1000);
		BackOffExecution backOffExecution = backOff.start();
		while (backOffExecution.nextBackOff() != BackOffExecution.STOP) {
			try {
				HttpResponse response = client.execute(httpRequest);
				if (response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
					return mapper.readValue(response.getEntity().getContent(), WeatherForecast.class);
				} else {
					WeatherForecastClientError clientError = mapper.readValue(response.getEntity().getContent(),
							WeatherForecastClientError.class);
					throw new WeatherForecastException("Failed to get respone " + clientError.getMessage()
							+ " statusCode = " + clientError.getCod());
				}
			} catch (Exception e) {
				throw new WeatherForecastException(e.getMessage(), e);
			}
		}
		return null;
	}

	public WeatherInfo getWeatherInformation(WeatherForecast weatherForecast) throws WeatherForecastException {
		try {
			WeatherInfo weatherInfo = new WeatherInfo();
			List<Double> highTemperature = new ArrayList<>();
			List<Double> lowTemperature = new ArrayList<>();
			Supplier<Stream<WeatherData>> filter = () -> weatherForecast.getWeatherData().stream().filter(data -> {
				return ChronoUnit.DAYS.between(
						LocalDate.parse(LocalDate.now().toString(), DateTimeFormatter.ISO_LOCAL_DATE),
						Instant.ofEpochMilli(data.getDt()).atZone(ZoneId.systemDefault()).toLocalDate()) <= 3;
			});
			// show the user weather 3 days high, low temperature
			filter.get().forEach(weatherData -> {
				highTemperature.add(weatherData.getMain().getTempMax());
				lowTemperature.add(weatherData.getMain().getTempMin());
			});
			weatherInfo.setHighTemperature(highTemperature);
			weatherInfo.setLowTemperature(lowTemperature);
			
			// Check if temperature is crossing maxTemperature, then send message "carry umbrella"
			DoubleSummaryStatistics temperatureStats = filter.get().mapToDouble(weatherData -> {
				return (weatherData.getMain().getTempMax() + weatherData.getMain().getTempMin()) / 2;
			}).summaryStatistics();
			
			if (temperatureStats.getAverage() > maxTemperature) {
				weatherInfo.setWeatherMessage("Use sunscreen lotion");
			}
			
			// Check if wind speed is crossing maxWind, then send message "too windy"
			DoubleSummaryStatistics windStats = filter.get()
					.mapToDouble(weatherData -> weatherData.getWind().getSpeed())
					.summaryStatistics();
			 if (windStats.getAverage() > maxWind) {
				 weatherInfo.setWeatherMessage("too windy");
				//weatherInfo.setWeatherMessage(weatherInfo.getWeatherMessage() + " too windy");
			}

			// Check if it's rainy.
			boolean isRainy = filter.get().anyMatch(weatherData -> {
				return weatherData.getWeather().stream().anyMatch(rainData -> {
					return rainData.getDescription().contains("rain");
				});
			});

			if (isRainy) {
				weatherInfo.setWeatherMessage("carry umbrella");
				//weatherInfo.setWeatherMessage(weatherInfo.getWeatherMessage() + " carry umbrella");
			}

			return weatherInfo;
		} catch (Exception e) {
			throw new WeatherForecastException(e.getMessage(), e);
		}
	}

}
