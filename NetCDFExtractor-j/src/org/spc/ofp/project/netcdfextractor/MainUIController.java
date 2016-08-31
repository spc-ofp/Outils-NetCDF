/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.stage.DirectoryChooser;
import org.spc.ofp.project.netcdfextractor.cell.NetCDFTreeCell;
import org.spc.ofp.project.netcdfextractor.data.FileInfo;
import org.spc.ofp.project.netcdfextractor.data.VariableInfo;
import org.spc.ofp.project.netcdfextractor.task.ImageGenerationTask;
import org.spc.ofp.project.netcdfextractor.task.NavigationTreeConstructionTask;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class MainUIController implements Initializable {

    @FXML
    private Node rootPane;
    @FXML
    private TextField dirField;
    @FXML
    private Tooltip dirFieldTip;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private TreeView treeView;
    @FXML
    private ImageView imageView;

    /**
     * Creates a new instance.
     */
    public MainUIController() {
    }

    @Override
    public void initialize(final URL location, final ResourceBundle bundle) {
        final String homePath = System.getProperty("user.home"); // NOI18N.
        final String path = prefs.get("last.directory", homePath); // NOI18N.
        dirField.setText(path);
        dirField.textProperty().addListener(dirChangeListener);
        //
        dirFieldTip.textProperty().bind(dirField.textProperty());
        //
        treeView.getSelectionModel().selectedItemProperty().addListener(selectedTreeItemInvalidationListener);
        treeView.setCellFactory(treeView -> new NetCDFTreeCell());
        //
        Platform.runLater(() -> {
            final File dir = new File(path);
            doLoadFilesAsync(dir);
        });
    }

    @FXML
    private void handleOpenItem() {
        browseForDirectory();
    }

    @FXML
    private void handleCloseItem() {
        doCloseCurrentDisplay();
    }

    @FXML
    private void handleExitItem() {
        doCloseCurrentDisplay();
        Platform.exit();
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
        directoryOptional.ifPresent(directory -> dirField.setText(directory.getAbsolutePath()));
    }

    ////////////////////////////////////////////////////////////////////////////
    /** 
     * Access to user preferences.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());

    /**
     * Called whenever the directory set by the user is edited.
     */
    private final ChangeListener<String> dirChangeListener = (observable, oldValue, newValue) -> {
        final File dir = new File(newValue);
        if (dir.exists() && dir.isDirectory()) {
            prefs.put("last.directory", dir.getAbsolutePath());
        }
        doLoadFilesAsync(dir);
    };

    /**
     * Called when the selection in the tree changed.
     */
    private final InvalidationListener selectedTreeItemInvalidationListener = observable -> {
        final Optional<TreeItem> selectionOptional = Optional.ofNullable((TreeItem) treeView.getSelectionModel().getSelectedItem());
        selectionOptional.ifPresent(item -> {
            final Object value = item.getValue();
            System.out.println(value);
            if (value instanceof FileInfo) {
            } else if (value instanceof VariableInfo) {
                final VariableInfo variableInfo = (VariableInfo) value;
                final FileInfo fileInfo = (FileInfo) item.getParent().getValue();
                final Path file = fileInfo.getFile();
                final String variableName = variableInfo.getFullName();
                doGenerateImageAsync(file, variableName);
            }
        });
    };

    ////////////////////////////////////////////////////////////////////////////
    /**
     * Close current display and stop all pending services.
     */
    private void doCloseCurrentDisplay() {
        stopLoadFiles();
        stopGenerateImage();
    }

    /**
     * Stop load files service and clear files display.
     */
    private void stopLoadFiles() {
        loadFilesServiceOptional.ifPresent(service -> {
            service.cancel();
            cleanupGenerateImage();
        });
        treeView.setRoot(null);
    }

    /**
     * Stop image generation service and clear image display.
     */
    private void stopGenerateImage() {
        generateImageServiceOptional.ifPresent(service -> {
            service.cancel();
            cleanupGenerateImage();
        });
        imageView.setImage(null);
    }

    ////////////////////////////////////////////////////////////////////////////
    private Optional<Service> loadFilesServiceOptional = Optional.empty();

    /**
     * Called at the end of the files load service.
     */
    private void cleanupLoadFiles() {
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        loadFilesServiceOptional = Optional.empty();
    }

    private void doLoadFilesAsync(final File directory) {
        doCloseCurrentDisplay();
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        final Service<Object> service = new Service() {

            @Override
            protected Task createTask() {
                return new NavigationTreeConstructionTask(directory.toPath());
            }
        };
        service.setOnSucceeded(workerStateEvent -> {
            final TreeItem root = (TreeItem) workerStateEvent.getSource().getValue();
            treeView.setRoot(root);
            cleanupLoadFiles();
        });
        service.setOnFailed(workerStateEvent -> {
            final Throwable ex = workerStateEvent.getSource().getException();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            cleanupLoadFiles();
        });
        progressBar.progressProperty().bind(service.progressProperty());
        loadFilesServiceOptional = Optional.of(service);
        service.start();
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * Called at the end of the generate image service.
     */
    private Optional<Service> generateImageServiceOptional = Optional.empty();

    private void cleanupGenerateImage() {
        generateImageServiceOptional = Optional.empty();
    }

    private void doGenerateImageAsync(final Path file, final String variableName) {
        stopGenerateImage();
        //
        generateImageServiceOptional.ifPresent(service -> {
            service.cancel();
            cleanupGenerateImage();
        });
        imageView.setImage(null);
        //
        final Service<Image> service = new Service<Image>() {

            @Override
            protected Task<Image> createTask() {
                // Reusing same image leads to visual artefacts.
                return new ImageGenerationTask(file, variableName, null);
            }
        };
        service.setOnSucceeded(workerStateEvent -> {
            final WritableImage image = (WritableImage) workerStateEvent.getSource().getValue();
            imageView.setImage(image);
            cleanupGenerateImage();
        });
        service.setOnFailed(workerStateEvent -> {
            final Throwable ex = workerStateEvent.getSource().getException();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            cleanupGenerateImage();
        });
        generateImageServiceOptional = Optional.of(service);
        service.start();
    }
}
