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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
		Map<String, AttributeValue> item = new HashMap<>();
		item.put("timestamp", new AttributeValue(String.valueOf(System.currentTimeMillis())));
		item.put("forecastData", new AttributeValue(weatherForecast));

		dynamoDB.putItem(dynamoDBTablename, item);
	}
}
