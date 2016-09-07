/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.util.logging.Logger;
import javafx.util.Pair;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

/**
 * NetCDF utility class.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public enum NetCDFUtils {
    /**
     * The unique instance of this class.
     */
    INSTANCE;

    public Number getNumericAttribute(final Variable variable, final String attributeName, final Number defaultValue) {
        Number result = defaultValue;
        final Attribute attribute = variable.findAttribute(attributeName);
        if (attribute != null) {
            result = attribute.getNumericValue().floatValue();
        } else {
            final String message = String.format("Could not locate attribute \"%s\" in variable \"%s\", using default value.", attributeName, variable.getShortName());
            Logger.getLogger(getClass().getName()).warning(message);
        }
        return result;
    }

    public Pair<Number, Number> getValidRangeAttribute(final Variable variable, final Number defaultMin, final Number defaultMax) {
        final Attribute validMinAttribute = variable.findAttribute("valid_min"); // NOI18N.
        final Attribute validMaxAttribute = variable.findAttribute("valid_max"); // NOI18N.
        final Attribute validRangeAttribute = variable.findAttribute("valid_range"); // NOI18N.
        Pair<Number, Number> result = null;
        if (validRangeAttribute != null) {
            result = new Pair<>(validRangeAttribute.getNumericValue(0), validRangeAttribute.getNumericValue(1));
        } else if (validMinAttribute != null && validMaxAttribute != null) {
            result = new Pair<>(validMinAttribute.getNumericValue(), validMaxAttribute.getNumericValue());
        } else {
            result = new Pair<>(defaultMin, defaultMax);
        }
        return result;
    }
}
