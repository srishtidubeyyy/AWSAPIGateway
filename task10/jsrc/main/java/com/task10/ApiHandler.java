package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task10.pojos.*;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@LambdaHandler(lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final Gson gson = new Gson();
	private final CognitoService cognitoService = new CognitoService();

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		String path = request.getPath();
		String method = request.getHttpMethod();

		switch (path) {
			case "/signup":
				return signUp(request);
			case "/signin":
				return signIn(request);
			case "/tables":
				if ("GET".equals(method)) {
					return getTables(request);
				} else if ("POST".equals(method)) {
					return createTable(request);
				} else {
					return badRequest();
				}
			case "/reservations":
				if ("GET".equals(method)) {
					return getReservations(request);
				} else if ("POST".equals(method)) {
					return createReservation(request);
				} else {
					return badRequest();
				}
			default:
				return notFound();
		}
	}
	private APIGatewayProxyResponseEvent signUp(APIGatewayProxyRequestEvent request) {
		String body = request.getBody();
		Gson gson = new Gson();
		SignUpRequest signUpRequest = gson.fromJson(body, SignUpRequest.class);

		// Validate email and password
		if (!isValidEmail(signUpRequest.getEmail())) {
			return badRequest("Invalid email format");
		}
		if (!isValidPassword(signUpRequest.getPassword())) {
			return badRequest("Invalid password format");
		}

		// Call Cognito service to sign up user
		try {
			cognitoService.signUpUser(signUpRequest.getEmail(), signUpRequest.getPassword());
			return successResponse("User signed up successfully");
		} catch (Exception e) {
			return serverErrorResponse("Error signing up user: " + e.getMessage());
		}
	}
	private boolean isValidEmail(String email) {
		// Implement email validation logic (e.g., using regex)
		if (email == null || email.isEmpty()) {
			return false;
		}
		// Simple email format validation using regex
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		return email.matches(emailRegex);
	}

	private boolean isValidPassword(String password) {
		// Implement password validation logic (e.g., check length, strength)
		if (password == null || password.length() < 12) {
			return false;
		}
		// Simple password strength validation
		// Password must be alphanumeric with at least one special character from "$%^*"
		String passwordRegex = "^(?=.*[a-zA-Z0-9])(?=.*[$%^*])[a-zA-Z0-9$%^*]+$";
		return password.matches(passwordRegex);
	}

	private APIGatewayProxyResponseEvent signIn(APIGatewayProxyRequestEvent request) {
		String body = request.getBody();
		Gson gson = new Gson();
		SignInRequest signInRequest = gson.fromJson(body, SignInRequest.class);

		// Call Cognito service to sign in user
		try {
			String accessToken = cognitoService.signInUser(signInRequest.getEmail(), signInRequest.getPassword());
			return successResponse("User signed in successfully", accessToken);
		} catch (Exception e) {
			return serverErrorResponse("Error signing in user: " + e.getMessage());
		}
	}
	private APIGatewayProxyResponseEvent successResponse(String message) {
		return successResponse(message, null);
	}

	private APIGatewayProxyResponseEvent successResponse(String message, String accessToken) {
		JsonObject responseBody = new JsonObject();
		responseBody.addProperty("message", message);
		if (accessToken != null) {
			responseBody.addProperty("accessToken", accessToken);
		}
		return response(200, responseBody);
	}
	private APIGatewayProxyResponseEvent serverErrorResponse(String message) {
		JsonObject responseBody = new JsonObject();
		responseBody.addProperty("message", message);
		return response(500, responseBody);
	}
	private APIGatewayProxyResponseEvent response(int statusCode, JsonObject responseBody) {
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(statusCode);
		response.setBody(responseBody.toString());
		return response;
	}

	private APIGatewayProxyResponseEvent getTables(APIGatewayProxyRequestEvent request) {
		// Implement logic to fetch tables
		List<Table> tables = new ArrayList<>();
		// Populate tables with actual data
		// Dummy data for demonstration purposes
		tables.add(new Table(1, 101, 4, false, 50));
		tables.add(new Table(2, 102, 6, true, null));
		String responseBody = gson.toJson(tables);
		return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(responseBody);
	}

	private APIGatewayProxyResponseEvent createTable(APIGatewayProxyRequestEvent request) {
		// Implement logic to create a new table
		Table table = gson.fromJson(request.getBody(), Table.class);
		int newTableId = 10; // Assuming new table ID
		Map<String, Integer> response = new HashMap<>();
		response.put("id", newTableId);
		String responseBody = gson.toJson(response);
		return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(responseBody);
	}


	private APIGatewayProxyResponseEvent createReservation(APIGatewayProxyRequestEvent request) {
		// Implement logic to create a reservation
		Reservation reservation = gson.fromJson(request.getBody(), Reservation.class);
		// Implement logic to create a reservation
		// Dummy implementation for demonstration purposes
		String newReservationId = "abc123"; // Assuming new reservation ID
		Map<String, String> response = new HashMap<>();
		response.put("reservationId", newReservationId);
		String responseBody = gson.toJson(response);
		return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(responseBody);
	}

	private APIGatewayProxyResponseEvent getReservations(APIGatewayProxyRequestEvent request) {
		// Implement logic to fetch reservations
		List<Reservation> reservations = new ArrayList<>();
		// Populate reservations with actual data
		// Dummy data for demonstration purposes
		reservations.add(new Reservation(101, "John Doe", "1234567890", "2024-06-01", "13:00", "15:00"));
		reservations.add(new Reservation(102, "Jane Smith", "0987654321", "2024-06-02", "18:00", "20:00"));
		String responseBody = gson.toJson(reservations);
		return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(responseBody);
	}
	private APIGatewayProxyResponseEvent badRequest() {
		// Create 400 Bad Request response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(400);
		response.setBody("Bad Request");
		return response;
	}
	private APIGatewayProxyResponseEvent badRequest(String message) {
		JsonObject responseBody = new JsonObject();
		responseBody.addProperty("message", message);
		return response(400, responseBody);
	}

	private APIGatewayProxyResponseEvent notFound() {
		// Create 404 Not Found response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(404);
		response.setBody("Not Found");
		return response;
	}

	private APIGatewayProxyResponseEvent successResponse() {
		// Create 200 OK response
		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		return response;
	}
}
