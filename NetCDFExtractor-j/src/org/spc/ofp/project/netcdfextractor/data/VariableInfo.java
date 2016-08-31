/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Pre-load NetCDF variable for the navigator UI.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class VariableInfo {

    private final String shortName;
    private final String fullName;
    private final String description;

    public VariableInfo(final String shortName, final String fullName, final String description) {
        this.shortName = shortName;
        this.fullName = fullName;
        this.description = description;
    }

    @Override
    public String toString() {
        return shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getDescription() {
        return description;
    }
    
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Sets whether this variable is selected.
     */
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "sekected", false); // NOI18N.

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
