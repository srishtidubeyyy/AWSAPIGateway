package com.task05;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task05.pojos.Event;
import com.task05.pojos.Request;
import com.task05.pojos.Response;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariable(key = "DYNAMODB_TABLE_NAME", value = "${target_table}")
public class ApiHandler implements RequestHandler<Request, Response> {

	public Response handleRequest(Request request, Context context) {
		context.getLogger().log(request.toString());
		String tableName=System.getenv("DYNAMODB_TABLE_NAME");
		try(DynamoDbClient dynamoDbClient=DynamoDbClient.create()){
			Event event=new Event(UUID.randomUUID().toString(),request.getPrincipalId(), LocalDateTime.now(),request.getContent());
			dynamoDbClient.putItem(i->i.tableName(tableName).item(Map.of(
					"id", AttributeValue.fromS(event.getId()),
					"principalId",AttributeValue.fromN(Integer.toString(event.getPrincipalId())),
					"createdAt",AttributeValue.fromS(LocalDateTime.now().toString()),
					"body", AttributeValue.fromM(request.getContent().entrySet()
							.stream()
							.collect(Collectors.toMap(Map.Entry::getKey, v->AttributeValue.fromS(v.getValue()))))
			)));
			return new Response(200,event);
		}
	}
}

