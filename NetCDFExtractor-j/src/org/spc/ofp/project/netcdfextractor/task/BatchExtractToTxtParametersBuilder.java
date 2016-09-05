/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Extraction parameters builder.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class BatchExtractToTxtParametersBuilder {

    /**
     * Delegated parameters.
     */
    private final BatchExtractToTxtParameters delegated = new BatchExtractToTxtParameters();

    /**
     * Hidden constructor.
     */
    private BatchExtractToTxtParametersBuilder() {
    }

    /**
     * Build a new parameter.
     * @return A {@code BatchExtractToTxtParameters} instance, never {@code null}.
     */
    public BatchExtractToTxtParameters build() {
        final BatchExtractToTxtParameters copy = new BatchExtractToTxtParameters();
        copy.forceGarbageCollection = delegated.forceGarbageCollection;
        copy.singleDocument = delegated.singleDocument;
        copy.destinationDir = delegated.destinationDir;
        copy.separator = delegated.separator;
        delegated.files
                .entrySet()
                .forEach(entry -> {
                    final Path source = entry.getKey();
                    final BatchExtractToTxtParameters.Settings settingsSource = entry.getValue();
                    final BatchExtractToTxtParameters.Settings settingsCopy = settingsForSource(copy, source);
                    settingsCopy.variables.addAll(settingsSource.variables);
                });
        return copy;
    }

    /**
     * Creates a new builder.
     * @return A {@code BatchExtractToTxtParametersBuilder} instance, never {@code null}.
     */
    public static BatchExtractToTxtParametersBuilder create() {
        return new BatchExtractToTxtParametersBuilder();
    }

    /**
     * Sets the force garbage collection flag.
     * @param value The new value.
     * @return A {@code BatchExtractToTxtParametersBuilder} instance, never {@code null}.
     */
    public BatchExtractToTxtParametersBuilder forceGarbageCollection(final boolean value) {
        delegated.forceGarbageCollection = value;
        return this;
    }

    public BatchExtractToTxtParametersBuilder singleDocument(final boolean value) {
        delegated.singleDocument = value;
        return this;
    }

    /**
     * Sets the separator.
     * @param separator The separator.
     * <br>If {@code null}, the default separator is used instead.
     * @return A {@code BatchExtractToTxtParametersBuilder} instance, never {@code null}.
     * @see BatchExtractToTxtParameters#DEFAULT_SEPARATOR
     */
    public BatchExtractToTxtParametersBuilder separator(final String separator) {
        delegated.separator = (separator == null) ? BatchExtractToTxtParameters.DEFAULT_SEPARATOR : separator;
        return this;
    }

    /**
     * Sets the destination.
     * @param destination The destination.
     * @return A {@code BatchExtractToTxtParametersBuilder} instance, never {@code null}.
     */
    public BatchExtractToTxtParametersBuilder destinationDir(final Path destination) {
        delegated.destinationDir = destination;
        return this;
    }

    /**
     * Adds a variable to this file.
     * @param source The source file.
     * @param variable The variable name.
     * @return A {@code BatchExtractToTxtParametersBuilder} instance, never {@code null}.
     * @throws NullPointerException If {@code source} is {@code null}.
     */
    public BatchExtractToTxtParametersBuilder addVariable(final Path source, final String variable) throws NullPointerException {
        Objects.requireNonNull(source);
        if (variable != null) {
            final BatchExtractToTxtParameters.Settings settings = settingsForSource(delegated, source);
            settings.variables.add(variable);
        }
        return this;
    }

    /**
     * Removes a variable from this file.
     * @param source The source file.
     * @param variable The variable name.
     * @return A {@code BatchExtractToTxtParametersBuilder} instance, never {@code null}.
     * @throws NullPointerException If {@code source} is {@code null}.
     */
    public BatchExtractToTxtParametersBuilder removeVariable(final Path source, final String variable) throws NullPointerException {
        Objects.requireNonNull(source);
        if (variable != null) {
            final BatchExtractToTxtParameters.Settings settings = settingsForSource(delegated, source);
            settings.variables.remove(variable);
        }
        return this;
    }

    /**
     * Clear all files and variable settings.
     * @return A {@code BatchExtractToTxtParametersBuilder} instance, never {@code null}.
     */
    public BatchExtractToTxtParametersBuilder clearAllFiles() {
        delegated.files.clear();
        return this;
    }

    /**
     * Gets the settings object for given source file.
     * <br>If no such object exists in the provided parameters, one will be initialized.
     * @param parameters The parameter object.
     * @param source The source file.
     * @return A {@code  BatchExtractToTxtParameters.Settings} instance, never {@code null}.
     */
    private static BatchExtractToTxtParameters.Settings settingsForSource(final BatchExtractToTxtParameters parameters, final Path source) {
        BatchExtractToTxtParameters.Settings result = parameters.files.get(source);
        if (result == null) {
            result = new BatchExtractToTxtParameters.Settings();
            parameters.files.put(source, result);
        }
        return result;
    }
}
