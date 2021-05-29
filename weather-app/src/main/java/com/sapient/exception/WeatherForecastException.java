package com.sapient.exception;

public class WeatherForecastException extends Exception {

	private static final long serialVersionUID = 876882487700880949L;

	public WeatherForecastException() {

	}

	public WeatherForecastException(String message) {
		super(message);

	}

	public WeatherForecastException(Throwable cause) {
		super(cause);

	}

	public WeatherForecastException(String message, Throwable cause) {
		super(message, cause);

	}

	public WeatherForecastException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
