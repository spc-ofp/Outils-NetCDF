/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Application class.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 * @version 1.0
 */
public final class Main extends Application {

    public static final ResourceBundle I18N = ResourceBundle.getBundle("org/spc/ofp/project/netcdfextractor/strings"); // NOI18N.
    private final Preferences prefs = Preferences.userNodeForPackage(getClass());

    @Override
    public void start(final Stage primaryStage) {
        final StackPane root = new StackPane();
        final Optional<URL> fxmlURLOptional = Optional.ofNullable(getClass().getResource("MainUI.fxml"));
        fxmlURLOptional.ifPresent(fxmlURL -> {
            final FXMLLoader fxmlLoader = new FXMLLoader(fxmlURL, I18N);
            try {
                final Node mainUI = fxmlLoader.load();
                root.getChildren().add(mainUI);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        });
        final Scene scene = new Scene(root);
        final Optional<URL> cssURLOptional = Optional.ofNullable(getClass().getResource("NetCDFExtractor.css")); // NOI18N.        
        cssURLOptional.ifPresent(cssURL -> scene.getStylesheets().add(cssURL.toExternalForm()));
        primaryStage.setTitle(I18N.getString("app.title")); // NOI18N.               
        primaryStage.setScene(scene);
        final double minWidth = 400;
        final double minHeight = 400;
        primaryStage.setMinWidth(minWidth);
        primaryStage.setMinHeight(minHeight);
        final double stageWidth = prefs.getDouble("stage.width", 800);
        final double stageHeight = prefs.getDouble("stage.height", 600);
        final double stageX = prefs.getDouble("stage.x", (Screen.getPrimary().getBounds().getWidth() - stageWidth) / 2d);
        final double stageY = prefs.getDouble("stage.y", (Screen.getPrimary().getBounds().getHeight() - stageHeight) / 2d);
        primaryStage.setX(stageX);
        primaryStage.setY(stageY);
        primaryStage.setWidth(stageWidth);
        primaryStage.setHeight(stageHeight);
        final List<Screen> screenList = Screen.getScreensForRectangle(stageX, stageY, stageWidth, stageHeight);
        if (screenList.isEmpty()) {
            primaryStage.centerOnScreen();
        }
        primaryStage.show();
        primaryStage.xProperty().addListener(observable -> prefs.putDouble("stage.x", primaryStage.getX()));
        primaryStage.yProperty().addListener(observable -> prefs.putDouble("stage.y", primaryStage.getY()));
        primaryStage.widthProperty().addListener(observable -> prefs.putDouble("stage.width", primaryStage.getWidth()));
        primaryStage.heightProperty().addListener(observable -> prefs.putDouble("stage.height", primaryStage.getHeight()));
//        ScenicView.show(scene);
    }

    /**
     * Program entry point.
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
