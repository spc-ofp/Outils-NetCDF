/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import org.spc.ofp.project.netcdfextractor.scene.control.cell.NetCDFTreeCell;
import org.spc.ofp.project.netcdfextractor.data.FileInfo;
import org.spc.ofp.project.netcdfextractor.data.VariableInfo;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.scene.control.about.libraries.LibrariesPane;
import org.spc.ofp.project.netcdfextractor.scene.control.dialog.DialogUtils;
import org.spc.ofp.project.netcdfextractor.scene.control.extract.ExtractConfigPane;
import org.spc.ofp.project.netcdfextractor.scene.control.task.TaskProgressMonitor;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParameters;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParametersBuilder;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtTask;
import org.spc.ofp.project.netcdfextractor.task.VariableImageGenerationTask;
import org.spc.ofp.project.netcdfextractor.task.NavigationTreeConstructionTask;
import org.spc.ofp.project.netcdfextractor.task.VariableHTMLReportTask;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class MainUIController extends ControllerBase {

    @FXML
    private BorderPane rootPane;
    @FXML
    private TextField dirField;
    @FXML
    private Tooltip dirFieldTip;
    @FXML
    private SplitPane splitPane;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TreeView treeView;
    @FXML
    private WebView infoWebView;
    @FXML
    private ImageView imageView;

    private TaskProgressMonitor taskProgressMonitor;

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
        taskProgressMonitor = new TaskProgressMonitor();
        rootPane.setBottom(taskProgressMonitor);
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

    @FXML
    private void handleExtractItem() {
        exportFiles();
    }

    @FXML
    void handleAboutItem() {
        final LibrariesPane librariesPane = new LibrariesPane();
        librariesPane.applicationProperty().bind(applicationProperty());
        final Dialog dialog = DialogUtils.INSTANCE.create(rootPane.getScene().getWindow(),
                Main.I18N.getString("about.title"), // NOI18N.
                librariesPane,
                ButtonType.CLOSE);
        dialog.showAndWait();
        librariesPane.dispose();
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
                doGenerateVariableInfoAsync(file, variableName);
                doGenerateVariableImageAsync(file, variableName);
            }
        });
    };

    ////////////////////////////////////////////////////////////////////////////
    /**
     * Close current display and stop all pending services.
     */
    private void doCloseCurrentDisplay() {
        stopLoadFiles();
        stopGenerateVariableInfo();
        stopGenerateVariableImage();
        //
        splitPane.setVisible(false);
        progressIndicator.setVisible(true);
    }

    /**
     * Stop load files service and clear files display.
     */
    private void stopLoadFiles() {
        loadFilesServiceOptional.ifPresent(service -> {
            service.cancel();
            cleanupGenerateVariableImage();
        });
        treeView.setRoot(null);
    }

    /**
     * Stop image generation service and clear image display.
     */
    private void stopGenerateVariableImage() {
        generateVariableImageServiceOptional.ifPresent(service -> {
            service.cancel();
            cleanupGenerateVariableImage();
        });
        imageView.setImage(null);
    }

    /**
     * Stop info generation service and clear image display.
     */
    private void stopGenerateVariableInfo() {
        generateVariableInfoServiceOptional.ifPresent(service -> {
            service.cancel();
            cleanupGenerateVariableInfo();
        });
        infoWebView.getEngine().loadContent(null);
    }

    ////////////////////////////////////////////////////////////////////////////
    private Optional<Service> loadFilesServiceOptional = Optional.empty();

    /**
     * Called at the end of the files load service.
     */
    private void cleanupLoadFiles() {
        loadFilesServiceOptional = Optional.empty();
        splitPane.setVisible(true);
        progressIndicator.setVisible(false);
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
        loadFilesServiceOptional = Optional.of(service);
        service.start();
    }

    ////////////////////////////////////////////////////////////////////////////
    private Optional<Service<String>> generateVariableInfoServiceOptional = Optional.empty();

    /**
     * Called at the end of the generate info service.
     */
    private void cleanupGenerateVariableInfo() {
        generateVariableInfoServiceOptional = Optional.empty();
    }

    private void doGenerateVariableInfoAsync(final Path file, final String variableName) {
        stopGenerateVariableInfo();
        //
        final Service<String> service = new Service<String>() {

            @Override
            protected Task<String> createTask() {
                return new VariableHTMLReportTask(file, variableName);
            }
        };
        service.setOnSucceeded(workerStateEvent -> {
            final String report = (String) workerStateEvent.getSource().getValue();
            infoWebView.getEngine().loadContent(report);
            cleanupGenerateVariableInfo();
        });
        service.setOnFailed(workerStateEvent -> {
            final Throwable ex = workerStateEvent.getSource().getException();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            cleanupGenerateVariableInfo();
        });
        generateVariableInfoServiceOptional = Optional.of(service);
        service.start();
    }

    private Optional<Service<Image>> generateVariableImageServiceOptional = Optional.empty();

    /**
     * Called at the end of the generate image service.
     */
    private void cleanupGenerateVariableImage() {
        generateVariableImageServiceOptional = Optional.empty();
    }

    private void doGenerateVariableImageAsync(final Path file, final String variableName) {
        stopGenerateVariableImage();
        //
        final Service<Image> service = new Service<Image>() {

            @Override
            protected Task<Image> createTask() {
                // Reusing same image leads to visual artefacts.
                return new VariableImageGenerationTask(file, variableName, null);
            }
        };
        service.setOnSucceeded(workerStateEvent -> {
            final WritableImage image = (WritableImage) workerStateEvent.getSource().getValue();
            imageView.setImage(image);
            cleanupGenerateVariableImage();
        });
        service.setOnFailed(workerStateEvent -> {
            final Throwable ex = workerStateEvent.getSource().getException();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            cleanupGenerateVariableImage();
        });
        generateVariableImageServiceOptional = Optional.of(service);
        service.start();
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * Stores all export services while they are running (to avoid early GC).
     */
    private final List<Service> exportServices = new LinkedList();

    /**
     * Export selected files.
     */
    private void exportFiles() {
        final TreeItem<Object> root = treeView.getRoot();
        if (root == null) {
            return;
        }
        final ExtractConfigPane extractConfigPane = new ExtractConfigPane();
        final BatchExtractToTxtParametersBuilder builder = extractConfigPane.getParametersBuilder();
        builder.clearAllFiles();
        root.getChildren()
                .stream()
                .forEach(fileItem -> {
                    final FileInfo fileInfo = (FileInfo) fileItem.getValue();
                    final Path file = fileInfo.getFile();
                    final String[] variables = fileItem.getChildren()
                            .stream()
                            .map(variableItem -> (VariableInfo) variableItem.getValue())
                            .filter(variableInfo -> variableInfo.isSelected())
                            .map(VariableInfo::getFullName)
                            .toArray(String[]::new);
                    if (variables.length > 0) {
                        Arrays.stream(variables)
                                .forEach(variable -> builder.addVariable(file, variable));
                    }
                });
        final Dialog dialog = DialogUtils.INSTANCE.create(rootPane.getScene().getWindow(),
                Main.I18N.getString("extract.title"), // NOI18N.
                extractConfigPane,
                ButtonType.OK, ButtonType.CANCEL);
        final Optional<ButtonType> result = dialog.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                final BatchExtractToTxtParameters parameters = builder.build();
                if (!parameters.isEmpty()) {
                    doExportFilesAsync(parameters);
                }
            }
        });
        extractConfigPane.dispose();
    }

    /**
     * Do the export asynchronously.
     * @param files Files to export.
     */
    private void doExportFilesAsync(final BatchExtractToTxtParameters parameters) {
        final Service<Void> service = new Service() {
            @Override
            protected Task createTask() {
                return new BatchExtractToTxtTask(parameters);
            }
        };
        service.setOnSucceeded(workerStateEvent -> {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Extraction succeeded."); // NOI18N.
            exportServices.remove(service);
        });
        service.setOnCancelled(workerStateEvent -> {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Extraction cancelled."); // NOI18N.
            exportServices.remove(service);
        });
        service.setOnFailed(workerStateEvent -> {
            final Throwable ex = workerStateEvent.getSource().getException();
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            exportServices.remove(service);
        });
        exportServices.add(service);
        taskProgressMonitor.setWorker(service);
        service.start();
    }
}
