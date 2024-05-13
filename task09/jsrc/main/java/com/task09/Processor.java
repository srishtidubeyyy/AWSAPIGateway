package com.task09;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.TracingMode;
import com.task09.pojos.WeatherUnit;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

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

public class Processor implements RequestHandler<Object,WeatherUnit> {
	private OpenAPIMeto openAPIMeto=new OpenAPIMeto();
	private DynamoDbEnhancedClient dynamoDbEnhancedClient=DynamoDbEnhancedClient.create();
	private DynamoDbTable<WeatherUnit> weatherdata=dynamoDbEnhancedClient.table(
			System.getenv("DYNAMODB_TARGET_TABLE"),
			TableSchema.fromBean(WeatherUnit.class)
	);
	public WeatherUnit handleRequest(Object request,Context context){
        String forecast= null;
        try {
            forecast = openAPIMeto.getWeatherForecast();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WeatherUnit weatherUnit=new WeatherUnit(UUID.randomUUID().toString(),forecast);
		weatherdata.putItem(weatherUnit);
		return weatherUnit;
	}
	}