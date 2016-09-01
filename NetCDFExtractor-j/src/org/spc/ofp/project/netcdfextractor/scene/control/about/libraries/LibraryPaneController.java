/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about.libraries;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import org.spc.ofp.project.netcdfextractor.Disposable;

/**
 * FXML controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class LibraryPaneController implements Initializable, Disposable {

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
    }

    @Override
    public void dispose() {
    }

    public void updateContent(final String library, final String version, final String owner, final URL homepage, final URL licenseFile) {
    }
}
