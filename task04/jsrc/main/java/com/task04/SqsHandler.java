package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.events.SqsTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.util.HashMap;
import java.util.Map;

@LambdaHandler(lambdaName = "sqs_handler",
	roleName = "sqs_handler-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SqsTriggerEventSource(batchSize = 1, targetQueue = "async_queue")
public class SqsHandler implements RequestHandler<SQSEvent,String> {

	public String handleRequest(SQSEvent event , Context context) {
		for(SQSEvent.SQSMessage message: event.getRecords()){
			String messageBody= message.getBody();
			System.out.println("Received Message:"+messageBody);
		}
		return " executed successfully from lambda sqs handler";
	}
}

