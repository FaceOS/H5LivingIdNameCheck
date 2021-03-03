package com.renren.faceos.h5.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * @auther pengjiaxin@faceos.com
 * @description
 * @date 2019/8/15
 */
public class Base64Utils {


    /**
     * 功能描述：bufferedImage转base64
     *
     * @param bufferedImage
     * @return java.lang.String
     * @author pengjiaxin@faceos.com
     * @date 2019/11/18
     */
    public static String imageToBase64(BufferedImage bufferedImage) {
        Base64.Encoder encoder = Base64.getEncoder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encoder.encodeToString(byteArrayOutputStream.toByteArray());
    }


    public static String resizeImageTo40K(String base64Img) {
        try {
            BufferedImage src = base64String2BufferedImage(base64Img);
            BufferedImage output = Thumbnails.of(src).size(src.getWidth() / 3, src.getHeight() / 3).asBufferedImage();
            String base64 = imageToBase64(output);
            if (base64.length() - base64.length() / 8 * 2 > 40000) {
                output = Thumbnails.of(output).scale(1 / (base64.length() / 40000)).asBufferedImage();
                base64 = imageToBase64(output);
            }
            return base64;
        } catch (Exception e) {
            return base64Img;
        }
    }


    private static InputStream BaseToInputStream(String base64string) {
        ByteArrayInputStream stream = null;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes1 = decoder.decodeBuffer(base64string);
            stream = new ByteArrayInputStream(bytes1);
        } catch (Exception e) {
            // TODO: handle exception
        }
        return stream;
    }

    public static BufferedImage base64String2BufferedImage(String base64string) {
        BufferedImage image = null;
        try {
            InputStream stream = BaseToInputStream(base64string);
            image = ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage imageScaleZoom(MultipartFile file) {
        try {
            /*
             * size(width,height) 若图片横比400小，高比500小，不变
             * 若图片横比400小，高比500大，高缩小到400，图片比例不变 若图片横比400大，高比500小，横缩小到400，图片比例不变
             * 若图片横比400大，高比500大，图片按比例缩小，横为200或高为300
             */
            BufferedImage bufferedImage = Thumbnails
                    .of(file.getInputStream())
                    .scale(0.25f)
                    .asBufferedImage();
            return bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
