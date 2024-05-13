package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

@LambdaHandler(lambdaName = "processor",
		roleName = "processor-role",
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
		tracingMode = TracingMode.Active
)
@LambdaUrlConfig()
@DependsOn(name = "Weather", resourceType = ResourceType.DYNAMODB_TABLE)
@EnvironmentVariable(key = "DYNAMODB_TARGET_TABLE", value = "${target_table}")

public class Processor implements RequestHandler<Object,String> {
	private final OpenAPIMeto openMeteoAPI;
	private final String dynamoDBTablename=System.getenv("DYNAMODB_TARGET_TABLE");
	AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
	public Processor() {
		openMeteoAPI = new OpenAPIMeto();
	}
	public String handleRequest(Object request, Context context) {
		try {
			String weatherForecast = openMeteoAPI.getWeatherForecast();
			System.out.println("Weather forecast: " + weatherForecast);
			putWeatherForecastInDynamoDB(weatherForecast);
			return weatherForecast;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return "error in finding data";
		}
	}
	private void putWeatherForecastInDynamoDB(String weatherForecast) {
//		Map<String, AttributeValue> item = new HashMap<>();
//		item.put("timestamp", new AttributeValue(String.valueOf(System.currentTimeMillis())));
//		item.put("forecastData", new AttributeValue(weatherForecast));
//
//		dynamoDB.putItem(dynamoDBTablename, item);
		String dynamoDBTableName = System.getenv("DYNAMODB_TARGET_TABLE");

		try {
			// Parse the weather forecast data JSON string
			JSONObject forecastData = new JSONObject(weatherForecast);

			// Generate a UUID for the id attribute
			String id = UUID.randomUUID().toString();

			// Construct the forecast object
			JSONObject forecastObject = forecastData.getJSONObject("forecast");

			Map<String, AttributeValue> item = new HashMap<>();
			item.put("id", new AttributeValue(id));
			item.put("forecast", new AttributeValue().withM(createForecastAttribute(forecastObject)));

			// Put the item into the DynamoDB table
			dynamoDB.putItem(dynamoDBTableName, item);
		} catch (JSONException e) {
			e.printStackTrace();
			// Handle JSON parsing error
		}
	}

	private Map<String, AttributeValue> createForecastAttribute(JSONObject forecastObject) throws JSONException {
		Map<String, AttributeValue> forecastAttribute = new HashMap<>();

		forecastAttribute.put("elevation", new AttributeValue().withN(String.valueOf(forecastObject.getDouble("elevation"))));
		forecastAttribute.put("generationtime_ms", new AttributeValue().withN(String.valueOf(forecastObject.getDouble("generationtime_ms"))));
		forecastAttribute.put("latitude", new AttributeValue().withN(String.valueOf(forecastObject.getDouble("latitude"))));
		forecastAttribute.put("longitude", new AttributeValue().withN(String.valueOf(forecastObject.getDouble("longitude"))));
		forecastAttribute.put("timezone", new AttributeValue(forecastObject.getString("timezone")));
		forecastAttribute.put("timezone_abbreviation", new AttributeValue(forecastObject.getString("timezone_abbreviation")));
		forecastAttribute.put("utc_offset_seconds", new AttributeValue().withN(String.valueOf(forecastObject.getInt("utc_offset_seconds"))));

		// Construct the hourly object
		JSONObject hourlyObject = forecastObject.getJSONObject("hourly");
		Map<String, AttributeValue> hourlyAttribute = new HashMap<>();
		hourlyAttribute.put("temperature_2m", new AttributeValue().withL(createNumberList(hourlyObject.getJSONArray("temperature_2m"))));
		hourlyAttribute.put("time", new AttributeValue().withL(createStringList(hourlyObject.getJSONArray("time"))));

		forecastAttribute.put("hourly", new AttributeValue().withM(hourlyAttribute));

		// Construct the hourly_units object
		JSONObject hourlyUnitsObject = forecastObject.getJSONObject("hourly_units");
		Map<String, AttributeValue> hourlyUnitsAttribute = new HashMap<>();
		hourlyUnitsAttribute.put("temperature_2m", new AttributeValue(hourlyUnitsObject.getString("temperature_2m")));
		hourlyUnitsAttribute.put("time", new AttributeValue(hourlyUnitsObject.getString("time")));

		forecastAttribute.put("hourly_units", new AttributeValue().withM(hourlyUnitsAttribute));

		return forecastAttribute;
	}

	private List<AttributeValue> createNumberList(JSONArray jsonArray) throws JSONException {
		List<AttributeValue> numberList = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			numberList.add(new AttributeValue().withN(String.valueOf(jsonArray.getDouble(i))));
		}
		return numberList;
	}

	private List<AttributeValue> createStringList(JSONArray jsonArray) throws JSONException {
		List<AttributeValue> stringList = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			stringList.add(new AttributeValue(jsonArray.getString(i)));
		}
		return stringList;
	}
}
