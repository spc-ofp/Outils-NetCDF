/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.extract;

import java.time.temporal.ChronoUnit;
import javafx.scene.control.ListCell;
import org.spc.ofp.project.netcdfextractor.Main;

/**
 * List cell for chrono units.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
final class ChronoUnitListCell extends ListCell<ChronoUnit> {

    @Override
    protected void updateItem(final ChronoUnit item, final boolean empty) {
        super.updateItem(item, empty);
        String text = null;
        if (!empty && item != null) {
            final String key = String.format("extract.time.unit-%s.label", item.name().toLowerCase()); // NOI18N.
            text = Main.I18N.getString(key);
        }
        setText(text);
    }
}
