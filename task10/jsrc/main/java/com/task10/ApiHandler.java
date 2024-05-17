package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task10.handler.ReservationsHandler;
import com.task10.handler.TablesHandler;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@LambdaHandler(lambdaName = "api_handler",
		roleName = "api_handler-role",
		isPublishVersion = false,
		logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final String USER_POOL_NAME = "cmtr-cb6c2635-simple-booking-userpool-test";
	private final CognitoIdentityProviderClient cognitoClient;
	private static final String USER_POOL_ID = getUserPoolId();
	private static final String APP_CLIENT_ID = getClientId();
	public ApiHandler() {
		cognitoClient = CognitoIdentityProviderClient.create();
	}
	public static String getUserPoolId() {
		CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create();
		ListUserPoolsRequest listUserPoolsRequest = ListUserPoolsRequest.builder().maxResults(10).build();
		ListUserPoolsResponse listUserPoolsResponse = cognitoClient.listUserPools(listUserPoolsRequest);
		String userPoolId = listUserPoolsResponse.userPools().get(0).id();
		for(UserPoolDescriptionType userPool : listUserPoolsResponse.userPools()) {
			if (userPool.name().equals(USER_POOL_NAME)) {
				userPoolId = userPool.id();
				System.out.println("User Pool Id is: " + userPoolId);
				break;
			}
		}
		return userPoolId;
	}

	public static String getClientId() {
		CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.create();
		ListUserPoolClientsRequest listUserPoolClientsRequest = ListUserPoolClientsRequest.builder()
				.userPoolId(USER_POOL_ID).maxResults(10).build();
		ListUserPoolClientsResponse listUserPoolClientsResponse = cognitoClient
				.listUserPoolClients(listUserPoolClientsRequest);
		String clientId = "";
		for (UserPoolClientDescription userPoolClient : listUserPoolClientsResponse.userPoolClients()) {
			if (userPoolClient.clientName().equals("client-app")) {
				clientId = userPoolClient.clientId();
				System.out.println("Client Id is: " + clientId);
				break;
			}
		}
		return clientId;
	}

	public  APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
		try {
			System.out.println(event);
			System.out.println(event.getResource());
			switch (event.getResource()) {
				case "/signup":
					return handleSignUp(event);
				case "/signin":
					return handleSignIn(event);
				case "/tables":
					return handleTables(event);
				case "/tables/{tableId}":
					return handleTables(event);
				case "/reservations":
					return handleReservations(event);
				default:
					break;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400);
	}
	private APIGatewayProxyResponseEvent handleReservations(APIGatewayProxyRequestEvent event) {
		if (event.getHttpMethod().equalsIgnoreCase("GET")) {
			return new ReservationsHandler().getAllReservations();
		} else if (event.getHttpMethod().equalsIgnoreCase("POST")) {
			return new ReservationsHandler().postReservations(event);
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400);
	}
	private APIGatewayProxyResponseEvent handleTables(APIGatewayProxyRequestEvent event) {

		if(Objects.nonNull(event.getPathParameters())){
			return new TablesHandler().getTables(event);
		} else if (event.getHttpMethod().equalsIgnoreCase("GET")) {
			return new TablesHandler().getAllTables();
		} else if (event.getHttpMethod().equalsIgnoreCase("POST")) {
			return new TablesHandler().postTables(event);
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400);
	}
	public APIGatewayProxyResponseEvent handleSignUp(APIGatewayProxyRequestEvent event) {
		try {
			Map<String, String> input = new Gson().fromJson(event.getBody(), HashMap.class);
			System.out.println("request body is parsed");
			String firstName = input.get("firstName");
			String lastName = input.get("lastName");
			String email = input.get("email");
			String password = input.get("password");
			// Validate the inputs here! (Check the email format and password rules)
			System.out.println("Creating user_request");
			AdminCreateUserRequest user_request = AdminCreateUserRequest.builder()
					.userPoolId(USER_POOL_ID)
					.username(email)
//              .userAttributes(
//                    AttributeType.builder()
//                          .name("firstName")
//                          .value(firstName)
//                          .build())
////                      AttributeType.builder()
////                            .name("lastName")
////                            .value(lastName)
////                            .build())
					.messageAction("SUPPRESS")
					.build();
			cognitoClient.adminCreateUser(user_request);
			//Here's how you can set a user's password in Cognito User Pool
			AdminSetUserPasswordRequest passwordRequest = AdminSetUserPasswordRequest.builder()
					.username(email)
					.password(password)
					.userPoolId(USER_POOL_ID)
					.permanent(true)
					.build();
			cognitoClient.adminSetUserPassword(passwordRequest);
			return new APIGatewayProxyResponseEvent().withStatusCode(200);
		} catch (Exception e) {
			// Error handling here
			return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Failed to sign up!");
		}
	}
	public APIGatewayProxyResponseEvent handleSignIn(APIGatewayProxyRequestEvent request) {
		Map<String, String> input = new Gson().fromJson(request.getBody(), HashMap.class);
		CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder().region(Region.EU_CENTRAL_1).build();
		Map<String,String> authParameters = new HashMap<>();
		authParameters.put("USERNAME", input.get("email"));
		authParameters.put("PASSWORD", input.get("password"));
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
				.authFlow(AuthFlowType.USER_PASSWORD_AUTH)
				.authParameters(authParameters)
				.clientId(APP_CLIENT_ID)
				.build();
		try {
			InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
			response.setBody("{ \"accessToken\": \"" + authResponse.authenticationResult().accessToken() + "\"}");
			response.setStatusCode(200);
		} catch (NotAuthorizedException e) {
			response.setStatusCode(400);
		} catch (UserNotFoundException e) {
			response.setStatusCode(400);
		} catch (Exception e) {
			response.setStatusCode(400);
		}
		return response;
	}
}