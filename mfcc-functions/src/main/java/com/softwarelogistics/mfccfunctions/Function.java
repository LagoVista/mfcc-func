package com.softwarelogistics.mfccfunctions;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpTrigger-Java". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTrigger-Java&code={your function key}
     * 2. curl "{your host}/api/HttpTrigger-Java?name=HTTP%20Query&code={your function key}"
     * Function Key is not needed when running locally, it is used to invoke function deployed to Azure.
     * More details: https://aka.ms/functions_authorization_keys
     */
    @FunctionName("mfcc/process")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST},
             authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<byte[]> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        com.softwarelogistics.MFCC mfcc = new com.softwarelogistics.MFCC();
    
        // Parse query parameter
        byte[] buffer = request.getBody();

         return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
    }
}
