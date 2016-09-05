/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.extract;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParameters;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParametersBuilder;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class ExtractConfigPaneController extends ControllerBase<ExtractConfigPane> {

    private static String CUSTOM_SEPARATOR = "custom"; // NOI18N.
    /**
     * Sets of default separators.
     */
    private static final Set<String> DEFAULT_SEPARATORS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            // Note: using a null value causes an IndexOutOfBoundsException.
            CUSTOM_SEPARATOR, " ", "\t", ",", ";", ":", "|", "/", "\\", "?", "!"))); // NOI18N.

    @FXML
    private Node rootPane;
    @FXML
    private TextField dirField;
    @FXML
    private Tooltip dirFieldTip;
    @FXML
    private ComboBox<String> separatorCombo;
    @FXML
    private TextField separatorField;
    @FXML
    private CheckBox singleOutputCheck;
    @FXML
    private CheckBox includeColumnHeaderCheck;

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
            if (separatorField != null) {
                separatorField.textProperty().removeListener(separatorChangeListener);
                separatorField.editableProperty().unbind();
            }
            if (separatorCombo != null) {
                separatorCombo.valueProperty().removeListener(separatorChangeListener);
            }
            if (singleOutputCheck != null) {
                singleOutputCheck.selectedProperty().removeListener(singleOuputChangeListener);
                singleOutputCheck = null;
            }
            if (includeColumnHeaderCheck != null) {
                includeColumnHeaderCheck.selectedProperty().removeListener(includeColumnHeaderChangeListener);
                includeColumnHeaderCheck = null;
            }
        } finally {
            super.dispose();
        }
    }

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        dirFieldTip.textProperty().bind(dirField.textProperty());
        //
        separatorField.textProperty().addListener(separatorChangeListener);
        separatorField.editableProperty().bind(new BooleanBinding() {
            {
                bind(separatorCombo.valueProperty());
            }

            @Override
            public void dispose() {
                unbind(separatorCombo.valueProperty());
            }

            @Override
            protected boolean computeValue() {
                final String separator = separatorCombo.getValue();
                return CUSTOM_SEPARATOR.equals(separator);
            }
        });
        //
        separatorCombo.getItems().setAll(DEFAULT_SEPARATORS);
        separatorCombo.getSelectionModel().select(BatchExtractToTxtParameters.DEFAULT_SEPARATOR);
        separatorCombo.valueProperty().addListener(separatorChangeListener);
        //
        singleOutputCheck.selectedProperty().addListener(singleOuputChangeListener);
        //
        includeColumnHeaderCheck.selectedProperty().addListener(includeColumnHeaderChangeListener);
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

    /**
     * Called whenever the separator selection changes.
     */
    private final ChangeListener<String> separatorChangeListener = (observable, oldValue, newValue) -> {
        final String comboSeparator = separatorCombo.getValue();
        final String fieldSeparator = separatorField.getText();
        final String separator = (CUSTOM_SEPARATOR.equals(comboSeparator)) ? fieldSeparator : comboSeparator;
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        builder.separator(separator);
    };

    /**
     * Called whenever the single output checkbox changes state.
     */
    private final ChangeListener<Boolean> singleOuputChangeListener = (observable, oldValue, newValue) -> {
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        builder.singleDocument(newValue);
    };

    /**
     * Called whenever the include column header checkbox changes state.
     */
    private final ChangeListener<Boolean> includeColumnHeaderChangeListener = (observable, oldValue, newValue) -> {
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        builder.includeColumnHeader(newValue);
    };

}
