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

    T getApplication();

    void setApplication(final T value);

    ObjectProperty<T> applicationProperty();
}
