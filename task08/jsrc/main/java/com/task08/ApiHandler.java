package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
	    roleName = "api_handler-role",
		layers = {"apilayer"} ,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<Object, Map<String, Object>> {
	private final OpenMeteoAPI openMeteoAPI;

	public ApiHandler() {
		openMeteoAPI = new OpenMeteoAPI();
	}

	public Map<String, Object> handleRequest(Object request, Context context) {
		try {
			String weatherForecast = openMeteoAPI.getWeatherForecast();
			System.out.println("Weather forecast: " + weatherForecast);

			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("statusCode", 200);
			resultMap.put("body", "Weather forecast: " + weatherForecast);
			return resultMap;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			Map<String, Object> errorResult = new HashMap<>();
			errorResult.put("statusCode", 500);
			errorResult.put("body", "Failed to fetch weather forecast");
			return errorResult;
		}
	}
}
