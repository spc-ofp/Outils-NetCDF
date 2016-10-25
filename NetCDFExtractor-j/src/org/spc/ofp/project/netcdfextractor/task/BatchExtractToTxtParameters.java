/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parameters for the text batch tasks.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class BatchExtractToTxtParameters {

    public static final boolean DEFAULT_FORCE_GC = true;

    boolean forceGarbageCollection = DEFAULT_FORCE_GC;

    public boolean isForceGarbageCollection() {
        return forceGarbageCollection;
    }

    public static final boolean DEFAULT_SINGLE_DOCUMENT = false;

    boolean singleDocument = DEFAULT_SINGLE_DOCUMENT;

    public boolean isSingleDocument() {
        return singleDocument;
    }

    public static final boolean DEFAULT_INCLUDE_COLUMN_HEADER = true;

    boolean includeColumnHeader = DEFAULT_INCLUDE_COLUMN_HEADER;

    public boolean isIncludeColumnHeader() {
        return includeColumnHeader;
    }

    /**
     * The default missing value, is equal to {@value}.    
     */
    public static String DEFAULT_MISSING_VALUE = "NaN"; // NOI18N.

    Object missingValue = DEFAULT_MISSING_VALUE;

    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    DateTimeFormatter dateTimeFormatter = DEFAULT_DATE_TIME_FORMATTER;

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    /**
     * The value to be printed out when encountering a missing, fill or out of bounds value in the file.
     * @return The missing value, may be {@code null}.
     */
    public Object getMissingValue() {
        return missingValue;
    }

    /**
     * The default separator, is equal to {@value}.    
     */
    public static final String DEFAULT_SEPARATOR = ","; // NOI18N.

    Path destinationDir = null;

    public Path getDestinationDir() {
        return destinationDir;
    }

    String separator = DEFAULT_SEPARATOR;

    /**
     * Gets the column separator.
     * @return A {@code String} instance, never {@code null}.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Extraction settings for a given file.
     * @author Fabrice Bouyé (fabriceb@spc.int)
     */
    public static final class Settings {

        final List<String> variables = new LinkedList<>();
        private final List<String> variablesUnmodifiable = Collections.unmodifiableList(variables);

        /**
         * Gets the list of variables to export (the same variable can be present several times in the list).
         * @return A non-modifiable {@code List<String>}, never {@code null}.
         */
        public List<String> getVariables() {
            return variablesUnmodifiable;
        }
    }

    /**
     * Map of files -> parameters to export.
     */
    final Map<Path, Settings> files = new LinkedHashMap<>();
    final Set<Path> filesUnmodifiable = Collections.unmodifiableSet(files.keySet());

    /**
     * Test whether this parameters is empty.
     * @return {@code True} if the test succeeds, {@code false} otherwise.
     */
    public final boolean isEmpty() {
        return files.isEmpty();
    }

    /**
     * Gets the set of files to export.
     * @return A non-modifiable {@code Set<Path>}, never {@code null}.
     */
    public final Set<Path> getFiles() {
        return filesUnmodifiable;
    }

    /**
     * Gets the export settings for given file.
     * @param source The source file.
     * @return A {@code Settings} instance, maybe {@code null} if {@code source} is not registered in this parameters set.
     */
    public final Settings getSettings(final Path source) {
        return files.get(source);
    }

    public static final int DEFAULT_PERIOD_SIZE = 1;

    int periodSize = DEFAULT_PERIOD_SIZE;

    /**
     * Gets the size of the time period.
     * @return An {@code int} &ge; 1.
     */
    public int getPeriodSize() {
        return periodSize;
    }

    public static final ChronoUnit DEFAULT_PERIOD_UNIT = ChronoUnit.SECONDS;

    ChronoUnit periodUnit = DEFAULT_PERIOD_UNIT;

    /**
     * Gets the unit of the time period.
     * @return A {@code ChronoUnit} instance, never {@code null}.
     */
    public ChronoUnit getPeriodUnit() {
        return periodUnit;
    }

    public static final ZonedDateTime DEFAULT_START_DATE = ZonedDateTime.parse("1970-01-01T00:00Z"); // NOI18N.

    ZonedDateTime startDate = DEFAULT_START_DATE;

    /**
     * Gets the start date of the calendar.
     * @return A {@code ZonedDateTime} instance, never {@code null}.
     */
    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public static final String DEFAULT_TIME_VARIABLE = "time"; // NOI18N.

    String timeVariable = DEFAULT_TIME_VARIABLE;

    /**
     * Gets the start date of the calendar.
     * @return A {@code String} instance, never {@code null}.
     */
    public String getTimeVariable() {
        return timeVariable;
    }
}
