/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.util.logging.Logger;
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

    public float getAttributeValueF(final Variable variable, final String attributeName, final float defaultValue) {
        float result = defaultValue;
        final Attribute attribute = variable.findAttribute(attributeName);
        if (attribute != null) {
            result = attribute.getNumericValue().floatValue();
            if (Float.isNaN(result)) {
                result = defaultValue;
            }
        } else {
            final String message = String.format("Could not locate attribute \"%s\" in variable \"%s\", using default value.", attributeName, variable.getShortName());
            Logger.getLogger(getClass().getName()).warning(message);
        }
        return result;
    }

    public double getAttributeValueD(final Variable variable, final String attributeName, final double defaultValue) {
        double result = defaultValue;
        final Attribute attribute = variable.findAttribute(attributeName);
        if (attribute != null) {
            result = attribute.getNumericValue().doubleValue();
            if (Double.isNaN(result)) {
                result = defaultValue;
            }
        } else {
            final String message = String.format("Could not locate attribute \"%s\" in variable \"%s\", using default value.", attributeName, variable.getShortName());
            Logger.getLogger(getClass().getName()).warning(message);
        }
        return result;
    }

}
