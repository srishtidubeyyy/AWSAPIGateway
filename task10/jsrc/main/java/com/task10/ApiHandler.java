package com.task10;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.google.gson.Gson;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;
import com.task10.pojos.Reservation;
import com.task10.pojos.SignInRequest;
import com.task10.pojos.SignUpRequest;
import com.task10.pojos.Table;
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
		// Implement sign-up logic
		SignUpRequest signUpRequest = gson.fromJson(request.getBody(), SignUpRequest.class);
		return null;
	}

	private APIGatewayProxyResponseEvent signIn(APIGatewayProxyRequestEvent request) {
		// Implement sign-in logic
		SignInRequest signInRequest = gson.fromJson(request.getBody(), SignInRequest.class);
		return null;
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

//	private APIGatewayProxyResponseEvent getTable(String tableId) {
//        tableId = request.getPathParameters().get("tableId");
//		// Implement logic to fetch table by ID
//		// Dummy implementation for demonstration purposes
//		Table table = new Table(Integer.parseInt(tableId), 101, 4, false, 50);
//		String responseBody = gson.toJson(table);
//		return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(responseBody);
//	}

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
