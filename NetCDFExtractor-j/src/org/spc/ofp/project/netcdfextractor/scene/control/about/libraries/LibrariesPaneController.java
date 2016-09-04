/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about.libraries;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;

/**
 * FXML controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class LibrariesPaneController extends ControllerBase<LibrariesPane> {

    @FXML
    private VBox rootPane;
    @FXML
    private TextFlow messageFlow;
    @FXML
    private TabPane tabPane;

    private LibraryPane libraryPane;

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        libraryPane = new LibraryPane();
        libraryPane.applicationProperty().bind(applicationProperty());
        //
        tabPane.getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
        //
        Platform.runLater(() -> populateLibraries());
    }

    @Override
    public void dispose() {
        try {
            tabPane.getSelectionModel().selectedItemProperty().removeListener(tabChangeListener);
            if (libraryPane != null) {
                libraryPane.dispose();
                libraryPane = null;
            }
        } finally {
            super.dispose();
        }
    }

    final Properties librariesProperties = new Properties();

    /**
     * Called whenever active tab changes.
     */
    private final ChangeListener<Tab> tabChangeListener = (observable, oldValue, newValue) -> {
        Optional.ofNullable(oldValue)
                .ifPresent(this::uninstallTab);
        Optional.ofNullable(newValue)
                .ifPresent(this::installTab);
    };

    private void populateLibraries() {
        try (final InputStream input = getClass().getResourceAsStream("libraries.properties")) { // NOI18N.
            librariesProperties.load(input);
        } catch (IOException ex) {
            Logger.getLogger(LibrariesPaneController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        final String librariesDef = librariesProperties.getProperty("libraries"); // NOI18N.
        if (librariesDef == null || librariesDef.trim().isEmpty()) {
            return;
        }
        // Create tabs.
        final String[] libraries = librariesDef.trim().split("\\s+"); // NOI18N.
        tabPane.getTabs().setAll(Arrays.stream(libraries)
                .map(this::createTabForLibrary)
                .toArray(Tab[]::new));
    }

    /**
     * Create a tab for given library.
     * @param library The library.
     * @return A {@code Tab} instance, never {@code null}.
     */
    private Tab createTabForLibrary(final String library) {
        final Tab tab = new Tab();
        final String libraryName = librariesProperties.getProperty(String.format("%s.name", library)); // NOI18N.
        tab.setText(libraryName);
        tab.setUserData(library);
        return tab;
    }

    private void uninstallTab(final Tab tab) {
        tab.setContent(null);
        libraryPane.clearContent();
    }

    private void installTab(final Tab tab) {
        final String library = (String) tab.getUserData();
        final String libraryName = librariesProperties.getProperty(String.format("%s.name", library)); // NOI18N.
        final String libraryVersion = librariesProperties.getProperty(String.format("%s.version", library)); // NOI18N.
        final String libraryOwner = librariesProperties.getProperty(String.format("%s.owner", library)); // NOI18N.
        final String libraryDescription = librariesProperties.getProperty(String.format("%s.description", library)); // NOI18N.
        final String libraryHomepage = librariesProperties.getProperty(String.format("%s.url", library)); // NOI18N.
        URL libraryURL = null;
        try {
            libraryURL = new URL(libraryHomepage);
        } catch (MalformedURLException ex) {
            Logger.getLogger(LibrariesPaneController.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        }
        final String libraryLicenseFile = librariesProperties.getProperty(String.format("%s.license", library)); // NOI18N.
        URL licenseURL = null;
        licenseURL = getClass().getResource(libraryLicenseFile);
        libraryPane.updateContent(libraryName, libraryVersion, libraryOwner, libraryDescription, libraryURL, licenseURL);
        tab.setContent(libraryPane);
    }
}
