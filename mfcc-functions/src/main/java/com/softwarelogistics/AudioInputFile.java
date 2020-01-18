package com.softwarelogistics;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AudioInputFile
{
    public int totalDataLen;
    public int sizeOfChunk;
    public int channels;
    public int bitRate;
    public int bytesPerSecond;
    public int totalAudioLength;
    public int sampleDuration;
    public int sampleSize;
    public short[] content;


    private int readInt(DataInputStream rdr) throws IOException {
        return rdr.readByte() & 0xff + ((rdr.readByte() & 0xff) << 8) + ((rdr.readByte() & 0xff) << 16)+ ((rdr.readByte() & 0xff) << 24);
    }

    private short readShort(DataInputStream rdr) throws IOException {
        return (short)(rdr.readByte() & 0xff + ((rdr.readByte() & 0xff) << 8));
    }

    private void confirmString(DataInputStream rdr, String str) throws Exception {
        for(int idx = 0; idx < str.length(); ++idx)
        {
            if(str.charAt(idx) != rdr.readByte())
            {
                throw  new Exception(String.format("Expected %c", str.charAt(idx)));
            }
        }
    }

    public static AudioInputFile readFile(Path path) throws Exception{
        byte[] buffer = Files.readAllBytes(path);
        return AudioInputFile.fromByteArray(buffer);
    }

    public static AudioInputFile fromByteArray(byte[] buffer) throws Exception {
        AudioInputFile inputFile = new AudioInputFile();
        inputFile.read(buffer);
        return  inputFile;
    }

    public void read(byte[] buffer) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(buffer);

        sampleSize = ((int)(buffer.length - 44)) / 2;

        DataInputStream rdr = new DataInputStream(inputStream);

        confirmString(rdr, "RIFF");

        totalDataLen = rdr.readByte() & 0xff + ((rdr.readByte() & 0xff) << 8) + ((rdr.readByte() & 0xff) << 16)+ ((rdr.readByte() & 0xff) << 24);

        confirmString(rdr, "WAVEfmt ");

        sizeOfChunk = readInt(rdr);

        if (rdr.readByte() != 1)
            throw new Exception("Not PCM format");

        rdr.readByte();

        channels = rdr.readByte();
        rdr.readByte();

        bitRate = readInt(rdr);
        bytesPerSecond = readInt(rdr);

        for(int idx = 0; idx < 4; ++idx)
            rdr.readByte();

        confirmString(rdr,"data");

        totalAudioLength = readInt(rdr);

        content = new short[sampleSize];
        for(int idx = 0; idx < sampleSize; ++idx)
        {
            content[idx] = readShort(rdr);
        }

        rdr.close();
        inputStream.close();
    }

    public float[] readFloatContent() {
        float wavContent[] = new float[sampleSize];
        float scaler = (float)(Short.MAX_VALUE) / 6;

        for(int idx = 0; idx < sampleSize; ++idx){
            wavContent[idx] = content[idx] / scaler;
        }

        return wavContent;
    }

    public short[] toContent() {
        return content;
    }

    public double[] readDoubleContent() {
        double wavContent[] = new double[sampleSize];
        float scaler = (float)(Short.MAX_VALUE) / 6;

        for(int idx = 0; idx < sampleSize; ++idx){
            wavContent[idx] = content[idx] / scaler;
        }

        return wavContent;
    }
}