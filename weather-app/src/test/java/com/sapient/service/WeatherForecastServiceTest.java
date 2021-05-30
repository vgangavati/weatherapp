package com.sapient.service;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapient.WeatherApplication;
import com.sapient.model.WeatherForecast;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(classes = WeatherApplication.class)
public class WeatherForecastServiceTest {

	@Autowired
	private MockMvc mvc;

	@SpyBean
	private IWeatherForecastService weatherForecastService;

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void testRainyWeather() throws Exception {
		InputStream rainyDataStream = this.getClass().getResourceAsStream("/apiweatherJson/testRain.json");
		WeatherForecast weatherForecast = mapper.readValue(rainyDataStream, WeatherForecast.class);
		Mockito.when(weatherForecastService.getWeatherForecast("bangalore"))
				.thenReturn(weatherForecast);
		 mvc.perform(get("/weather/bangalore"))
				 .andExpect(status().isOk())
				.andExpect(jsonPath("$.weatherMessage", is("carry umbrella")));
	 }

	@Test
	public void testMaxTemperature() throws Exception {
		InputStream rainyDataStream = this.getClass().getResourceAsStream("/apiweatherJson/testMaxTemperature.json");
		WeatherForecast weatherForecast = mapper.readValue(rainyDataStream, WeatherForecast.class);
		Mockito.when(weatherForecastService.getWeatherForecast("bangalore")).thenReturn(weatherForecast);
		mvc.perform(get("/weather/bangalore")).andExpect(status().isOk())
				.andExpect(jsonPath("$.weatherMessage", is("Use sunscreen lotion")));
	}

	@Test
	public void testMaxWindy() throws Exception {
		InputStream rainyDataStream = this.getClass().getResourceAsStream("/apiweatherJson/testMaxWind.json");
		WeatherForecast weatherForecast = mapper.readValue(rainyDataStream, WeatherForecast.class);
		Mockito.when(weatherForecastService.getWeatherForecast("bangalore")).thenReturn(weatherForecast);
		mvc.perform(get("/weather/bangalore")).andExpect(status().isOk())
				.andExpect(jsonPath("$.weatherMessage", is("too windy")));
	}
}
