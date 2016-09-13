/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.data;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;

/**
 * Pre-load NetCDF files for the navigator UI.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class FileInfo {

    /**
     * The source file.
     */
    private final Path file;
    /**
     * The variables descriptors.
     */
    private final Set<VariableInfo> variables;

    /**
     * Possible selected states of this file info.
     * @author Fabrice Bouyé (fabriceb@spc.int)
     */
    public enum SelectedSate {
        /**
         * No variable selected.
         */
        NONE,
        /**
         * Some variables selected.
         */
        SOME,
        /**
         * All variables selected.
         */
        ALL;
    }

    /**
     * Creates a new instance.
     * @param file The source file.
     * @param variables The variables descriptors.
     * @throws NullPointerException If {@code file} is {@code null}.
     */
    public FileInfo(final Path file, final VariableInfo... variables) throws NullPointerException {
        Objects.requireNonNull(file);
        this.file = file;
        this.variables = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(variables)));
        this.variables.stream()
                .forEach(variableInfo -> variableInfo.selectedProperty().addListener(variableSelectedChangeListener));
        recomputeSelectedState();
    }

    @Override
    public String toString() {
        return file.getFileName().toString();
    }

    /**
     * Gets the source file.
     * @return A {@code Path} instance, never {@code null}.
     */
    public Path getFile() {
        return file;
    }

    /**
     * Select all variables in this info.
     * @param selected The value.
     */
    public void selectAllVariables(final boolean selected) {
        selectedEditing = true;
        variables.stream()
                .forEach(variableInfo -> variableInfo.setSelected(selected));
        selectedEditing = false;
        recomputeSelectedState();
    }

    /**
     * Editing flag.
     */
    private boolean selectedEditing = false;

    /**
     * Recompute the selected state.
     */
    private void recomputeSelectedState() {
        if (selectedEditing) {
            return;
        }
        final long totalVariableCount = variables.size();
        final long selectedVariableCount = variables.stream()
                .filter(VariableInfo::isSelected)
                .count();
        final SelectedSate newState = (selectedVariableCount == 0) ? SelectedSate.NONE : (selectedVariableCount == totalVariableCount) ? SelectedSate.ALL : SelectedSate.SOME;
        selectedState.set(newState);
    }

    /**
    * Called whenever the selected property of a variable changes.
    */
    private final ChangeListener<Boolean> variableSelectedChangeListener = (observable, oldValue, newValue) -> recomputeSelectedState();

    ////////////////////////////////////////////////////////////////////////////
    /**
     * The selected state of this info object.
     */
    private final ReadOnlyObjectWrapper<SelectedSate> selectedState = new ReadOnlyObjectWrapper<>(this, "selectedState", SelectedSate.NONE); // NOI18N.

    public final SelectedSate getSelectedState() {
        return selectedState.get();
    }

    public final ReadOnlyObjectProperty<SelectedSate> selectedStateProperty() {
        return selectedState.getReadOnlyProperty();
    }
}
