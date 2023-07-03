package com.encentral.inverter.impl.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageInverterActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ImagePath.class, this::invertImage)
                .build();
    }

    private void invertImage(ImagePath imagePath) {
        System.out.println("\n\nConverting Image");

        BufferedImage image;
        File file;

        try {
            file = new File(imagePath.getImagePath());
            image = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("Error reading image");
            return;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int pixel = image.getRGB(x, y);
                Color color = new Color(pixel, true);

                color = new Color(
                        color.getGreen(),
                        color.getBlue(),
                        color.getRed()
                );

//                color = new Color(
//                        255 - color.getRed(),
//                        255 - color.getGreen(),
//                        255 - color.getBlue()
//                );

                int invertedPixel = color.getRGB();
                image.setRGB(x, y, invertedPixel);
            }
        }

        try {
            ImageIO.write(
                    image,
                    getFileExtension(imagePath.getImagePath()),
                    new File(file.getParent(), imagePath.getOutputFilename())
            );
        } catch (IOException e) {
            System.out.println("Error writing output");
            return;
        }
        System.out.println("Conversion Completed");
        System.out.println(imagePath.getOutputFilename());
        System.out.println("\n\n");
    }

    private static String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < filePath.length() - 1) {
            return filePath.substring(dotIndex + 1).toLowerCase();
        } else {
            return "png"; // Default to PNG format if the extension is not found
        }
    }

    public static Props create() {
        return Props.create(ImageInverterActor.class);
    }

    public static final class ImagePath {
        private String imagePath;
        private String outputFilename;

        public ImagePath(String imagePath, String outputFilename) {
            this.imagePath = imagePath;
            this.outputFilename = outputFilename;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public String getOutputFilename() {
            return outputFilename;
        }

        public void setOutputFilename(String outputFilename) {
            this.outputFilename = outputFilename;
        }
    }
}
