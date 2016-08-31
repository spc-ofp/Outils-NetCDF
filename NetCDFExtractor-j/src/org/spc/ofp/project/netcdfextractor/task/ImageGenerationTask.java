/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class ImageGenerationTask extends Task<Image> {

    /**
     * The target directory.
     */
    private final Path file;
    private final String variablename;
    private final WritableImage image;

    private final boolean invertLat = true;

    /**
     * Creates a new instance.
     * @param file The source file.
     * @param variablename The target variable.
     * @param image The target image, may be {@code null}.
     * @throws NullPointerException If {@code file} or {@code variable} is {@code null}.
     * @throws IllegalArgumentException If {@code file} does not exist, cannot be read, or is not a file.
     */
    public ImageGenerationTask(final Path file, final String variablename, final WritableImage image) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(file);
        if (!Files.exists(file) || !Files.isReadable(file) || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException();
        }
        this.file = file;
        this.variablename = variablename;
        this.image = image;
    }

    @Override
    protected Image call() throws Exception {
        final String absoluteFilename = file.toAbsolutePath().toString();
        final String filename = file.getFileName().toString();
        try (final NetcdfFile netcdf = NetcdfFile.open(absoluteFilename)) {
            final Variable variable = netcdf.findVariable(variablename);
            final int rank = variable.getRank();
            System.out.println(variable.getDataType());
            variable.getAttributes().stream().forEach(System.out::println);
            // Extract meta-data from the variable.
            final float missingValue = getVariableAttributeValue(variable, "missing_value", -Float.MAX_VALUE);
            final float fillValue = getVariableAttributeValue(variable, "_FillValue", Float.MAX_VALUE);
            final float scaleFactor = getVariableAttributeValue(variable, "scale_factor", 1);
            final float add_offset = getVariableAttributeValue(variable, "add_offset", 0);
            final float validMin = getVariableAttributeValue(variable, "valid_min", -Float.MAX_VALUE);
            final float validMax = getVariableAttributeValue(variable, "valid_max", Float.MAX_VALUE);
            // Extract dimensions from the variable.
            final int[] shape = variable.getShape();
            final int xlon = shape[rank - 1];
            final int ylat = shape[rank - 2];
            final Array array = variable.read();
            final Index index = array.getIndex();
            float min = missingValue;
            float max = missingValue;
            for (int y = 0; y < ylat; y++) {
                index.setDim(rank - 2, y);
                for (int x = 0; x < xlon; x++) {
                    index.setDim(rank - 1, x);
                    final float sourceValue = array.getFloat(index);
                    if (sourceValue != fillValue && sourceValue != missingValue) {
                        final float value = sourceValue * scaleFactor + add_offset;
                        if (min == missingValue) {
                            min = value;
                            max = value;
                        } else {
                            min = Math.min(min, value);
                            max = Math.max(max, value);
                        }
                    }
                }
            }
            // Allocate image.
            final int imageScale = 3;
            final int imageWidth = xlon * imageScale;
            final int imageHeight = ylat * imageScale;
            final boolean createNewImage = (image == null || (image.getWidth() != imageWidth && image.getHeight() != imageHeight));
            System.out.printf("Image: %d x %d", imageWidth, imageHeight);
            final WritableImage result = (createNewImage) ? new WritableImage(imageWidth, imageHeight) : image;
            final PixelWriter writer = result.getPixelWriter();
            for (int y = 0; y < ylat; y++) {
                index.setDim(rank - 2, y);
                for (int x = 0; x < xlon; x++) {
                    index.setDim(rank - 1, x);
                    final float sourceValue = array.getFloat(index);
                    if (sourceValue != fillValue && sourceValue != missingValue) {
                        final float value = sourceValue * scaleFactor + add_offset;
//                                    final int rgb = makeColor(value, validMin, validMax);
                        final int rgb = computeColor(value, min, max);
                        final Color color = new Color(rgb);
                        final int imageX = imageScale * x;
                        final int imageY = imageScale * ((invertLat) ? ylat - 1 - y : y);
                        writer.setArgb(imageX, imageY, rgb);
                        for (int a = 0; a < imageScale; a++) {
                            for (int b = 0; b < imageScale; b++) {
                                writer.setArgb(imageX + a, imageY + b, rgb);
                            }
                        }
                    }
                }
            }
            return result;
        }
    }

    private float getVariableAttributeValue(final Variable variable, final String attributeName, final float defaultValue) {
        float result = defaultValue;
        final Attribute attribute = variable.findAttribute(attributeName);
        if (attribute != null) {
            result = attribute.getNumericValue().floatValue();
        } else {
            final String message = String.format("Could not locate attribute \"%s\" in variable \"%s\", using default value.", attributeName, variable.getShortName());
            Logger.getLogger(getClass().getName()).warning(message);
        }
        return result;
    }

    private int computeColor(final float value, final float minValue, final float maxValue) {
        if (!(minValue <= value && value <= maxValue)) {
            final String message = String.format("%f [%f - %f]", value, minValue, maxValue);
            System.err.println(message);
            System.exit(1);
        }
        final float hue = (240f / 360f) * (1 - (value - minValue) / (maxValue - minValue));
        final float saturation = 1;
        final float brightness = 1;
        final int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        return rgb;
    }
}
