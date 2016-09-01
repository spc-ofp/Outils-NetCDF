/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about.libraries;

import java.net.URL;
import java.util.Optional;
import javafx.scene.layout.GridPane;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.scene.FXMLUtils;

/**
 * Show information about a third party library.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class LibraryPane extends GridPane implements Disposable {

    private Optional<LibraryPaneController> controller = Optional.empty();

    /**
     * Creates a new instance.
     */
    public LibraryPane() {
        super();
        setId("libraryPane"); // NOI18N.
        final URL fxmlURL = getClass().getResource("LibraryPane.fxml"); // NOI18N.
        controller = FXMLUtils.INSTANCE.loadAndInject(fxmlURL, this);
    }

    @Override
    public void dispose() {
        controller = FXMLUtils.INSTANCE.disposeController(controller);
    }
    
    public void updateContent(final String library, final String version, final String owner, final URL homepage, final URL licenseFile) {
        controller.ifPresent(c -> c.updateContent(library, version, owner, homepage, licenseFile));
    }
}
