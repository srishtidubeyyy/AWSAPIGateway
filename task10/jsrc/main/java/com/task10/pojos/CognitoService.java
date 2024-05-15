package com.task10.pojos;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CognitoService {
    private final CognitoIdentityProviderClient cognitoClient;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public String signUp(String username, String email, String password) {
        // Check if email format is valid
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Check if password format is valid
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Invalid password format");
        }

        SignUpResponse response = cognitoClient.signUp(SignUpRequest.builder()
                .clientId("yourClientId")
                .username(username)
                .password(password)
                .userAttributes(
                        AttributeType.builder()
                                .name("email")
                                .value(email)
                                .build())
                .build());

        return response.userSub(); // Return the user sub (unique identifier)
    }

    public String signIn(String username, String password) {
        try {
            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(InitiateAuthRequest.builder()
                    .clientId("yourClientId")
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .authParameters(
                            Collections.singletonMap("USERNAME", username)
                    )
                    .build());

            return authResponse.authenticationResult().accessToken();
        } catch (NotAuthorizedException e) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    private boolean isValidEmail(String email) {
        // Use a regex pattern to validate email format
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidPassword(String password) {
        // Check if password length is at least 8 characters
        // You can add more password complexity rules as needed
        return password.length() >= 8;
    }
}
