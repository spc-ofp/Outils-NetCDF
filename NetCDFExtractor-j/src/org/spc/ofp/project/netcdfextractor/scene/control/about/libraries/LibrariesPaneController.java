/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import org.spc.ofp.project.netcdfextractor.Disposable;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class LibrariesPaneController implements Initializable, Disposable {

    @FXML
    private StackPane rootPane;
    @FXML
    private TabPane tabPane;

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        Platform.runLater(() -> populateLibraries());
    }

    @Override
    public void dispose() {
    }

    private void populateLibraries() {
        final Properties properties = new Properties();
        try (final InputStream input = getClass().getResourceAsStream("libraries.properties")) { // NOI18N.
            properties.load(input);
        } catch (IOException ex) {
            Logger.getLogger(LibrariesPaneController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        final String librariesDef = properties.getProperty("libraries"); // NOI18N.
        if (librariesDef == null || librariesDef.trim().isEmpty()) {
            return;
        }
        final String[] libraries = librariesDef.trim().split("\\s+");
//        Arrays.stream(libraries)
//                ./
    }

}
