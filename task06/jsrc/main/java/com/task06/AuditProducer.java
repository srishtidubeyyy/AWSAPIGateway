package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.task06.models.AuditEntry;
import com.task06.models.ConfigurationItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@LambdaHandler(
		lambdaName = "audit_producer",
		roleName = "audit_producer-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(
		targetTable = "Configuration",
		batchSize = 10
)
@DependsOn(
		name = "Configuration",
		resourceType = ResourceType.DYNAMODB_TABLE
)
@EnvironmentVariable(key = "DYNAMODB_TARGET_TABLE", value = "${target_table}")
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {
	private final String table_name=System.getenv("DYNAMODB_TARGET_TABLE");
	private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
	private static final Logger logger = Logger.getLogger(AuditProducer.class.getName());

	@Override
	public Void handleRequest(DynamodbEvent event, Context context) {
		for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
			String eventName = record.getEventName();
			if ("INSERT".equals(eventName)) {
				handleInsertEvent(record);
			} else if ("MODIFY".equals(eventName)) {
				handleModifyEvent(record);
			}
		}
		return null;
	}

	private void handleInsertEvent(DynamodbEvent.DynamodbStreamRecord record) {
		logger.info("Method handleInsertEvent is called");
		ConfigurationItem newItem = convertDynamodbRecordToConfigurationItem(record.getDynamodb().getNewImage());
		AuditEntry auditEntry = createAuditEntry(newItem);
		AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
		PutItemRequest putItemRequest = new PutItemRequest()
				.withTableName(table_name)
				.withItem(auditEntry.toMap());
		PutItemResult putItemResult = dynamoDBClient.putItem(putItemRequest);
		String logMessage = "Audit entry for INSERT event saved to Audit table: " + putItemResult;//
		logger.log(Level.INFO, logMessage);
	}

	private void handleModifyEvent(DynamodbEvent.DynamodbStreamRecord record) {
		logger.info("Method handleModifyEvent is called");
		ConfigurationItem oldItem = convertDynamodbRecordToConfigurationItem(record.getDynamodb().getOldImage());
		ConfigurationItem newItem = convertDynamodbRecordToConfigurationItem(record.getDynamodb().getNewImage());
		AuditEntry auditEntry = createAuditEntry(oldItem, newItem);
		AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
		PutItemRequest putItemRequest = new PutItemRequest()
				.withTableName(table_name)
				.withItem(auditEntry.toMap());
		PutItemResult putItemResult = dynamoDBClient.putItem(putItemRequest);
		String logMessage = "Audit entry for MODIFY event: " + putItemResult;
		logger.log(Level.INFO, logMessage);
	}

	private ConfigurationItem convertDynamodbRecordToConfigurationItem(Map<String, com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue> dynamodbImage) {
		logger.info("Method convertDynamodbRecordToConfigurationItem is called");
		ConfigurationItem item = new ConfigurationItem();
		item.setKey(dynamodbImage.get("key").getS());
		item.setValue(Integer.parseInt(dynamodbImage.get("value").getN()));
		return item;
	}

	private AuditEntry createAuditEntry(ConfigurationItem newItem) {
		logger.info("Method createAuditEntry with one argument is called");
		AuditEntry auditEntry = new AuditEntry();
		auditEntry.setId(UUID.randomUUID().toString());
		auditEntry.setItemKey(newItem.getKey());
		auditEntry.setModificationTime(LocalDateTime.now());
		auditEntry.setNewValue(newItem.getValue());
		return auditEntry;
	}

	private AuditEntry createAuditEntry(ConfigurationItem oldItem, ConfigurationItem newItem) {
		logger.info("Method createAuditEntry with two arguments is called");
		AuditEntry auditEntry = new AuditEntry();
		auditEntry.setId(UUID.randomUUID().toString());
		auditEntry.setItemKey(newItem.getKey());
		auditEntry.setModificationTime(LocalDateTime.now());
		auditEntry.setUpdatedAttribute("value");
		auditEntry.setOldValue(oldItem.getValue());
		auditEntry.setNewValue(newItem.getValue());
		return auditEntry;
	}
}