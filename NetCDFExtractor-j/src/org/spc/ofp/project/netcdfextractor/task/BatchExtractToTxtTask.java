/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import javafx.concurrent.Task;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Batch extract to text files.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public class BatchExtractToTxtTask extends Task<Void> {

    /**
     * The task parameters
     */
    private final BatchExtractToTxtParameters parameters;

    /**
     * Creates a new instance.
     * @param parameters The task parameters
     * @throws IllegalArgumentException If there is no files to export.
     */
    public BatchExtractToTxtTask(final BatchExtractToTxtParameters parameters) throws IllegalArgumentException {
        Objects.requireNonNull(parameters);
        this.parameters = parameters;
    }

    @Override
    protected Void call() throws Exception {
        final Set<Path> files = parameters.getFiles();
        totalProgress = 2 * files.size();
        // Export files.
        for (final Path file : files) {
            // Settings.
            final BatchExtractToTxtParameters.Settings settings = parameters.getSettings(file);
            final String separator = settings.getSeparator();
            final Path output = settings.getDestination();
            final String[] variables = settings.getVariables().toArray(new String[0]);
            progress++;
            updateProgress(progress, totalProgress);
            if (isCancelled()) {
                return null;
            }
            // Export.
            exportFile(file, output, separator, variables);
            progress++;
            updateProgress(progress, totalProgress);
            if (isCancelled()) {
                return null;
            }
        }
        return null;
    }

    /**
     * Generate default destination file for given source file.
     * @param source The source file.
     * @return A {@code Path} instance, never {@code null}.
     * @throws NullPointerException If {@code source} is {@code null}.
     */
    public static Path createDefaultDestination(final Path source) throws NullPointerException {
        Objects.requireNonNull(source);
        final String dir = source.getParent().toString();
        final String sourceName = source.getFileName().toString();
        final String outputName = sourceName.replaceAll("\\.(nc|cdf)", ".txt"); // NOI18N.
        final Path destination = Paths.get(dir, outputName);
        return destination;
    }

    private long progress = 0;
    private long totalProgress = 100;

    /**
     * Export a given file.
     * @param source The source file.
     * @param destination The destination file.
     * @param separator The separator.
     * @throws IOException In case of IO error.
     * @throws InvalidRangeException NetCDF index error, should never happen.
     */
    private void exportFile(final Path source, final Path destination, final String separator, final String... variableNames) throws IOException, InvalidRangeException {
        try (final NetcdfFile netcdf = NetcdfFile.open(source.toString())) {
            // Collect variables.
            final Variable[] variables = Arrays.stream(variableNames)
                    .map(netcdf::findVariable)
                    .filter(variable -> variable.getRank() == 3)
                    .toArray(Variable[]::new);
            totalProgress += 1;
            progress++;
            updateProgress(progress, totalProgress);
            if (isCancelled()) {
                return;
            }
            if (variables.length == 0) {
                return;
            }
            // Collect dimensions.
            totalProgress += 3;
            final Dimension[] dimensions = variables[0].getDimensions()
                    .stream()
                    .toArray(Dimension[]::new);
            progress++;
            updateProgress(progress, totalProgress);
            if (isCancelled()) {
                return;
            }
            final Variable[] dimensionVariables = Arrays.stream(dimensions)
                    .map(Dimension::getFullName)
                    .map(dimensionName -> netcdf.findVariable(dimensionName))
                    .toArray(Variable[]::new);
            progress++;
            updateProgress(progress, totalProgress);
            if (isCancelled()) {
                return;
            }
            final int[] sizes = Arrays.stream(dimensions)
                    .mapToInt(Dimension::getLength)
                    .toArray();
            progress++;
            updateProgress(progress, totalProgress);
            if (isCancelled()) {
                return;
            }
            //
            totalProgress += 1 + (dimensions.length + variables.length + 1) * sizes[0] * sizes[1] * sizes[2];
            try (final BufferedWriter writer = Files.newBufferedWriter(destination);
                    final PrintWriter out = new PrintWriter(writer)) {
                // Write header.
                writeHeader(out, separator, dimensions, variables);
                progress++;
                updateProgress(progress, totalProgress);
                if (isCancelled()) {
                    return;
                }
                // Extraction.
                final int[] zIndex = {0};
                final int[] zShape = {1};
                final int[] yIndex = {0};
                final int[] yShape = {1};
                final int[] xIndex = {0};
                final int[] xShape = {1};
                final int[] vIndex = {0, 0, 0};
                final int[] vShape = {1, 1, 1};
                for (int z = 0; z < sizes[0]; z++) {
                    zIndex[0] = z;
                    final Array zArray = dimensionVariables[0].read(zIndex, zShape);
                    for (int y = 0; y < sizes[1]; y++) {
                        yIndex[0] = y;
                        final Array yArray = dimensionVariables[1].read(yIndex, yShape);
                        for (int x = 0; x < sizes[2]; x++) {
                            xIndex[0] = x;
                            final Array xArray = dimensionVariables[2].read(xIndex, xShape);
                            // Time.
                            final long time = zArray.getLong(0);
                            final ZonedDateTime utc = Instant.ofEpochSecond(time).atZone(ZoneOffset.UTC);
                            final StringBuilder line = new StringBuilder();
                            line.append(utc);
                            line.append(separator);
                            progress++;
                            updateProgress(progress, totalProgress);
                            if (isCancelled()) {
                                return;
                            }
                            // Lat.
                            line.append(yArray.getFloat(0));
                            line.append(separator);
                            progress++;
                            updateProgress(progress, totalProgress);
                            if (isCancelled()) {
                                return;
                            }
                            // Lon.
                            line.append(xArray.getFloat(0));
                            line.append(separator);
                            progress++;
                            updateProgress(progress, totalProgress);
                            if (isCancelled()) {
                                return;
                            }
                            // Variables.
                            vIndex[0] = z;
                            vIndex[1] = y;
                            vIndex[2] = x;
                            for (final Variable variable : variables) {
                                final Attribute fillValueAttribute = variable.findAttribute("_FillValue"); // NOI18N.
                                final double fillValue = fillValueAttribute.getNumericValue().doubleValue();
                                final Attribute scaleFactorAttribute = variable.findAttribute("scale_factor"); // NOI18N.
                                final double scaleFactor = scaleFactorAttribute.getNumericValue().doubleValue();
                                final Array vArray = variable.read(vIndex, vShape);
                                final double variableValue = vArray.getDouble(0);
                                final double value = variableValue * scaleFactor;
                                if (value != fillValue) {
                                    line.append(value);
                                }
                                line.append(separator);
                                progress++;
                                updateProgress(progress, totalProgress);
                                if (isCancelled()) {
                                    return;
                                }
                            }
                            // Write line.s
                            line.delete(line.lastIndexOf(separator), line.length());
                            out.println(line.toString());
                            progress++;
                            updateProgress(progress, totalProgress);
                            if (isCancelled()) {
                                return;
                            }
                        }
                    }
                }
            }
        }
        System.gc();
    }

    /**
     * Write the header of the file.
     * @param out The output writer.
     * @param separator The value separator.
     * @param dimensions Array of dimensions.
     * @param variables Array of variables.
     */
    private void writeHeader(final PrintWriter out, final String separator, final Dimension[] dimensions, final Variable[] variables) {
        final StringBuilder line = new StringBuilder();
        // Dimensions.
        for (final Dimension dimension : dimensions) {
            line.append(dimension.getShortName());
            line.append(separator);
        }
        // Variables.
        for (final Variable variable : variables) {
            line.append(variable.getShortName());
            line.append(separator);
        }
        // Write line.
        line.delete(line.lastIndexOf(separator), line.length());
        out.println(line.toString());
    }
}
