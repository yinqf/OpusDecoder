package com.yinqf.opus;

import org.concentus.OpusDecoder;
import org.concentus.OpusException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OpusMain {
    //是否存在文件头信息（默认存在）
    private final static boolean mWithFrameHead = true;
    //声道数(默认单声道)
    private final static int channels = 1;
    //采样率(默认16K)
    private final static int sampleRate = 16000;
    //单次解包大小(默认40
    private static int sample = 40;

    public static void main(String[] args) throws IOException, OpusException {
        //存在头信息，则解包大小+1
        if(mWithFrameHead){
            sample = sample + 1;
        }
        //解码文件路径
        String sourceFilePath = "/Users/yinqf/Downloads/test0504.opus";
        byte[] opusBytes = Files.readAllBytes(Paths.get(sourceFilePath));
        System.out.println("解码文件大小:"+opusBytes.length);
        //输出文件路径
        FileOutputStream fileOut = new FileOutputStream("/Users/yinqf/Downloads/test3.wav");

        //先写入44字节wav头信息
        byte[] wavHeader = WavHeader.bytes(opusBytes.length * 12L, channels, sampleRate, 16);
        fileOut.write(wavHeader);

        //wav缓存大小
        byte[] wavBuffer = new byte[sampleRate * channels * 2];

        //音频总时长
        int totalTimes = 0;
        //创建解码对象
        OpusDecoder decoder = new OpusDecoder(sampleRate, channels);
        //临时存储解码数据
        List<Byte> tempBytes = new ArrayList<>();
        for(Byte b : opusBytes){
            tempBytes.add(b);
            if(tempBytes.size() >= sample * 2){
                //每次取出sample大小的数据包
                byte[] data_packet = new byte[(sample -1) * 2];
                //去掉2字节头信息
                for(int i = 2; i < tempBytes.size(); i++){
                    data_packet[i-2] = tempBytes.get(i);
                }
                try {
                    //开始解码
                    int samplesDecoded = decoder.decode(data_packet, 0, data_packet.length,
                            wavBuffer, 0, sampleRate / 2, false);
                    //解码成功，写入数据
                    fileOut.write(wavBuffer, 0, samplesDecoded * 2);
                    totalTimes += samplesDecoded;
                    //打印解码数据大小
                    System.out.println(samplesDecoded);
                    //清空缓存
                    tempBytes = new ArrayList<>();
                } catch (OpusException e) {
                    //解码失败，移除头信息的第一个字节，继续解码
                    System.out.println("errorMsg+:"+e.getMessage());
                    tempBytes.remove(0);
                }
            }
        }

        System.out.println("音频总时长（秒）: " + (totalTimes / (float) sampleRate));
        fileOut.close();
    }
}

