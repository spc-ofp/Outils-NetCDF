/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javafx.concurrent.Task;
import javax.imageio.ImageIO;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class BatchConvertToImageTask extends Task<Void> {

    /**
     * The target directory.
     */
    private final Path sourceDir;

    private final boolean invertLat = true;

    /**
     * Creates a new instance.
     * @param sourceDir The source directory.
     * @throws NullPointerException If {@code sourceDir} is {@code null}.
     * @throws IllegalArgumentException If {@code sourceDir} does not exist or is not a directory.
     */
    public BatchConvertToImageTask(final Path sourceDir) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(sourceDir);
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException();
        }
        this.sourceDir = sourceDir;
    }

    @Override
    protected Void call() throws Exception {
        generatePaletteImage();
        final List<Path> files = walkFolder(sourceDir.toFile());
        for (final Path file : files) {
            final String absoluteFilename = file.toAbsolutePath().toString();
            final String filename = file.getFileName().toString();
            try (final NetcdfFile netcdf = NetcdfFile.open(absoluteFilename)) {
                final List<Variable> variables = netcdf.getVariables();
                for (final Variable variable : variables) {
                    final int rank = variable.getRank();
                    if (rank == 1) {
                        System.out.printf("Skipping %s %n", variable.getFullName());
                        continue;
                    }
                    System.out.printf("Keeping %s %d %n", variable.getFullName(), rank);
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
                    final BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
                    // Begin drawing image.
                    final Graphics2D g2d = image.createGraphics();
                    try {
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
                                    g2d.setPaint(color);
                                    final int imageX = imageScale * x;
                                    final int imageY = imageScale * ((invertLat) ? ylat - y : y);
                                    g2d.fillRect(imageX, imageY, imageScale, imageScale);
                                }
                            }
                        }
                    } finally {
                        g2d.dispose();
                    }
                    // Save image.
                    final String baseFilename = filename.substring(0, filename.lastIndexOf('.'));
                    final String imageFilename = String.format("%s_%s.%s", baseFilename, variable.getShortName(), "png");
                    final Path imageFile = Paths.get(file.getParent().toString(), imageFilename);
                    System.out.println(imageFile.toAbsolutePath().toString());
                    ImageIO.write(image, "png", imageFile.toFile());
                    // Extract coords.
                    final Dimension lonDim = variable.getDimension(rank - 1);
                    final Variable lonVariable = variable.getParentGroup().findVariable(lonDim.getShortName());
                    lonVariable.getAttributes().stream().forEach(System.out::println);
//                    final Array lonArray = lonVariable.read();
                    final Dimension latDim = variable.getDimension(rank - 2);
                    final Variable latVariable = variable.getParentGroup().findVariable(latDim.getShortName());
                    latVariable.getAttributes().stream().forEach(System.out::println);
//                    final Array latArray = latVariable.read();
                    final float minLon = getVariableAttributeValue(lonVariable, "min", 0);
                    final float maxLon = getVariableAttributeValue(lonVariable, "max", 0);
                    final float minLat = getVariableAttributeValue(latVariable, "min", 0);
                    final float maxLat = getVariableAttributeValue(latVariable, "max", 0);
                    final float dx = (maxLon - minLon) / xlon;
                    final float dy = (maxLat - minLat) / ylat;
                    final float lon0 = minLon - dx / 2f;
                    final float lon1 = maxLon + dx / 2f;
                    final float lat0 = maxLat + dy / 2f;
                    final float lat1 = minLat - dy / 2f;
                    // Save coords file.
                    final String coordsFilename = String.format("%s_%s_coords.%s", baseFilename, variable.getShortName(), "txt");
                    final Path coordsFile = Paths.get(file.getParent().toString(), coordsFilename);
                    try (final PrintWriter writer = new PrintWriter(coordsFile.toFile())) {
                        writer.println("Dimension boundaries");
                        writer.printf("%f\t%f%n", maxLat, minLon);
                        writer.printf("%f\t%f%n", minLat, maxLon);
                        writer.printf("Avg cell width: %f%n", dx);
                        writer.printf("Avg cell height: %f%n", dy);
                        writer.println("Outer shell");
                        writer.printf("%f\t%f%n", lat0, lon0);
                        writer.printf("%f\t%f%n", lat1, lon1);
                    }
                }
            }
        }
        return null;
    }

    private void generatePaletteImage() throws IOException {
        final int min = 0;
        final int max = 255;
        final int imageWidth = 256;
        final int imageHeight = 50;
        final BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        // Begin drawing image.
        final Graphics2D g2d = image.createGraphics();
        try {
            IntStream.range(min, max).forEach(index -> {
                final int rgb = computeColor(index, min, max);
                final Color color = new Color(rgb);
                g2d.setPaint(color);
                g2d.fillRect(index, 0, 1, imageHeight);
            });
        } finally {
            g2d.dispose();
        }
        final Path imageFile = Paths.get(sourceDir.toString(), "ramp.png");
        ImageIO.write(image, "png", imageFile.toFile());
    }

    private List<Path> walkFolder(final File directory) throws IOException {
        final File[] files = directory.listFiles();
        if (files == null) {
            return Collections.EMPTY_LIST;
        }
        final List<Path> result = new LinkedList();
        for (final File file : files) {
            if (file.isDirectory()) {
                result.addAll(walkFolder(file));
            } else if (file.isFile()) {
                final String filePath = file.getAbsolutePath();
                if (filePath.endsWith(".nc") || filePath.endsWith(".cdf")) { // NOI18N.
                    result.add(file.toPath());
                }
            }
        }
        return Collections.unmodifiableList(result);
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
