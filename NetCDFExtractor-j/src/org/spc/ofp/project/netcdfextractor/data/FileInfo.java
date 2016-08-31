/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.data;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Pre-load NetCDF files for the navigator UI.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class FileInfo {

    private final Path file;
    private final List<String> variables;

    public FileInfo(final Path file, final String... variables) {
        this.file = file;
        this.variables = Collections.unmodifiableList(Arrays.asList(variables));
    }

    public Path getFile() {
        return file;
    }

    @Override
    public String toString() {
        return file.getFileName().toString();
    }

    /**
     * Sets whether this file is selected.
     */
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "sekected", false);

    public final boolean isSelected() {
        return selected.get();
    }

    public final void setSelected(final boolean value) {
        selected.set(value);
    }

    public final BooleanProperty selectedProperty() {
        return selected;
    }

}
