package com.joechang.loco.utils;

import com.joechang.loco.response.ServerException;
import org.apache.catalina.Server;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.MediaType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Author:    joechang
 * Created:   6/10/15 5:49 PM
 * Purpose:   Some general purpose methods for manipulating images.  Resizing, cropping, etc.
 */
public class ImageUtils {
    public enum Shape {
        CIRCLE,
        SQUARE,
        ROUNDED,
        NONE
    }

    private final static String DEFAULT_MAP_POINTER = "public/img/map-pointer50x69.png";
    private final static BufferedImage MAP_POINTER = ImageUtils.fileStringToImage(DEFAULT_MAP_POINTER);

    public static BufferedImage scaleImage(BufferedImage image, double percentage, Shape shape) {
        int width = (int) Math.round(image.getWidth() * percentage);
        int height = (int) Math.round(image.getHeight() * percentage);

        Image tmp = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = newImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        if (shape != null && shape != Shape.NONE) {
            int shortestSide = width < height ? width : height;
            g2.setComposite(AlphaComposite.Src);
            g2.setColor(Color.WHITE);

            switch (shape) {
                case CIRCLE:
                    g2.fill(new Ellipse2D.Float(0, 0, shortestSide, shortestSide));
                    break;
                case ROUNDED:
                    g2.fill(new RoundRectangle2D.Float(0, 0, shortestSide, shortestSide, shortestSide * .20f, shortestSide * .20f));
                    break;
                case SQUARE:
                    g2.fill(new Rectangle2D.Float(0, 0, shortestSide, shortestSide));
                    break;
            }

            g2.setComposite(AlphaComposite.SrcAtop);
        }

        g2.drawImage(tmp, 0, 0, null);
        g2.dispose();

        return newImage;
    }

    public static BufferedImage mapPointer(BufferedImage avatar, int height) {
        return frameWithinPointer(avatar, MAP_POINTER, height);
    }

    protected static BufferedImage frameWithinPointer(BufferedImage avatar, BufferedImage pointer, int height) {
        double scale = findRoughScale(avatar, 50);
        BufferedImage scaledAvatar = scaleImage(avatar, scale, Shape.CIRCLE);
        BufferedImage combined = new BufferedImage(pointer.getWidth(), pointer.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics g = combined.getGraphics();
        g.drawImage(pointer, 0, 0, null);
        g.drawImage(scaledAvatar, 3, 3, null);
        g.dispose();

        return scaleImageTo(combined, height);
    }


    public static BufferedImage scaleImageTo(BufferedImage image, int targetPixelsForLongestSide) {
        double scaleTo = findRoughScale(image, targetPixelsForLongestSide);
        return scaleImage(image, scaleTo, null);
    }

    public static double findRoughScale(BufferedImage image, int targetPixels) {
        return targetPixels * 1.0d / getLongestSide(image);
    }

    private static int getShortestSide(BufferedImage image) {
        int oldWidth = image.getWidth();
        int oldHeight = image.getHeight();
        return oldWidth < oldHeight ? oldWidth : oldHeight;
    }

    private static int getLongestSide(BufferedImage image) {
        int oldWidth = image.getWidth();
        int oldHeight = image.getHeight();
        return oldWidth > oldHeight ? oldWidth : oldHeight;
    }

    public static byte[] imageToByteArray(BufferedImage image, String type) {
        byte[] ret = new byte[]{};
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, type, baos);
        } catch (IOException ioe) {
            //Eat it, return empty array.
        } finally {
            IOUtils.closeQuietly(baos);
        }
        return baos.toByteArray();
    }

    public static BufferedImage urlStringToImage(String url) {
        try {
            URL u = new URL(url);
            return urlToImage(u);
        } catch (MalformedURLException mue) {
            throw new ServerException("URL not valid.");
        }
    }

    public static BufferedImage urlToImage(URL u) {
        try {
            BufferedImage img = ImageIO.read(u);
            return img;
        } catch (IOException ioe) {
            throw new ServerException("Could not read URL.");
        }
    }

    public static BufferedImage fileStringToImage(String filename) {
        try {
            File f = new File(filename);
            BufferedImage img = ImageIO.read(f);
            return img;
        } catch (IOException ioe) {
            throw new ServerException("Could not read File");
        }
    }

    /**
     * Such a hack, but works for now.  To do it via magic bytes is hellish.
     * @param url
     * @return extension of file, assume three chars after the last period in the url.
     */
    public static String determineImageType(String url) {
        int idx = url.lastIndexOf('.') + 1;
        return url.substring(idx, idx + 3);
    }

    public static MediaType mediaTypeFromExtension(String ext) {
        switch (ext.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "png":
                return MediaType.IMAGE_PNG;
            default:
                return null;
        }
    }

}
