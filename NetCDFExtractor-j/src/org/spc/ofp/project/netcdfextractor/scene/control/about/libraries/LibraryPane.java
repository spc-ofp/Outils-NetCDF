/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about.libraries;

import java.net.URL;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.VBox;
import org.spc.ofp.project.netcdfextractor.ApplicationChild;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.Main;
import org.spc.ofp.project.netcdfextractor.scene.FXMLUtils;

/**
 * Show information about a third party library.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class LibraryPane extends VBox implements Disposable, ApplicationChild<Main> {
    
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
        applicationProperty().unbind();
        setApplication(null);
    }
    
    /**
     * The parent application.
     */
    private final ObjectProperty<Main> application = new SimpleObjectProperty<>(this, "application"); // NOI18N.

    @Override
    public final Main getApplication() {
        return application.get();
    }
    
    @Override
    public final void setApplication(Main value) {
        application.set(value);
    }
    
    @Override
    public final ObjectProperty<Main> applicationProperty() {
        return application;
    }
    
    public void clearContent() {
        controller.ifPresent(c -> c.clearContent());
    }
    
    public void updateContent(final String library, final String version, final String owner, final String description, final URL homepage, final URL licenseFile) {
        controller.ifPresent(c -> c.updateContent(library, version, owner, description, homepage, licenseFile));
    }
}
