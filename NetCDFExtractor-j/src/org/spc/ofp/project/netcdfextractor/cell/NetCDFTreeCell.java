/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.cell;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeCell;
import org.spc.ofp.project.netcdfextractor.data.FileInfo;
import org.spc.ofp.project.netcdfextractor.data.VariableInfo;

/**
 * Treel cell for the NetCDF tree.
 * <br>As of 2016/08/31, the tree should only contain instances of:
 * <ul>
 * <li>{@code org.spc.ofp.project.netcdfextractor.data.FileInfo}</li>
 * <li>{@code org.spc.ofp.project.netcdfextractor.data.VariableInfo}</li>
 * </ul>
 * @author Fabrice Bouyé (fabriceb@spc.int)
 * @see org.spc.ofp.project.netcdfextractor.data.FileInfo
 * @see org.spc.ofp.project.netcdfextractor.data.VariableInfo
 */
public final class NetCDFTreeCell extends TreeCell {

    /**
     * Delegated checkbox.
     */
    private final CheckBox checkBox = new CheckBox();

    /**
     * Creates a new instance.
     */
    public NetCDFTreeCell() {
        super();
        setId("netcdfCell"); // NOI18N.        
        getStyleClass().add("netcdf-cell"); // NOI18N.        
    }

    /**
     * Current info.
     */
    private FileInfo fileInfo;

    @Override
    protected void updateItem(final Object item, final boolean empty) {
        super.updateItem(item, empty);
        if (fileInfo != null && fileInfo != item) {
            checkBox.selectedProperty().unbindBidirectional(fileInfo.selectedProperty());
            fileInfo = null;
        }
        String text = null;
        Node graphic = null;
        if (!empty && item != null) {
            if (item instanceof FileInfo) {
                final FileInfo info = (FileInfo) item;
                text = info.toString();
                graphic = checkBox;
                if (fileInfo != info) {
                    fileInfo = info;
                    checkBox.selectedProperty().bindBidirectional(info.selectedProperty());
                }
            } else if (item instanceof VariableInfo) {
                final VariableInfo info = (VariableInfo) item;
                text = info.toString();
            }
        }
        setText(text);
        setGraphic(graphic);
    }
}
