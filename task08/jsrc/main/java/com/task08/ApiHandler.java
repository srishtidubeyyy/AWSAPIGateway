package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.api.srishti.OpenMeteoAPI;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.RetentionSetting;
import java.io.IOException;
@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role",
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED,
        architecture = Architecture.X86_64,
        layers = {"weather-api-layer"}
)
@LambdaLayer(
        layerName = "weather-api-layer",
		libraries = "lib/apimeteo-1.0.jar")
@LambdaUrlConfig(

)
public class ApiHandler implements RequestHandler<Object, String> {
    private final OpenMeteoAPI openMeteoAPI;

    public ApiHandler() {
        openMeteoAPI = new OpenMeteoAPI();
    }

    public String handleRequest(Object request, Context context) {
        try {
            String weatherForecast = openMeteoAPI.getWeatherForecast();
            System.out.println("Weather forecast: " + weatherForecast);
            return weatherForecast;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "error in finding data";
        }
    }
}
