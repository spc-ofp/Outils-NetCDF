/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.text.TextFlow;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.text.UIMessageUtils;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int) */
public final class AcknowledgementPaneController extends ControllerBase {

    @FXML
    private TextFlow spcAboutFlow;

    @Override
    public void dispose() {
        try {
            if (spcAboutFlow != null) {
                spcAboutFlow.getChildren().clear();
                spcAboutFlow = null;
            }
        } finally {
            super.dispose();
        }
    }

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        final String spcAboutText = bundle.getString("about.spc.message"); // NOI18N.
        spcAboutFlow.getChildren().setAll(UIMessageUtils.INSTANCE.split(spcAboutText, this::showDocument));
    }
}
