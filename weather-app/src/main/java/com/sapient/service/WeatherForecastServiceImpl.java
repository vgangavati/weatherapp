package com.sapient.service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
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
			ZonedDateTime now = ZonedDateTime.now();
			Long afterThreeDays = now.plusDays(3).toLocalDate().toEpochDay();
			Supplier<Stream<WeatherData>> filter = () -> weatherForecast.getWeatherData().stream().filter(data -> {
				return Date.from(Instant.ofEpochMilli(data.getDt()))
						.after(Date.from(Instant.ofEpochMilli(afterThreeDays)));
			});
			filter.get().forEach(weatherData -> {
				highTemperature.add(weatherData.getMain().getTempMax());
				lowTemperature.add(weatherData.getMain().getTempMin());
			});
			DoubleSummaryStatistics temperatureStats = filter.get().mapToDouble(weatherData -> {
				return (weatherData.getMain().getTempMax() + weatherData.getMain().getTempMin()) / 2;
			}).summaryStatistics();
			DoubleSummaryStatistics windStats = filter.get()
					.mapToDouble(weatherData -> weatherData.getWind().getSpeed())
					.summaryStatistics();
			if (temperatureStats.getAverage() > maxTemperature) {
				weatherInfo.setWeatherMessage("Use sunscreen lotion");
			} else if (windStats.getAverage() > maxWind) {
				weatherInfo.setWeatherMessage("too windy");
			} else {
				weatherInfo.setWeatherMessage("carry umbrella");
			}
			weatherInfo.setHighTemperature(highTemperature);
			weatherInfo.setLowTemperature(lowTemperature);
			return weatherInfo;
		} catch (Exception e) {
			throw new WeatherForecastException(e.getMessage(), e);
		}
	}

}
