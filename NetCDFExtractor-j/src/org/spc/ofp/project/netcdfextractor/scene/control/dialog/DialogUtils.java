/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.dialog;

import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Dialog utility class.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public enum DialogUtils {
    /**
     * The unique instance of this class.
     */
    INSTANCE;

    public Dialog create(final Window parent, final String header, final Node content, final ButtonType... buttons) {
        return create(parent, Modality.APPLICATION_MODAL, StageStyle.UNDECORATED, null, header, content, buttons);
    }

    public Dialog create(final Window parent, final Modality modality, final StageStyle style, final String title, final String header, final Node content, final ButtonType... buttons) {
        final Dialog dialog = new Dialog();
        dialog.initOwner(parent);
        dialog.initModality(modality);
        dialog.initStyle(style);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        final DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().setAll(buttons);
        return dialog;
    }
}
