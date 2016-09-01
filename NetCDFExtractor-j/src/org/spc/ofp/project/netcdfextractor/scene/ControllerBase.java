/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spc.ofp.project.netcdfextractor.scene;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.Initializable;
import org.spc.ofp.project.netcdfextractor.ApplicationChild;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.Main;

/**
 * Base class for FXML controllers.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public abstract class ControllerBase implements Initializable, Disposable, ApplicationChild<Main> {

    /** 
     * Creates a new instance.
     */
    public ControllerBase() {
    }

    @Override
    public void dispose() {
        applicationProperty().unbind();
        setApplication(null);
    }

    /**
     * The parent application.
     */
    private final ObjectProperty<Main> application = new SimpleObjectProperty<>(this, "application"); // NOI18N.

    @Override
    public final Main getApplication() {
        return application.get();
    }

    @Override
    public final void setApplication(Main value) {
        application.set(value);
    }

    @Override
    public final ObjectProperty<Main> applicationProperty() {
        return application;
    }
}
