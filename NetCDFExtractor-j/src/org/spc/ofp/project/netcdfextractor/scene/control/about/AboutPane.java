/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about;

import java.net.URL;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.BorderPane;
import org.spc.ofp.project.netcdfextractor.ApplicationChild;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.Main;
import org.spc.ofp.project.netcdfextractor.scene.FXMLUtils;

/**
 * The about pane.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class AboutPane extends BorderPane implements Disposable, ApplicationChild<Main> {

    private Optional<AboutPaneController> controller = Optional.empty();

    /**
     * Creates a new instance.
     */
    public AboutPane() {
        super();
        setId("aboutPane"); // NOI18N.
        final URL fxmlURL = getClass().getResource("AboutPane.fxml"); // NOI18N.
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

}
