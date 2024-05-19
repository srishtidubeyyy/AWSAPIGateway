package com.task11.handler;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.task11.pojos.Reservations;
import com.task11.pojos.Tables;

import java.util.*;

public class ReservationsHandler {
    private final String ReservationsTable = "cmtr-cb6c2635-Reservations-test";
    private final String TablesTable = "cmtr-cb6c2635-Tables-test";


    public APIGatewayProxyResponseEvent postReservations(APIGatewayProxyRequestEvent request) {

        System.out.println("Post Reservations");
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
        DynamoDB dynamoDB = new DynamoDB(client);
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        System.out.println("Mapper created successfully");
        Gson gson = new Gson();
        Reservations reservationItem = gson.fromJson(request.getBody(), Reservations.class);

        DynamoDBQueryExpression<Tables> queryExpression = new DynamoDBQueryExpression<Tables>()
                .withIndexName("TableIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("#num = :val")
                .withExpressionAttributeNames(Collections.singletonMap("#num", "number"))
                .withExpressionAttributeValues(Collections.singletonMap(":val", new AttributeValue().withN(String.valueOf(reservationItem.getTableNumber()))));

        List<Tables> tableItems = mapper.query(Tables.class, queryExpression);
        System.out.println("TableItems : " + tableItems.toString());

        if (tableItems.isEmpty()) {
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }

        System.out.println("Checking Overlapping");
        ScanResult scanResult = client.scan(new ScanRequest().withTableName(ReservationsTable)); // Replace with the name of your 'Reservations' table
        System.out.println("Scanned successfully, checking for overlapping");
        for (Map<String, AttributeValue> item : scanResult.getItems()) {
            if (item.get("tableNumber").getN().equals(String.valueOf(reservationItem.getTableNumber())) &&
                    !(item.get("slotTimeEnd").getS().compareTo(reservationItem.getSlotTimeStart()) < 0 ||
                            item.get("slotTimeStart").getS().compareTo(reservationItem.getSlotTimeEnd()) > 0) &&
                    (item.get("date").getS().equals(reservationItem.getDate()))) {
                return new APIGatewayProxyResponseEvent().withStatusCode(400);
            }
        }

        String uuid = UUID.randomUUID().toString();
        reservationItem.setId(uuid);
        System.out.println("Saving Reservations");
        mapper.save(reservationItem);
        System.out.println("Returning");
        Map<String, String> responseStruct = new HashMap<>();
        responseStruct.put("reservationId", reservationItem.getId());
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(new Gson().toJson(responseStruct));

        return response;
    }


    public APIGatewayProxyResponseEvent getAllReservations() {
        AmazonDynamoDB ddb = AmazonDynamoDBClientBuilder.defaultClient();

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(ReservationsTable);

        ScanResult result = ddb.scan(scanRequest);
        List<Reservations> reservations = new ArrayList<>();

        for (Map<String, AttributeValue> item : result.getItems()) {
            Reservations reservation = new Reservations();
            reservation.setId(item.get("id").getS());
            reservation.setTableNumber(Integer.parseInt(item.get("tableNumber").getN()));
            reservation.setClientName(item.get("clientName").getS());
            reservation.setPhoneNumber(item.get("phoneNumber").getS());
            reservation.setDate(item.get("date").getS());
            reservation.setSlotTimeStart(item.get("slotTimeStart").getS());
            reservation.setSlotTimeEnd(item.get("slotTimeEnd").getS());

            reservations.add(reservation);
        }

        Map<String, List<Reservations>> responseStruct = new HashMap<>();
        responseStruct.put("reservations", reservations);

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody(new Gson().toJson(responseStruct));

        return response;
    }

}