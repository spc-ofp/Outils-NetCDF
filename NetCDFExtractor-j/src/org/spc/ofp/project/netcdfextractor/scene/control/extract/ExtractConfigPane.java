/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.extract;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import javafx.scene.layout.GridPane;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.scene.FXMLUtils;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParameters;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParametersBuilder;

/**
 * Configure the extraction.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class ExtractConfigPane extends GridPane implements Disposable {

    private final Optional<ExtractConfigPaneController> controller;

    /**
     * Creates a new instance.
     */
    public ExtractConfigPane() {
        super();
        setId("extractConfigPane"); // NOI18N.
        controller = FXMLUtils.INSTANCE.loadAndInject(getClass().getResource("ExtractConfigPane.fxml"), this); // NOI18N.
    }

    @Override
    public void dispose() {
        FXMLUtils.INSTANCE.disposeController(controller);
    }

    /**
     * The parameters builder object.
     */
    private final BatchExtractToTxtParametersBuilder parametersBuilder = BatchExtractToTxtParametersBuilder.create();

    BatchExtractToTxtParametersBuilder getParametersBuilder() {
        return parametersBuilder;
    }

    public void addVariable(final Path source, final String variable) {
        Objects.requireNonNull(source);
        getParametersBuilder().addVariable(source, variable);
        controller.ifPresent(ExtractConfigPaneController::requestUpdateUI);
    }

    public BatchExtractToTxtParameters createParameters() {
        return getParametersBuilder().build();
    }
}
