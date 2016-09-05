/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.extract;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParametersBuilder;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class ExtractConfigPaneController extends ControllerBase<ExtractConfigPane> {

    @FXML
    private Node rootPane;
    @FXML
    private TextField dirField;
    @FXML
    private Tooltip dirFieldTip;

    /**
     * Creates a new instance.
     */
    public ExtractConfigPaneController() {
    }

    @Override
    public void dispose() {
        try {
            if (dirFieldTip != null) {
                dirFieldTip.textProperty().unbind();
                dirFieldTip = null;
            }
        } finally {
            super.dispose();
        }
    }

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        dirFieldTip.textProperty().bind(dirField.textProperty());
    }

    @Override
    protected void uninstallNode(final ExtractConfigPane node) {
    }

    @Override
    protected void installNode(final ExtractConfigPane node) {
    }

    @FXML
    private void handleDirButton() {
        browseForDirectory();
    }

    private void browseForDirectory() {
        final String path = dirField.getText();
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            final String homePath = System.getProperty("user.home"); // NOI18N.
            dir = new File(homePath);
        }
        final DirectoryChooser dialog = new DirectoryChooser();
        dialog.setInitialDirectory(dir);
        final Optional<File> directoryOptional = Optional.ofNullable(dialog.showDialog(rootPane.getScene().getWindow()));
        directoryOptional.ifPresent(directory -> {
            dirField.setText(directory.getAbsolutePath());
            final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
            builder.destinationDir(Paths.get(directory.toURI()));
        });
    }
}
