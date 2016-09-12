/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.extract;

import java.time.format.DateTimeFormatter;
import javafx.scene.control.ListCell;
import org.spc.ofp.project.netcdfextractor.Main;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParameters;

/**
 * List cell for date time formatters.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
final class DateTimeFormatterListCell extends ListCell<DateTimeFormatter> {

    @Override
    protected void updateItem(final DateTimeFormatter item, final boolean empty) {
        super.updateItem(item, empty);
        String text = null;
        if (!empty && item != null) {
            if (item == BatchExtractToTxtParameters.DEFAULT_DATE_TIME_FORMATTER) {
                text = Main.I18N.getString("extract.time.output-format.default.label"); // NOI18N.
            } else {
                final int index = getIndex();
                final String key = String.format("extract.time.output-format.custom%d.label", index); // NOI18N.
                text = Main.I18N.getString(key);
            }
        }
        setText(text);
    }
}
