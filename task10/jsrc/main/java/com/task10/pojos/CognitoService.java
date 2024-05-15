package com.task10.pojos;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import java.util.Map;
import java.util.function.Consumer;

public class CognitoService {
    private final CognitoIdentityProviderClient cognitoClient;

    public CognitoService() {
        this.cognitoClient = CognitoIdentityProviderClient.create();
    }

    public void signUpUser(String email, String password) {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId("your-client-id")
                .username(email)
                .password(password)
                .build();

        SignUpResponse signUpResponse = cognitoClient.signUp(signUpRequest);
        // Handle response if necessary
    }

    public String signInUser(String email, String password) {
        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId("your-client-id")
                .authFlow("USER_PASSWORD_AUTH")
                .authParameters(Map.of(
                        "USERNAME", email,
                        "PASSWORD", password
                ))
                .build();

        InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
        AuthenticationResultType authenticationResult = authResponse.authenticationResult();
        return authenticationResult.accessToken();
    }
}
