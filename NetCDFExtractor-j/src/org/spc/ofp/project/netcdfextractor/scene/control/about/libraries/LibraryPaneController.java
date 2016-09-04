/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.about.libraries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import org.spc.ofp.project.netcdfextractor.Main;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;

/**
 * FXML controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class LibraryPaneController extends ControllerBase<LibraryPane> {

    @FXML
    private Text titleText;
    @FXML
    private Text versionText;
    @FXML
    private Text ownerText;
    @FXML
    private Text descriptionText;
    @FXML
    private Hyperlink homeLink;
    @FXML
    private Text licenseText;
    @FXML
    private TextArea licenseArea;

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        clearContent();
    }

    @Override
    public void dispose() {
        try {            
        } finally {
            super.dispose();
        }
    }

    public void clearContent() {
        titleText.setText(null);
        versionText.setText(null);
        ownerText.setText(null);
        descriptionText.setText(null);
        homeLink.setOnAction(null);
        homeLink.setVisible(false);
        licenseText.setVisible(false);
        licenseArea.setText(null);
    }

    public void updateContent(final String library, final String version, final String owner, final String description, final URL homepage, final URL licenseFile) {
        titleText.setText(library);
        versionText.setText(version);
        ownerText.setText(owner);
        descriptionText.setText(description);
        homeLink.setOnAction(null);
        homeLink.setVisible(homepage != null);
        if (homepage != null) {
            homeLink.setOnAction(actionEvent -> {
                final Optional<Main> application = Optional.ofNullable(getApplication());
                application.ifPresent(a -> a.getHostServices().showDocument(homepage.toExternalForm()));
            });
        }
        if (licenseFile == null) {
            licenseText.setVisible(false);
            licenseArea.setText(null);
        } else {
            licenseText.setVisible(true);
            loadLicenseAsync(licenseFile);
        }
    }

    private Optional<Service> licenseLoader = Optional.empty();

    private void loadLicenseAsync(final URL licenseFile) {
        licenseLoader.ifPresent(Service::cancel);
        final Service<String> service = new Service<String>() {
            @Override
            protected Task<String> createTask() {
                return new Task<String>() {
                    @Override
                    protected String call() throws Exception {
                        String result = null;
                        try (final InputStream input = licenseFile.openStream();
                                final InputStreamReader reader = new InputStreamReader(input);
                                final LineNumberReader in = new LineNumberReader(reader)) {
                            final StringBuilder builder = new StringBuilder();
                            for (String line = in.readLine(); line != null; line = in.readLine()) {
                                builder.append(line);
                                builder.append('\n'); // NOI18N.
                                if (isCancelled()) {
                                    return null;
                                }
                            }
                            result = builder.toString();
                        }
                        return result;
                    }
                };
            }
        };
        service.setOnSucceeded(workerStateEvent -> {
            final String value = (String) workerStateEvent.getSource().getValue();
            licenseArea.setText(value);
        });
        service.setOnFailed(workerStateEvent -> {
            final Throwable ex = workerStateEvent.getSource().getException();
            Logger.getLogger(LibraryPaneController.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
        });
        licenseLoader = Optional.of(service);
        service.start();
    }
}
