package com.softwarelogistics.mfccfunctions;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.HttpResponseMessage;
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
    @FunctionName("process")
    @HttpOutput(name="$return", dataType = "binary")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST},
             authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<byte[]> request,
            final ExecutionContext context)
        {
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

            double[] ins = inputFile.readDoubleContent();
            for(int e = 0; e < 50; ++e) {
                System.out.print(String.format("%d. %.5f, ",e, ins[e]));
            }

            System.out.println();
            System.out.println();

            double[][] dbls = processor.dctMfcc(inputFile.readDoubleContent());
            for(int y = 0; y < dbls.length; ++y) {
                System.out.print(String.format("%d. ", y));

                for (int x = 0; x < dbls.length; ++x) {
                    System.out.print(String.format("%.2f, ", dbls[y][x]));
                }
                System.out.println("");
            }


            short[] finalOutput = new short[response.length + 6];
            finalOutput[0] = (short)response.length;
            finalOutput[1] = (short)processor.width;
            finalOutput[2] = (short)processor.height;
            for(int idx = 0; idx < response.length; ++idx){
                finalOutput[idx + 3] = response[idx];
            }

            System.out.println(String.format(">>>>>> Shape Width=[%d] Height[%d] mels [%d]", processor.width, processor.height, mels));

            return request.createResponseBuilder(HttpStatus.OK).body(finalOutput).header("is_raw","true").header("Content-Type", "application/octet-stream").header("Content-Length", String.valueOf(finalOutput.length)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()).build();
        }
    }
}
