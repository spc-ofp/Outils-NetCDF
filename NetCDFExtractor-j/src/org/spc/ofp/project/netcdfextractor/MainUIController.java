/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.scenicview.ScenicView;
import org.spc.ofp.project.netcdfextractor.scene.control.cell.NetCDFTreeCell;
import org.spc.ofp.project.netcdfextractor.data.FileInfo;
import org.spc.ofp.project.netcdfextractor.data.VariableInfo;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.scene.control.about.AboutPane;
import org.spc.ofp.project.netcdfextractor.scene.control.dialog.DialogUtils;
import org.spc.ofp.project.netcdfextractor.scene.control.extract.ExtractConfigPane;
import org.spc.ofp.project.netcdfextractor.scene.control.task.TaskProgressMonitor;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParameters;
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
    // Select all.
    @FXML
    private MenuItem selectAllVariablesItem;
    @FXML
    private Button selectAllVariablesButton;
    // Extract
    @FXML
    private MenuItem extractItem;
    @FXML
    private Button extractButton;

    private TaskProgressMonitor taskProgressMonitor;

    /**
     * Creates a new instance.
     */
    public MainUIController() {
    }

    @Override
    public void dispose() {
        try {
            // Stop all running services.
            loadFilesServiceOptional.ifPresent(Service::cancel);
            generateVariableImageServiceOptional.ifPresent(Service::cancel);
            generateVariableInfoServiceOptional.ifPresent(Service::cancel);
            exportServices.stream()
                    .forEach(Service::cancel);
            //
            if (dirField != null) {
                dirField.textProperty().removeListener(dirChangeListener);
                dirField = null;
            }
            if (dirFieldTip != null) {
                dirFieldTip.textProperty().unbind();
                dirFieldTip = null;
            }
            if (treeView != null) {
                treeView.rootProperty().removeListener(treeRootChangeListener);
                treeView.getSelectionModel().selectedItemProperty().removeListener(selectedTreeItemInvalidationListener);
                treeView.setRoot(null);
                treeView = null;
            }
            if (selectAllVariablesItem != null) {
                selectAllVariablesItem.disableProperty().unbind();
                selectAllVariablesItem = null;
            }
            if (selectAllVariablesButton != null) {
                selectAllVariablesButton.disableProperty().unbind();
                selectAllVariablesButton = null;
            }
            if (extractItem != null) {
                extractItem.disableProperty().unbind();
                extractItem = null;
            }
            if (extractButton != null) {
                extractButton.disableProperty().unbind();
                extractButton = null;
            }
            if (taskProgressMonitor != null) {
                taskProgressMonitor.dispose();
                taskProgressMonitor = null;
            }
        } finally {
            super.dispose();
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle bundle) {
        dirField.textProperty().addListener(dirChangeListener);
        dirField.setOnKeyReleased(dirKeyListener);
        //
        dirFieldTip.textProperty().bind(dirField.textProperty());
        // Select all.
        selectAllVariablesItem.setDisable(true);
        selectAllVariablesButton.setDisable(true);
        // Extract.
        extractItem.setDisable(true);
        extractButton.setDisable(true);
        //
        treeView.rootProperty().addListener(treeRootChangeListener);
        treeView.getSelectionModel().selectedItemProperty().addListener(selectedTreeItemInvalidationListener);
        treeView.setCellFactory(treeView -> new NetCDFTreeCell());
        //
        Platform.runLater(() -> {
            final String homePath = System.getProperty("user.home"); // NOI18N.
            final String dirPath = prefs.get("last.directory", homePath); // NOI18N.
            final Path directory = Paths.get(dirPath);
            doLoadFiles(directory);
        });
    }

    @FXML
    private void handleOpenItem() {
        doBrowseForDirectory();
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
        doBrowseForDirectory();
    }

    @FXML
    private void handleExtractItem() {
        doExportFiles();
    }

    @FXML
    private void handleExtractButton() {
        doExportFiles();
    }

    @FXML
    private void handleRefreshViewItem() {
        doRefreshView();
    }

    @FXML
    private void handleRefreshViewButton() {
        doRefreshView();
    }

    @FXML
    private void handleAboutItem() {
        final AboutPane aboutPane = new AboutPane();
        aboutPane.applicationProperty().bind(applicationProperty());
        final Dialog dialog = DialogUtils.INSTANCE.create(rootPane.getScene().getWindow(),
                Modality.NONE, StageStyle.UNDECORATED, null,
                Main.I18N.getString("about.title"), // NOI18N.
                aboutPane,
                ButtonType.CLOSE);
        ScenicView.show(dialog.getDialogPane());
        dialog.showAndWait();
        aboutPane.dispose();
    }

    @FXML
    private void handleSelectAllVariablesItem() {
        doSelectAllVariables();
    }

    @FXML
    private void handleSelectAllVariablesButton() {
        doSelectAllVariables();
    }

    private void doRefreshView() {
        final Path directory = Paths.get(dirField.getText());
        doLoadFiles(directory);
    }

    /**
     * Select the directory.
     */
    private void doBrowseForDirectory() {
        final String path = dirField.getText();
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            final String homePath = System.getProperty("user.home"); // NOI18N.
            dir = new File(homePath);
        }
        final DirectoryChooser dialog = new DirectoryChooser();
        dialog.setInitialDirectory(dir);
        final Optional<File> directoryOptional = Optional.ofNullable(dialog.showDialog(rootPane.getScene().getWindow()));
        directoryOptional.ifPresent(directory -> doLoadFiles(directory.toPath()));
    }

    /**
     * Select all variables.
     */
    private void doSelectAllVariables() {
        final TreeItem<Object> root = treeView.getRoot();
        if (root == null) {
            return;
        }
        root.getChildren()
                .stream()
                .forEach(fileItem -> fileItem.getChildren()
                        .stream()
                        .map(TreeItem::getValue)
                        .map(object -> (VariableInfo) object)
                        .forEach(variableInfo -> variableInfo.setSelected(true)));
    }

    ////////////////////////////////////////////////////////////////////////////
    /** 
     * Access to user preferences.
     */
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());

    /** 
     * Called whenever the root of the tree view is replaced.
     */
    private final ChangeListener<TreeItem> treeRootChangeListener = (observable, oldValue, newValue) -> {
        // Unbind control and put them in a disabled state.
        Optional.ofNullable(oldValue).ifPresent(oldRoot -> {
            // Select all.
            selectAllVariablesItem.disableProperty().unbind();
            selectAllVariablesItem.setDisable(true);
            selectAllVariablesButton.disableProperty().unbind();
            selectAllVariablesButton.setDisable(true);
            // Extract
            extractItem.disableProperty().unbind();
            extractItem.setDisable(true);
            extractButton.disableProperty().unbind();
            extractButton.setDisable(true);
        });
        // Bind control to the size of the root's children list.
        Optional.ofNullable(newValue).ifPresent(newRoot -> {
            final BooleanBinding treeHasNoContent = Bindings.isEmpty(newRoot.getChildren());
            // Select all.
            selectAllVariablesItem.disableProperty().bind(treeHasNoContent);
            selectAllVariablesButton.disableProperty().bind(treeHasNoContent);
            // Extract.
            extractItem.disableProperty().bind(treeHasNoContent);
            extractButton.disableProperty().bind(treeHasNoContent);
        });
    };

    private boolean dirEditing = false;

    /**
     * Called whenever the directory set by the user is edited.
     */
    private final ChangeListener<String> dirChangeListener = (observable, oldValue, newValue) -> {
        if (dirEditing) {
            return;
        }
        doRefreshView();
    };

    private final EventHandler<KeyEvent> dirKeyListener = (event) -> {
        switch (event.getCode()) {
            case ENTER: {
                doRefreshView();
            }
            break;
        }
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

    public void doLoadFiles(final Path directory) {
        try {
            dirEditing = true;
            final String dirPath = directory.toString();
            dirField.setText(dirPath);
            prefs.put("last.directory", dirPath);
            doLoadFilesAsync(directory);
        } finally {
            dirEditing = false;
        }
    }

    private void doLoadFilesAsync(final Path directory) {
        doCloseCurrentDisplay();
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return;
        }
        final Service<Object> service = new Service() {

            @Override
            protected Task createTask() {
                return new NavigationTreeConstructionTask(directory);
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
    private void doExportFiles() {
        final TreeItem<Object> root = treeView.getRoot();
        if (root == null) {
            return;
        }
        // Check if there is anything to export.
        final boolean canExport = root.getChildren()
                .stream()
                .map(fileItem -> fileItem.getChildren()
                        .stream()
                        .map(variableItem -> (VariableInfo) variableItem.getValue())
                        .map(variableInfo -> variableInfo.isSelected())
                        .reduce(false, (a, b) -> a || b))
                .reduce(false, (a, b) -> a || b);
        if (!canExport) {
            return;
        }
        // Prepare export dialog.
        final ExtractConfigPane extractConfigPane = new ExtractConfigPane();
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
                                .forEach(variable -> extractConfigPane.addVariable(file, variable));
                    }
                });
        final Dialog dialog = DialogUtils.INSTANCE.create(rootPane.getScene().getWindow(),
                Main.I18N.getString("extract.title"), // NOI18N.
                extractConfigPane,
                ButtonType.OK, ButtonType.CANCEL);
        final Optional<ButtonType> result = dialog.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                final BatchExtractToTxtParameters parameters = extractConfigPane.createParameters();
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
        connectToTaskProgressMonitor(service);
        service.start();
    }

    /**    
     *
     */
    private void connectToTaskProgressMonitor(final Worker worker) {
        // Initialize the monitor if needed.
        if (taskProgressMonitor == null) {
            taskProgressMonitor = new TaskProgressMonitor();
            rootPane.setBottom(taskProgressMonitor);
        }
        taskProgressMonitor.setWorker(worker);
    }
}
