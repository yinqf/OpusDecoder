package com.yinqf.opus;


import java.util.ArrayList;
import java.util.List;

/**
 * @Desc: wav头信息
 * @Author: yinqf【yinqf7437@gmail.com】
 * @Date: 2023-05-04 15:57
 */
public class WavHeader {
    private static final int CHANNEL_NUMS = 2;
    private static final int SAMPLE_RATE = 16000;
    private static final int BIT_PER_SAMPLE = 16;

    /**
     * 默认参数生成wav头信息
     *
     * @param inputSize raw PCM data
     * @return wav header
     */
    public static byte[] defaultValue(long inputSize) {
        return bytes(inputSize, CHANNEL_NUMS, SAMPLE_RATE, BIT_PER_SAMPLE);
    }

    /**
     * 生成wav头数据
     *
     * @param inputSize     raw PCM data
     *                      limit of file size for wave file: < 2^(2*4) - 36 bytes (~4GB)
     * @param channelCount  number of channels: 1 for mono, 2 for stereo, etc.
     * @param sampleRate    sample rate of PCM audio
     * @param bitsPerSample bits per sample, i.e. 16 for PCM16
     * @return wav header
     */
    public static byte[] bytes(long inputSize, int channelCount, int sampleRate, int bitsPerSample) {
        List<Byte> data = new ArrayList<>(44);
        // WAVE RIFF header
        // chunk id
        writeStr(data, "RIFF");
        // chunk size
        writeUInt(data, 36 + inputSize);
        // format
        writeStr(data, "WAVE");

        // SUB CHUNK 1 (FORMAT)
        // SUB CHUNK 1 id
        writeStr(data, "fmt ");
        // SUB CHUNK 1 size
        writeUInt(data, 16);
        // audio format (1 = PCM)
        writeUShort(data, (short) 1);
        // number of channelCount
        writeUShort(data, (short) channelCount);
        // sample rate
        writeUInt(data, sampleRate);
        // byte rate
        writeUInt(data, (long) sampleRate * channelCount * bitsPerSample / 8);
        // block align
        writeUShort(data, (short) (channelCount * bitsPerSample / 8));
        // bits per sample
        writeUShort(data, (short) bitsPerSample);

        // SUB CHUNK 2 (AUDIO DATA)
        writeStr(data, "data");
        // SUB CHUNK 2 size
        writeUInt(data, inputSize);
        byte[] byteArray = new byte[data.size()];
        int index = 0;
        for (Byte b : data) {
            byteArray[index++] = b;
        }
        return byteArray;
    }

    public static void writeStr(List<Byte> output, String data) {
        for (int i = 0; i < data.length(); i++) {
            output.add((byte) data.charAt(i));
        }
    }

    public static void writeUInt(List<Byte> output, long data) {
        output.add((byte) (data));
        output.add((byte) (data >> 8));
        output.add((byte) (data >> 16));
        output.add((byte) (data >> 24));
    }

    public static void writeUShort(List<Byte> output, short data) {
        output.add((byte) (data));
        output.add((byte) (data >> 8));
    }
}
