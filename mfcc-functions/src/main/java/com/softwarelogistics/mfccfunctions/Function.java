package com.softwarelogistics.mfccfunctions;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.softwarelogistics.AudioInputFile;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * https://docs.microsoft.com/en-us/azure/azure-functions/functions-create-first-java-maven
     * Need to install
     *   - JDK (> 8 should be fine, tested with 13) https://aka.ms/azure-jdks or pull from Oracle
     *   - Apache Maven (and add to path) https://maven.apache.org/
     *   - Azure CLI  https://docs.microsoft.com/en-us/cli/azure
     *   - Azure Functions Core Tools https://docs.microsoft.com/en-us/azure/azure-functions/functions-run-local#v2
     *
     *   Make sure you az login
     *
     *   mvn clean package will run tests.
     *   mvn azure-functions:run will test local (didn't work well but didn't really debug)
     *   mvn azure-functions:deploy
     */
    @FunctionName("mfcc/process")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST},
             authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<byte[]> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        byte[] buffer = request.getBody();

        try {
            AudioInputFile inputFile = AudioInputFile.fromByteArray(buffer);

            Map<String, String> params = request.getQueryParameters();

            int mels = 128,  mfcc = 20, fft = 2048, hopLength = 512;

            if(params.containsKey("mels"))
                mels = Integer.parseInt(params.get("mels"));

            if(params.containsKey("mfcc"))
                mfcc = Integer.parseInt(params.get("mfcc"));

            if(params.containsKey("fft"))
                mfcc = Integer.parseInt(params.get("fft"));

            if(params.containsKey("hoplength"))
                mfcc = Integer.parseInt(params.get("hoplength"));

            com.softwarelogistics.MFCC processor = new com.softwarelogistics.MFCC(mels, mfcc, fft, hopLength, inputFile.bitRate);
            short[] response = processor.process(inputFile.content);

            short[] finalOutput = new short[response.length + 6];
            finalOutput[0] = (short)response.length;
            finalOutput[1] = (short)processor.width;
            finalOutput[2] = (short)processor.height;
            for(int idx = 0; idx < response.length; ++idx){
                finalOutput[idx + 3] = response[0];
            }

            System.out.println(String.format(">>>>>> Shape Width=[%d] Height[%d] mels [%d]", processor.width, processor.height, mels));

            return request.createResponseBuilder(HttpStatus.OK).body(finalOutput).build();

        } catch (Exception e) {
            e.printStackTrace();
        }

         System.out.println(String.format("Passed in buffer of size [%d]", buffer.length));

        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Could not process request.").build();
    }
}
