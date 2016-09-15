/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.scene.control.about.libraries.LibrariesPane;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class AboutPaneController extends ControllerBase<AboutPane> {

    @FXML
    private StackPane librariesStack;
    @FXML
    private AcknowledgementPaneController acknowledgementPaneController;

    private LibrariesPane librariesPane;

    @Override
    public void dispose() {
        try {
            if (librariesPane != null) {
                librariesPane.applicationProperty().unbind();
                librariesPane.dispose();
                librariesPane = null;
            }
            if (acknowledgementPaneController != null) {
                acknowledgementPaneController.applicationProperty().unbind();
                acknowledgementPaneController.dispose();
            }
        } finally {
            super.dispose();
        }
    }

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        librariesPane = new LibrariesPane();
        librariesPane.applicationProperty().bind(applicationProperty());
        //
        librariesStack.getChildren().add(librariesPane);
        //
        acknowledgementPaneController.applicationProperty().bind(applicationProperty());
    }
}
