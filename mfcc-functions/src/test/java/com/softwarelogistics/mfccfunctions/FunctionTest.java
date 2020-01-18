package com.softwarelogistics.mfccfunctions;

import com.microsoft.azure.functions.*;
import com.softwarelogistics.AudioInputFile;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.AudioFileFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for Function class.
 */
public class FunctionTest {
    /**
     * Unit test for HttpTriggerJava method.
     */
    @Test
    public void testHttpTriggerJava() throws Exception {
        // Setup
        @SuppressWarnings("unchecked")
        final HttpRequestMessage<byte[]> req = mock(HttpRequestMessage.class);

        Path path = Paths.get(".\\src\\test\\uas\\uas0.wav");

        byte[] buffer = Files.readAllBytes(path);

        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("mels", "256");
        queryParams.put("mfcc", "80");
        doReturn(queryParams).when(req).getQueryParameters();
        doReturn(buffer).when(req).getBody();

        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));

        final ExecutionContext context = mock(ExecutionContext.class);
        doReturn(Logger.getGlobal()).when(context).getLogger();

        // Invoke
        final HttpResponseMessage ret = new Function().run(req, context);

        short[] responseBody = (short[]) ret.getBody();
        assertEquals(33, responseBody[1]);
        assertEquals(80, responseBody[2]);
        assertEquals(responseBody.length - 6, responseBody[0]);

        System.out.println(String.format(">>>>>> Response=[%d] Width=[%d] Height[%d]", responseBody[0], responseBody[1], responseBody[2]));

        // Verify
        assertEquals(ret.getStatus(), HttpStatus.OK);
    }
}
