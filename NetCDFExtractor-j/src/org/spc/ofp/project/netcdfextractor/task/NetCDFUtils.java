/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.logging.Logger;
import javafx.util.Pair;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarPeriod.Field;

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

    /**
     * Converts a NetCDF {@code Field} (time unit) to a Java {@code ChronoUnit}.
     * @param field The source value.
     * @return A {@code ChronoUnit} value, never {@code null}.
     * @throws NullPointerException If {@code field} is {@code null}.
     */
    public ChronoUnit fieldToJava(final Field field) throws NullPointerException {
        Objects.requireNonNull(field);
        ChronoUnit result = ChronoUnit.SECONDS;
        switch (field) {
            case Millisec:
                result = ChronoUnit.MILLIS;
                break;
            case Second:
                result = ChronoUnit.SECONDS;
                break;
            case Minute:
                result = ChronoUnit.MINUTES;
                break;
            case Hour:
                result = ChronoUnit.HOURS;
                break;
            case Day:
                result = ChronoUnit.DAYS;
                break;
            case Month:
                result = ChronoUnit.MONTHS;
                break;
            case Year:
                result = ChronoUnit.YEARS;
                break;
        }
        return result;
    }

    /**
     * Converts a Java {@code ChronoUnit} to a NetCDF {@code Field} (time unit).
     * @param chronoUnit The source value.
     * @return A {@code Field} value, never {@code null}.
     * @throws NullPointerException If {@code chronoUnit} is {@code null}.
     * @throws IllegalArgumentException If {@code chronoUnit} is not supported.
     */
    public Field fieldFromJava(final ChronoUnit chronoUnit) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(chronoUnit);
        Field result = Field.Second;
        switch (chronoUnit) {
            case MILLIS:
                result = Field.Millisec;
                break;
            case SECONDS:
                result = Field.Second;
                break;
            case MINUTES:
                result = Field.Minute;
                break;
            case HOURS:
                result = Field.Hour;
                break;
            case DAYS:
                result = Field.Day;
                break;
            case MONTHS:
                result = Field.Month;
                break;
            case YEARS:
                result = Field.Year;
                break;
            default:
                final String message = String.format("%s not supported.", chronoUnit); // NOI18N.
                throw new IllegalArgumentException(message);
        }
        return result;
    }
}
