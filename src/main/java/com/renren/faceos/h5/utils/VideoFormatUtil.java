package com.renren.faceos.h5.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import ws.schild.jave.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @ClassName VideoFormatUtil
 * @Description 视频格式转换工具
 * @Author geekcjj
 * @Date 2020年12月13日 上午10:52:28
 */
public class VideoFormatUtil {

    public static Logger Log = LoggerFactory.getLogger(VideoFormatUtil.class);

    private static File transferToFile(MultipartFile multipartFile) {
//        选择用缓冲区来实现这个转换即使用java 创建的临时文件 使用 MultipartFile.transferto()方法 。
        File file = null;
        try {
            file = File.createTempFile("temp", ".mp4");
            multipartFile.transferTo(file);
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 传视频File对象，返回压缩后File对象信息
     *
     * @param file
     */
    public static File compressionVideo(MultipartFile file, String picName) {
//        File source = null;
//        File target = null;
        File source = transferToFile(file);
        File target = new File("d:" + File.separator + picName);
        long time = System.currentTimeMillis();
        try {
            MultimediaObject object = new MultimediaObject(source);
            AudioInfo audioInfo = object.getInfo().getAudio();
            //  视频属性设置
            int maxBitRate = 128000;
            int maxSamplingRate = 44100;
            int bitRate = 800000;
            int maxFrameRate = 20;
            int maxWidth = 1280;
            AudioAttributes audio = new AudioAttributes();
            // 设置通用编码格式
            audio.setCodec("aac");
            // 设置最大值：比特率越高，清晰度/音质越好
            // 设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 128000 = 128kb)
            if (audioInfo.getBitRate() > maxBitRate) {
                audio.setBitRate(new Integer(maxBitRate));
            }
            // 设置重新编码的音频流中使用的声道数（1 =单声道，2 = 双声道（立体声））。如果未设置任何声道值，则编码器将选择默认值 0。
            audio.setChannels(audioInfo.getChannels());
            // 设置编码时候的音量值，未设置为0,如果256，则音量值不会改变
            // audio.setVolume(256);
            if (audioInfo.getSamplingRate() > maxSamplingRate) {
                audio.setSamplingRate(maxSamplingRate);
            }
            //  视频编码属性配置
            VideoInfo videoInfo = object.getInfo().getVideo();
            VideoAttributes video = new VideoAttributes();
            video.setCodec("h264");
            // 设置音频比特率,单位:b (比特率越高，清晰度/音质越好，当然文件也就越大 800000 = 800kb)
            if (videoInfo.getBitRate() > bitRate) {
                video.setBitRate(bitRate);
            }
            // 视频帧率：15 f / s 帧率越低，效果越差
            // 设置视频帧率（帧率越低，视频会出现断层，越高让人感觉越连续），视频帧率（Frame rate）是用于测量显示帧数的量度。所谓的测量单位为每秒显示帧数(Frames per Second，简：FPS）或“赫兹”（Hz）。
            if (videoInfo.getFrameRate() > maxFrameRate) {
                video.setFrameRate(maxFrameRate);
            }
            // 限制视频宽高
            int width = videoInfo.getSize().getWidth();
            int height = videoInfo.getSize().getHeight();
            if (width > maxWidth) {
                float rat = (float) width / maxWidth;
                video.setSize(new VideoSize(maxWidth, (int) (height / rat)));
            }
            EncodingAttributes attr = new EncodingAttributes();
            attr.setFormat("mp4");
            attr.setAudioAttributes(audio);
            attr.setVideoAttributes(video);
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attr);
            System.out.println("压缩总耗时：" + (System.currentTimeMillis() - time) / 1000);
            return target;
        } catch (Exception e) {
            System.out.println("压缩失败");
        } finally {
            if (target.length() > 0) {
                source.delete();
            }
        }
        return source;
    }

    public static void main(String[] args) {
        //System.out.println("数据 = [" + getVideoProcessImage("E:/MyFile/mylove/video/dPQRVRFuZHJ8.mp4","E:/MyFile/sfaafasddxg.jpg") + "]");
        //System.out.println("数据 = [" + getVideoInfo("E:/MyFile/mylove/video/dPQRVRFuZHJ8.mp4") + "]");
        //System.out.println("数据 = [" + videoToAudio("E:/MyFile/mylove/video/dPQRVRFuZHJ8.mp4","E:/MyFile/sfaafasddxgd.mp3") + "]");
    }

}