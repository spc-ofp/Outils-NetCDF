/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.util.Pair;
import org.spc.ofp.project.netcdfextractor.Main;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Batch extract to text files.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class BatchExtractToTxtTask extends Task<Void> {

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
        if (files.isEmpty()) {
            return null;
        }
        final String separator = parameters.getSeparator();
        final Path destinationDir = parameters.getDestinationDir();
        totalFiles = files.size();
        //
        final boolean singleDocument = parameters.isSingleDocument();
        // Export files as a single document.
        if (singleDocument) {
            final Path firstFile = files.iterator().next();
            final BatchExtractToTxtParameters.Settings settings = parameters.getSettings(firstFile);
            final Path output = createDestination(firstFile, destinationDir);
            try (final BufferedWriter writer = Files.newBufferedWriter(output);
                    final PrintWriter out = new PrintWriter(writer)) {
                final Iterator<Path> fileIterator = files.iterator();
                for (int fileIndex = 0; fileIndex < files.size(); fileIndex++) {
                    // Settings.
                    final boolean includeColumnHeader = (fileIndex == 0) ? parameters.isIncludeColumnHeader() : false;
                    final Path file = fileIterator.next();
                    final String[] variables = settings.getVariables().toArray(new String[0]);
                    if (variables.length == 0) {
                        final String message = String.format("No variable to export for file \"%s\".", file.toString()); // NOI18N.
                        Logger.getLogger(getClass().getName()).warning(message);
                        continue;
                    }
                    if (isCancelled()) {
                        return null;
                    }
                    // Title.
                    updateTitleForFile(file);
                    // Export.
                    try (final NetcdfFile netcdf = NetcdfFile.open(file.toString())) {
                        exportFile(file, netcdf, out, includeColumnHeader, separator, variables);
                    }
                    if (isCancelled()) {
                        return null;
                    }
                    currentFile++;
                    if (parameters.isForceGarbageCollection()) {
                        System.gc();
                    }
                }
            }

        } // Export files individually.
        else {
            final boolean includeColumnHeader = parameters.isIncludeColumnHeader();
            for (final Path file : files) {
                // Settings.
                final BatchExtractToTxtParameters.Settings settings = parameters.getSettings(file);
                final Path output = createDestination(file, destinationDir);
                final String[] variables = settings.getVariables().toArray(new String[0]);
                if (variables.length == 0) {
                    final String message = String.format("No variable to export for file \"%s\".", file.toString()); // NOI18N.
                    Logger.getLogger(getClass().getName()).warning(message);
                    continue;
                }
                if (isCancelled()) {
                    return null;
                }
                // Title.                
                updateTitleForFile(file);
                // Export.
                try (final NetcdfFile netcdf = NetcdfFile.open(file.toString());
                        final BufferedWriter writer = Files.newBufferedWriter(output);
                        final PrintWriter out = new PrintWriter(writer)) {
                    exportFile(file, netcdf, out, includeColumnHeader, separator, variables);
                }
                //
                if (isCancelled()) {
                    return null;
                }
                currentFile++;
                if (parameters.isForceGarbageCollection()) {
                    System.gc();
                }
            }
        }
        return null;
    }

    /**
     * Generate destination file for given source file and given target directory.
     * @param source The source file.
     * @param targetDir The target directory.
     * @return A {@code Path} instance, never {@code null}.
     * @throws NullPointerException If {@code source} or {@code targetDir} is {@code null}.
     * @throws IllegalArgumentException If {@code targetDir} is not a directory.
     * @throws java.io.IOException If the destination file is not writable.
     */
    public static Path createDestination(final Path source, final Path targetDir) throws NullPointerException, IllegalArgumentException, IOException {
        Objects.requireNonNull(source);
        final Path outputDir = (targetDir == null) ? source.getParent() : targetDir;
        if (!Files.isDirectory(outputDir)) {
            throw new IllegalArgumentException("targetDir is not a directory.");
        }
        final String dir = outputDir.toString();
        final String sourceName = source.getFileName().toString();
        final String outputName = sourceName.replaceAll("\\.(nc|cdf)", ".txt"); // NOI18N.
        final Path destination = Paths.get(dir, outputName);
        if (Files.exists(destination) && (!Files.isRegularFile(destination) || !Files.isWritable(destination))) {
            final String message = String.format("Cannot write destination file \"%s\".", destination.toString());
            throw new IOException(message);
        }
        return destination;
    }

    /**
     * Set of supported data types for export.
     */
    private static final Set<DataType> SUPPORTED_DATA_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            DataType.SHORT,
            DataType.INT,
            DataType.LONG,
            DataType.FLOAT,
            DataType.DOUBLE
    )));

    private long currentProgress = 0;
    private long totalProgress = 100;

    private int currentFile = 0;
    private int totalFiles = 0;

    /**
     * Update the task title for given file.
     * @param source The source file.
     */
    private void updateTitleForFile(final Path source) {
        final String titlePattern = Main.I18N.getString("extract.title.pattern"); // NOI18N.
        final String title = String.format(titlePattern, currentFile + 1, totalFiles, source.getFileName().toString());
        updateTitle(title);
    }

    /**
     * Export a given file into the given output.
     * @param source The source file.
     * @param netcdf The NetCDF file opened from the source.
     * @param destination The destination file.
     * @param includeColumnHeader If {@code true} prints out the columns names.
     * @param separator The separator.
     * @param variablesNames Variables to export.
     * @throws IOException In case of IO error.
     * @throws InvalidRangeException NetCDF index error, should never happen.
     */
    private void exportFile(final Path source, final NetcdfFile netcdf, final PrintWriter out, final boolean includeColumnHeader, final String separator, final String... variableNames) throws IOException, InvalidRangeException {
        currentProgress = 0;
        //
        final int periodSize = parameters.getPeriodSize();
        final ChronoUnit periodUnit = parameters.getPeriodUnit();
        final ZonedDateTime startDate = parameters.getStartDate();
        final Object outputMissingValue = parameters.getMissingValue();
        final DateTimeFormatter dateTimeFormatter = parameters.getDateTimeFormatter();
        ////////////////////////////////////////////////////////////////////
        // Collect variables.
        updateMessage(Main.I18N.getString("extract.progress.collecting-variables")); // NOI18N.
        final Variable[] variables = Arrays.stream(variableNames)
                .map(netcdf::findVariable)
                .filter(variable -> variable.getRank() == 3 && SUPPORTED_DATA_TYPES.contains(variable.getDataType()))
                .toArray(Variable[]::new);
        // Nothing to do.
        if (variables.length == 0) {
            final String message = String.format("No variable to export for file \"%s\".", source.toString()); // NOI18N.
            Logger.getLogger(getClass().getName()).warning(message);
            return;
        }
        if (isCancelled()) {
            return;
        }
        ////////////////////////////////////////////////////////////////////
        // Collect dimensions.
        updateMessage(Main.I18N.getString("extract.progress.collecting-dimensions")); // NOI18N.
        final Dimension[] dimensions = variables[0].getDimensions()
                .stream()
                .toArray(Dimension[]::new);
        if (isCancelled()) {
            return;
        }
        // Dimension sizes.
        updateMessage(Main.I18N.getString("extract.progress.collecting-dimensions-sizes")); // NOI18N.
        final int[] sizes = Arrays.stream(dimensions)
                .mapToInt(Dimension::getLength)
                .toArray();
        if (isCancelled()) {
            return;
        }
        ////////////////////////////////////////////////////////////////////
        // Now compute total extraction length.
        // 6 preliminary steps.
        final long totalRows = sizes[0] * sizes[1] * sizes[2];
        totalProgress = 7;
        // 1 header to write
        if (includeColumnHeader) {
            totalProgress += 1;
        }
        // Each file row is (numDims + numVars + 1 row to write)
        totalProgress += (dimensions.length + variables.length + 1) * totalRows;
        ////////////////////////////////////////////////////////////////////
        // Variable data type.
        updateMessage(Main.I18N.getString("extract.progress.collecting-variables-types")); // NOI18N.
        final DataType[] dataTypes = Arrays.stream(variables)
                .map(Variable::getDataType)
                .toArray(DataType[]::new);
        currentProgress++;
        updateProgress(currentProgress, totalProgress);
        if (isCancelled()) {
            return;
        }
        // Variable fill values.
        updateMessage(Main.I18N.getString("extract.progress.collecting-variables-fill-values")); // NOI18N.
        final Number[] fillValues = Arrays.stream(variables)
                .map(variable -> NetCDFUtils.INSTANCE.getNumericAttribute(variable, "_FillValue", Double.NaN)) // NOI18N.
                .toArray(Number[]::new);
        currentProgress++;
        updateProgress(currentProgress, totalProgress);
        if (isCancelled()) {
            return;
        }
        // Variable missing values.
        updateMessage(Main.I18N.getString("extract.progress.collecting-variables-missing-values")); // NOI18N.
        final Number[] missingValues = Arrays.stream(variables)
                .map(variable -> NetCDFUtils.INSTANCE.getNumericAttribute(variable, "missing_value", Double.NaN)) // NOI18N.
                .toArray(Number[]::new);
        currentProgress++;
        updateProgress(currentProgress, totalProgress);
        if (isCancelled()) {
            return;
        }
        // Variable scale factors.
        updateMessage(Main.I18N.getString("extract.progress.collecting-variables-scale-factors")); // NOI18N.
        final Number[] scaleFactors = Arrays.stream(variables)
                .map(variable -> NetCDFUtils.INSTANCE.getNumericAttribute(variable, "scale_factor", 1)) // NOI18N.
                .toArray(Number[]::new);
        currentProgress++;
        updateProgress(currentProgress, totalProgress);
        if (isCancelled()) {
            return;
        }
        // Variable add offets.
        updateMessage(Main.I18N.getString("extract.progress.collecting-variables-add-offsets")); // NOI18N.
        final Number[] addOffsets = Arrays.stream(variables)
                .map(variable -> NetCDFUtils.INSTANCE.getNumericAttribute(variable, "add_offset", 0)) // NOI18N.
                .toArray(Number[]::new);
        currentProgress++;
        updateProgress(currentProgress, totalProgress);
        if (isCancelled()) {
            return;
        }
        // Variable valid ranges.
        updateMessage(Main.I18N.getString("extract.progress.collecting-variables-valid-ranges")); // NOI18N.
        final Pair<Number, Number>[] validRanges = Arrays.stream(variables)
                .map(variable -> NetCDFUtils.INSTANCE.getValidRangeAttribute(variable, -Double.MAX_VALUE, Double.MAX_VALUE))
                .toArray(Pair[]::new);
        currentProgress++;
        updateProgress(currentProgress, totalProgress);
        if (isCancelled()) {
            return;
        }
        // Dimension variables.
        updateMessage(Main.I18N.getString("extract.progress.collecting-dimensions-variables")); // NOI18N.
        final Variable[] dimensionVariables = Arrays.stream(dimensions)
                .map(Dimension::getFullName)
                .map(dimensionName -> netcdf.findVariable(dimensionName))
                .toArray(Variable[]::new);
        currentProgress++;
        updateProgress(currentProgress, totalProgress);
        if (isCancelled()) {
            return;
        }
        ////////////////////////////////////////////////////////////////////
        // Extract.
        long currentRow = 0;
        final String rowPattern = Main.I18N.getString("extract.progress.row.pattern"); // NOI18N.
        // Write header.
        if (includeColumnHeader) {
            writeHeader(out, separator, dimensions, variables);
            currentProgress++;
            updateProgress(currentProgress, totalProgress);
            if (isCancelled()) {
                return;
            }
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
            final long time = zArray.getLong(0);
            for (int y = 0; y < sizes[1]; y++) {
                yIndex[0] = y;
                final Array yArray = dimensionVariables[1].read(yIndex, yShape);
                final float lat = yArray.getFloat(0);
                for (int x = 0; x < sizes[2]; x++) {
                    xIndex[0] = x;
                    final Array xArray = dimensionVariables[2].read(xIndex, xShape);
                    final float lon = xArray.getFloat(0);
                    //
                    updateMessage(String.format(rowPattern, currentRow + 1, totalRows));
                    final StringBuilder line = new StringBuilder();
                    // Time.
//                            final ZonedDateTime utc = Instant.ofEpochSecond(time).atZone(ZoneOffset.UTC);
                    final ZonedDateTime utc = startDate.plus(time * periodSize, periodUnit);
                    line.append(utc.format(dateTimeFormatter));
                    line.append(separator);
                    currentProgress++;
                    updateProgress(currentProgress, totalProgress);
                    if (isCancelled()) {
                        return;
                    }
                    // Lat.
                    line.append(lat);
                    line.append(separator);
                    currentProgress++;
                    updateProgress(currentProgress, totalProgress);
                    if (isCancelled()) {
                        return;
                    }
                    // Lon.
                    line.append(lon);
                    line.append(separator);
                    currentProgress++;
                    updateProgress(currentProgress, totalProgress);
                    if (isCancelled()) {
                        return;
                    }
                    // Variables.
                    vIndex[0] = z;
                    vIndex[1] = y;
                    vIndex[2] = x;
                    for (int variableIndex = 0; variableIndex < variables.length; variableIndex++) {
                        final Variable variable = variables[variableIndex];
                        final DataType dataType = dataTypes[variableIndex];
                        final Array vArray = variable.read(vIndex, vShape);
                        switch (dataType) {
                            case SHORT:
                            case INT:
                            case LONG: {
                                final long validMin = validRanges[variableIndex].getKey().longValue();
                                final long validMax = validRanges[variableIndex].getValue().longValue();
                                final long missingValue = missingValues[variableIndex].longValue();
                                final long fillValue = fillValues[variableIndex].longValue();
                                final long scaleFactor = scaleFactors[variableIndex].longValue();
                                final long addOffset = addOffsets[variableIndex].longValue();
                                final long variableValue = vArray.getLong(0);
                                if (variableValue != fillValue && variableValue != missingValue && validMin <= variableValue && variableValue <= validMax) {
                                    final long value = variableValue * scaleFactor + addOffset;
                                    line.append(value);
                                } else if (outputMissingValue != null) {
                                    line.append(outputMissingValue);
                                }
                            }
                            break;
                            case FLOAT:
                            case DOUBLE: {
                                final double validMin = validRanges[variableIndex].getKey().doubleValue();
                                final double validMax = validRanges[variableIndex].getValue().doubleValue();
                                final double missingValue = missingValues[variableIndex].doubleValue();
                                final double fillValue = fillValues[variableIndex].doubleValue();
                                final double scaleFactor = scaleFactors[variableIndex].doubleValue();
                                final double addOffset = addOffsets[variableIndex].doubleValue();
                                final double variableValue = vArray.getDouble(0);
                                if (!Double.isNaN(variableValue) && variableValue != fillValue && variableValue != missingValue && validMin <= variableValue && variableValue <= validMax) {
                                    final double value = variableValue * scaleFactor + addOffset;
                                    line.append(value);
                                } else if (outputMissingValue != null) {
                                    line.append(outputMissingValue);
                                }
                            }
                            break;
                        }
                        line.append(separator);
                        currentProgress++;
                        updateProgress(currentProgress, totalProgress);
                        if (isCancelled()) {
                            return;
                        }
                    }
                    // Write line.
                    line.delete(line.lastIndexOf(separator), line.length());
                    out.println(line.toString());
                    currentProgress++;
                    updateProgress(currentProgress, totalProgress);
                    if (isCancelled()) {
                        return;
                    }
                    currentRow++;
                }
            }
        }
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
