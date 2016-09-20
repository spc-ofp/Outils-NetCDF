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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.spc.ofp.project.netcdfextractor.NetCDFExtractorConstants;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.scene.control.about.libraries.LibrariesPane;
import org.spc.ofp.project.netcdfextractor.text.UIMessageUtils;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class AboutPaneController extends ControllerBase<AboutPane> {
    @FXML
    private Text appTitleText;
    @FXML
    private Text appVersionText;
    @FXML
    private TextFlow appDescriptionFlow;
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
        appTitleText.setText(bundle.getString("app.title")); // NOI18N.
        //
        appVersionText.setText(NetCDFExtractorConstants.INSTANCE.getVersion());
        //
        final String descriptionText = bundle.getString("app.description"); // NOI18N.
        appDescriptionFlow.getChildren().setAll(UIMessageUtils.INSTANCE.split(descriptionText, this::showDocument));
        //
        librariesPane = new LibrariesPane();
        librariesPane.applicationProperty().bind(applicationProperty());
        //
        librariesStack.getChildren().add(librariesPane);
        //
        acknowledgementPaneController.applicationProperty().bind(applicationProperty());
    }
}
