/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
                final MainUIController controller = fxmlLoader.getController();
                controller.setApplication(this);
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
        final double stageWidth = prefs.getDouble("stage.width", 800); // NOI18N.     
        final double stageHeight = prefs.getDouble("stage.height", 600); // NOI18N.     
        final double stageX = prefs.getDouble("stage.x", (Screen.getPrimary().getBounds().getWidth() - stageWidth) / 2d); // NOI18N.     
        final double stageY = prefs.getDouble("stage.y", (Screen.getPrimary().getBounds().getHeight() - stageHeight) / 2d); // NOI18N.     
        primaryStage.setX(stageX);
        primaryStage.setY(stageY);
        primaryStage.setWidth(stageWidth);
        primaryStage.setHeight(stageHeight);
        final Image[] icons = IntStream.of(16, 24, 32, 48, 64, 128, 256, 512)
                .mapToObj(index -> String.format("NetCDFExtractor-%d.png", index)) // NOI18N.     
                .map(Main.class::getResource)
                .filter(url -> url != null)
                .map(URL::toExternalForm)
                .map(Image::new)
                .toArray(Image[]::new);
        primaryStage.getIcons().setAll(icons);
        final List<Screen> screenList = Screen.getScreensForRectangle(stageX, stageY, stageWidth, stageHeight);
        if (screenList.isEmpty()) {
            primaryStage.centerOnScreen();
        }
        primaryStage.show();
        primaryStage.xProperty().addListener(observable -> prefs.putDouble("stage.x", primaryStage.getX())); // NOI18N.    
        primaryStage.yProperty().addListener(observable -> prefs.putDouble("stage.y", primaryStage.getY())); // NOI18N.    
        primaryStage.widthProperty().addListener(observable -> prefs.putDouble("stage.width", primaryStage.getWidth())); // NOI18N.    
        primaryStage.heightProperty().addListener(observable -> prefs.putDouble("stage.height", primaryStage.getHeight())); // NOI18N.    
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
