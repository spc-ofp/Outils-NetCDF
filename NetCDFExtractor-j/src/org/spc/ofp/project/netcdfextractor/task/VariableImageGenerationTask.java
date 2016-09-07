/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.awt.Color;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Generate a preview image from a variable.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class VariableImageGenerationTask extends Task<Image> {

    /**
     * The target directory.
     */
    private final Path file;
    private final String variableName;
    private final WritableImage image;

    private final boolean invertLat = true;

    /**
     * Creates a new instance.
     * @param file The source file.
     * @param variableName The target variable.
     * @param image The target image, may be {@code null}.
     * @throws NullPointerException If {@code file} or {@code variableName} is {@code null}.
     * @throws IllegalArgumentException If {@code file} does not exist, cannot be read, or is not a file.
     */
    public VariableImageGenerationTask(final Path file, final String variableName, final WritableImage image) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(variableName);
        if (!Files.exists(file) || !Files.isReadable(file) || !Files.isRegularFile(file)) {
            throw new IllegalArgumentException();
        }
        this.file = file;
        this.variableName = variableName;
        this.image = image;
    }

    @Override
    protected Image call() throws Exception {
        final String absoluteFilename = file.toAbsolutePath().toString();
        final String filename = file.getFileName().toString();
        try (final NetcdfFile netcdf = NetcdfFile.open(absoluteFilename)) {
            final Variable variable = netcdf.findVariable(variableName);
            final int rank = variable.getRank();
//            System.out.println(variable.getDataType());
//            variable.getAttributes().stream().forEach(System.out::println);
            // Extract meta-data from the variable.
            final float missingValue = NetCDFUtils.INSTANCE.getNumericAttribute(variable, "missing_value", Float.NaN).floatValue();
            final float fillValue = NetCDFUtils.INSTANCE.getNumericAttribute(variable, "_FillValue", Float.NaN).floatValue();
            final float scaleFactor = NetCDFUtils.INSTANCE.getNumericAttribute(variable, "scale_factor", 1).floatValue();
            final float add_offset = NetCDFUtils.INSTANCE.getNumericAttribute(variable, "add_offset", 0).floatValue();
            final float validMin = NetCDFUtils.INSTANCE.getNumericAttribute(variable, "valid_min", Float.NaN).floatValue();
            final float validMax = NetCDFUtils.INSTANCE.getNumericAttribute(variable, "valid_max", Float.NaN).floatValue();
            // Extract dimensions from the variable.
            final int[] shape = variable.getShape();
            final int xlon = shape[rank - 1];
            final int ylat = shape[rank - 2];
            final Array array = variable.read();
            final Index index = array.getIndex();
            float min = validMin;
            float max = validMax;
            if (Float.isNaN(min) || Float.isNaN(max)) {
                for (int y = 0; y < ylat; y++) {
                    index.setDim(rank - 2, y);
                    for (int x = 0; x < xlon; x++) {
                        index.setDim(rank - 1, x);
                        final float sourceValue = array.getFloat(index);
                        if (!Float.isNaN(sourceValue) && sourceValue != fillValue && sourceValue != missingValue) {
                            final float value = sourceValue * scaleFactor + add_offset;
                            if (Float.isNaN(min) || Float.isNaN(max)) {
                                min = value;
                                max = value;
                            } else {
                                min = Math.min(min, value);
                                max = Math.max(max, value);
                            }
                        }
                    }
                }
            }
            if (Float.isNaN(min) || Float.isNaN(max)) {
                throw new IllegalArgumentException("Cannot compute the [min, max] range of values.");
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
                    if (!Float.isNaN(sourceValue) && sourceValue != fillValue && sourceValue != missingValue) {
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

    private int computeColor(final float value, final float minValue, final float maxValue) throws IllegalArgumentException {
        if (!(minValue <= value && value <= maxValue)) {
            final String message = String.format("%f [%f - %f]", value, minValue, maxValue);
            throw new IllegalArgumentException(message);
        }
        final float hue = (240f / 360f) * (1 - (value - minValue) / (maxValue - minValue));
        final float saturation = 1;
        final float brightness = 1;
        final int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        return rgb;
    }
}
