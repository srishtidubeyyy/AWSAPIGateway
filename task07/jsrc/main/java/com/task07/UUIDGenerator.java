package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.task07.pojo.S3File;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@LambdaHandler(lambdaName = "uuid_generator",
	roleName = "uuid_generator-role",
	isPublishVersion = false,
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(name = "uuid-storage",
		resourceType = ResourceType.S3_BUCKET)
@DependsOn(name = "uuid_trigger",
		resourceType = ResourceType.CLOUDWATCH_RULE)
@RuleEventSource(targetRule = "uuid_trigger")
@EnvironmentVariable(key = "S3_TARGET_BUCKET", value = "${target_bucket}")
@EnvironmentVariable(key = "MY_AWS_S3_REGION",value = "${region}")
public class UUIDGenerator implements RequestHandler<Object, Void> {

	private static final String BUCKET_NAME = "your-bucket-name";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	private static final String AWS_REGION=System.getenv("MY_AWS_S3_REGION");

	@Override
	public Void handleRequest(Object input, Context context) {
		List<UUID> uuidList = generateRandomUUIDs(10);

		S3File s3File = new S3File(uuidList);
		String jsonData = convertToJSON(s3File);

		String currentDate = LocalDateTime.now().format(DATE_FORMATTER);
		String fileName = currentDate;

		uploadToS3(jsonData, fileName);

		return null;
	}

	private List<UUID> generateRandomUUIDs(int numUUIDs) {
		List<UUID> uuids = new ArrayList<>();
		for (int i = 0; i < numUUIDs; i++) {
			uuids.add(UUID.randomUUID());
		}
		return uuids;
	}

	private String convertToJSON(S3File s3File) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(s3File);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "{}";
		}
	}

	private void uploadToS3(String jsonData, String fileName) {
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withRegion(AWS_REGION)
				.withForceGlobalBucketAccessEnabled(true)
				.build();

		byte[] contentBytes = jsonData.getBytes();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setContentType("application/json");

		s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileName, inputStream, metadata));
	}
}