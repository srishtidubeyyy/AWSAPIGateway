package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;

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
public class UuidGenerator implements RequestHandler<Object,String> {

	private static final int NUM_UUIDS = 10;
	private static final String BUCKET_NAME = System.getenv("S3_TARGET_BUCKET");
	@Override
	public String handleRequest(Object input, Context context) {
		List<String> uuids = generateRandomUUIDs(NUM_UUIDS);
		String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		String fileName = "uuids_" + currentTime + ".txt";

		uploadToS3(uuids, fileName);

		return "Successfully generated and uploaded UUIDs to S3.";
	}

	private List<String> generateRandomUUIDs(int numUUIDs) {
		List<String> uuids = new ArrayList<>();
		for (int i = 0; i < numUUIDs; i++) {
			uuids.add(UUID.randomUUID().toString());
		}
		return uuids;
	}

	private void uploadToS3(List<String> data, String fileName) {
		AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
		String dataAsString = String.join("\n", data);

		byte[] contentBytes = dataAsString.getBytes();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);

		s3Client.putObject(new PutObjectRequest(BUCKET_NAME, fileName, inputStream, metadata));
	}
}
