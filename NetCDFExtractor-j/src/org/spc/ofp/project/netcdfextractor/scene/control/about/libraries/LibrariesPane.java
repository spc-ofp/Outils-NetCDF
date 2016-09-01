/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about.libraries;

import java.net.URL;
import java.util.Optional;
import javafx.scene.layout.StackPane;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.scene.FXMLUtils;

/**
 * Show information about third party libraries.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class LibrariesPane extends StackPane implements Disposable {

    private Optional<LibrariesPaneController> controller = Optional.empty();

    /**
     * Creates a new instance.
     */
    public LibrariesPane() {
        super();
        setId("librariesPane"); // NOI18N.
        final URL fxmlURL = getClass().getResource("LibrariesPane.fxml"); // NOI18N.
        controller = FXMLUtils.INSTANCE.loadAndInject(fxmlURL, this);
    }

    @Override
    public void dispose() {
        FXMLUtils.INSTANCE.disposeController(controller);
    }
}
