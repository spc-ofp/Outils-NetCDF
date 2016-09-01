/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;

/**
 * Defines an application child.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 * @param <T> The type of the application.
 */
public interface ApplicationChild<T extends Application> {

    /**
     * Gets the parent application.
     * @return A {@code T} instance, may be {@code null}.
     */
    T getApplication();

    /**
     * Sets the parent application.
     * @param value The new value.
     */
    void setApplication(final T value);

    /**
     * Gets the parent application property.
     * @return An {@code ObjectProperty<T>} instance, never {@code null}.
     */
    ObjectProperty<T> applicationProperty();
}
