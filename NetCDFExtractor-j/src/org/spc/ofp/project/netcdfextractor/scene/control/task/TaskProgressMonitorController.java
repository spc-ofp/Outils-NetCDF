/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.task;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class TaskProgressMonitorController extends ControllerBase<TaskProgressMonitor> {

    @FXML
    private ProgressBar taskProgressBar;
    @FXML
    private Text taskTitleText;
    @FXML
    private Text taskMessageText;
    @FXML
    private Button stopTaskButton;

    /**
     * Creates a new instance.
     */
    public TaskProgressMonitorController() {
        super();
    }

    @Override
    public void dispose() {
        try {
        } finally {
            super.dispose();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @Override
    protected void uninstallNode(final TaskProgressMonitor node) {
        node.workerProperty().removeListener(workerChangeListener);
        Optional.ofNullable(node.getWorker())
                .ifPresent(this::uninstallWorker);
        stopTaskButton.setDisable(true);
    }

    @Override
    protected void installNode(final TaskProgressMonitor node) {
        Optional.ofNullable(node.getWorker())
                .ifPresent(this::installWorker);
        if (node.getWorker() == null) {
            stopTaskButton.setDisable(true);
        }
        node.workerProperty().addListener(workerChangeListener);
    }

    /**
     * Called whenever the worker changes.
     */
    private final ChangeListener<Worker> workerChangeListener = (observable, oldValue, newValue) -> {
        Optional.ofNullable(oldValue)
                .ifPresent(this::uninstallWorker);
        Optional.ofNullable(newValue)
                .ifPresent(this::installWorker);
        if (newValue == null) {
            stopTaskButton.setDisable(true);
        }
    };

    /**
     * Uninstall the worker.
     * <br>Default implementation does nothing.
     * @param worker The worker, never {@code null}.
     */
    protected void uninstallWorker(final Worker worker) {
        taskTitleText.textProperty().unbind();
        taskProgressBar.progressProperty().unbind();
        taskMessageText.textProperty().unbind();
        stopTaskButton.disableProperty().unbind();
    }

    /**
     * Install the worker.
     * <br>Default implementation does nothing.
     * @param worker The worker, never {@code null}.
     */
    protected void installWorker(final Worker worker) {
        taskTitleText.textProperty().bind(worker.titleProperty());
        taskProgressBar.progressProperty().bind(worker.progressProperty());
        taskMessageText.textProperty().bind(worker.messageProperty());
        stopTaskButton.disableProperty().bind(worker.runningProperty().not());
    }

    /**
     * Called whenever the stop button is clicked.
     */
    @FXML
    private void handleStopButton() {
        parentNode().ifPresent(node -> {
            final Optional<Worker> worker = Optional.ofNullable(node.getWorker());
            worker.ifPresent(Worker::cancel);
        });
    }
}
