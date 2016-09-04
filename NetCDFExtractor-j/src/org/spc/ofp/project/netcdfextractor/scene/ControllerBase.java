/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spc.ofp.project.netcdfextractor.scene;

import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import org.spc.ofp.project.netcdfextractor.ApplicationChild;
import org.spc.ofp.project.netcdfextractor.Disposable;
import org.spc.ofp.project.netcdfextractor.Main;

/**
 * Base class for FXML controllers.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 * @param <T> The type of the parent node.
 */
public abstract class ControllerBase<T extends Node> implements Initializable, Disposable, ApplicationChild<Main> {

    /** 
     * Creates a new instance.
     */
    public ControllerBase() {
        nodeProperty().addListener(nodeChangeListener);
    }

    @Override
    public void dispose() {
        applicationProperty().unbind();
        setApplication(null);
        nodeProperty().unbind();
        setNode(null);
        nodeProperty().removeListener(nodeChangeListener);
    }

    /**
     * Called whenever the parent node value changes.
     */
    private final ChangeListener<T> nodeChangeListener = (observable, oldValue, newValue) -> {
        Optional.ofNullable(oldValue)
                .ifPresent(this::uninstallNode);
        Optional.ofNullable(newValue)
                .ifPresent(this::installNode);
    };

    /**
     * Uninstall the parent node.
     * <br>Default implementation does nothing.
     * @param node The parent node, never {@code null}.
     */
    protected void uninstallNode(final T node) {
    }

    /**
     * Install the parent node.
     * <br>Default implementation does nothing.
     * @param node The parent node, never {@code null}.
     */
    protected void installNode(final T node) {
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
    private final ObjectProperty<T> node = new SimpleObjectProperty<>(this, "node"); // NOI18N.

    public final T getNode() {
        return node.get();
    }

    public final void setNode(T value) {
        node.set(value);
    }

    public final ObjectProperty<T> nodeProperty() {
        return node;
    }

    /**
     * Gets the parent node.
     * @return An {@code Optional<T>} instance, never {@code null}.
     */
    public final Optional<T> parentNode() {
        return Optional.ofNullable(getNode());
    }
}
