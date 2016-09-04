/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor;

/**
 * Defines disposable resources.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public interface Disposable {

    /**
     * Call to dispose the resource.
     * <br>The resource should not be manipulated anymore once this method has been called.
     */
    public void dispose();
}
