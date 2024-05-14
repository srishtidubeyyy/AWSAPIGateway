package com.task09;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.io.IOException;
import java.util.*;
@LambdaHandler(lambdaName = "processor",
		roleName = "processor-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
		tracingMode = TracingMode.Active
)
@LambdaUrlConfig(authType = AuthType.NONE, invokeMode = InvokeMode.BUFFERED)
@DynamoDbTriggerEventSource(batchSize = 10, targetTable = "Weather")

public class Processor implements RequestHandler<Object, Map<String, Object>> {

	public Map<String, Object> handleRequest(Object request, Context context) {
		// Retrieve weather data from the Open-Meteo API
        String weatherData = null;
        try {
            weatherData = retrieveWeatherData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Push the weather data to DynamoDB
		if (weatherData != null) {
			pushToDynamoDB(weatherData);
		}

		// Prepare the response
		Map<String, Object> response = new HashMap<>();
		response.put("statusCode", 200);
		response.put("body", "Weather data pushed to DynamoDB successfully");

		return response;
	}

	private String retrieveWeatherData() throws IOException, InterruptedException {
		OpenAPIMeto meteoAPI = new OpenAPIMeto();
		return meteoAPI.getWeatherForecast();
	}


	private void pushToDynamoDB(String weatherData) {
		try {
			final Table eventsTable;
			final Table eventsTable2;
			// Create DynamoDB client
			AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
			DynamoDB dynamoDB = new DynamoDB(client);

			// Get DynamoDB table
			eventsTable = dynamoDB.getTable("cmtr-9e564e24-Weather-test");
			eventsTable2 = dynamoDB.getTable("cmtr-9e564e24-Weather");

			// Generate UUID for the primary key
			String id = UUID.randomUUID().toString();

			// Create item to be inserted into DynamoDB table
			Item item = new Item().withPrimaryKey("id", id).withJSON("forecast", weatherData);

			// Insert item into DynamoDB table
			eventsTable.putItem(item);
			eventsTable2.putItem(item);

			// Log success message
			System.out.println("Weather data inserted into DynamoDB successfully.");
		} catch (Exception e) {
			// Log error message
			System.err.println("Error while inserting weather data into DynamoDB: " + e.getMessage());
			// You might want to handle the error more gracefully here
		}
	}
}