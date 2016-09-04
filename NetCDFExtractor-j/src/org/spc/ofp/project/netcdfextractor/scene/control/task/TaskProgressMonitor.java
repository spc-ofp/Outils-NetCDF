/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.task;

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Worker;
import javafx.scene.layout.GridPane;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.scene.FXMLUtils;

/**
 * Monitors task progress.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class TaskProgressMonitor extends GridPane implements Disposable {

    private final Optional<TaskProgressMonitorController> controller;

    /**
     * Creates a new instance.
     */
    public TaskProgressMonitor() {
        super();
        setId("taskProgressMonitor"); // NOI18N.
        controller = FXMLUtils.INSTANCE.loadAndInject(getClass().getResource("TaskProgressMonitor.fxml"), this); // NOI18N.
    }

    @Override
    public void dispose() {
        FXMLUtils.INSTANCE.disposeController(controller);
    }

    /**
     * The worker that is being monitored.
     */
    private final ObjectProperty<Worker> worker = new SimpleObjectProperty<>(this, "worker"); // NOI18N.

    public Worker getWorker() {
        return worker.get();
    }

    public void setWorker(final Worker value) {
        worker.set(value);
    }

    public ObjectProperty<Worker> workerProperty() {
        return worker;
    }
}
