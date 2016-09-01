/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.scene;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.Main;

/**
 * FXML utility class.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public enum FXMLUtils {
    /**
     * The unique instance of this class.
     */
    INSTANCE;

    /**
     * Loads an FXML using the {@code fx:root} construct and inject it into the given node.
     * @param <T> The type of the FXML controller.
     * @param <N> The type of the FXML root.
     * @param fxmlURL The URL to the FXML file.
     * @param root The node.
     * @return 
     */
    public <T, N extends Node> Optional<T> loadAndInject(final URL fxmlURL, N root) {
        Optional<T> result = Optional.empty();
        if (fxmlURL != null) {
            try {
                final FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL, Main.I18N);
                fxmlLoader.setRoot(root);
                fxmlLoader.load();
                result = Optional.ofNullable(fxmlLoader.getController());
            } catch (IOException ex) {
                Logger.getLogger(FXMLUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return result;
    }

    /**
     * Dispose the controller.
     * @param <T> The type of the controller.
     * @param controller The optional controller.
     * @return Always {@code Optional.empty()}.
     */
    public <T> Optional<T> disposeController(final Optional<T> controller) {
        if (controller != null) {
            controller.ifPresent(this::disposeController);
        }
        return Optional.empty();
    }

    /**
     * Dispose the controller.
     * @param <T> The type of the controller.
     * @param controller The controller.
     */
    public <T> void disposeController(final T controller) {
        if (controller instanceof Disposable) {
            final Disposable disposable = (Disposable) controller;
            disposable.dispose();
        }
    }
}
