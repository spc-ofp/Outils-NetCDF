/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.nio.file.Path;
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
    
    boolean forceGarbageCollection = true;
    
    public boolean isForceGarbageCollection() {
        return forceGarbageCollection;
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

        Path destination;

        /**
         * Gets the destination file.
         * @return A {@code Path} instance, may be {@code null} if not initialized.
         */
        public Path getDestination() {
            return destination;
        }

        String separator = ","; // NOI18N.

        /**
         * Gets the column separator.
         * @return A {@code String} instance, never {@code null}.
         */
        public String getSeparator() {
            return separator;
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
}
