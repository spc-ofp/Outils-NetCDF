/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.cell;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import org.spc.ofp.project.netcdfextractor.Main;
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

    private static PseudoClass FILE_PSEUDO_CLASS = PseudoClass.getPseudoClass("file"); // NOI18N.    
    private static PseudoClass VARIABLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("variable"); // NOI18N.    

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
    private Object object;

    @Override
    protected void updateItem(final Object item, final boolean empty) {
        super.updateItem(item, empty);
        if (object != null && object != item) {
            if (object instanceof FileInfo) {
                final FileInfo fileInfo = (FileInfo) object;
                checkBox.selectedProperty().unbindBidirectional(fileInfo.selectedProperty());
            } else if (object instanceof VariableInfo) {
                final VariableInfo variableInfo = (VariableInfo) object;
                checkBox.selectedProperty().unbindBidirectional(variableInfo.selectedProperty());
            }
            object = null;
        }
        String text = null;
        Node graphic = null;
        pseudoClassStateChanged(FILE_PSEUDO_CLASS, false);
        pseudoClassStateChanged(VARIABLE_PSEUDO_CLASS, false);
        if (!empty && item != null) {
            if (item instanceof FileInfo) {
                pseudoClassStateChanged(FILE_PSEUDO_CLASS, true);
                final FileInfo info = (FileInfo) item;
                text = info.toString();
//                graphic = checkBox;
//                if (object != info) {
//                    object = info;
//                    checkBox.selectedProperty().bindBidirectional(info.selectedProperty());
//                }
                final Tooltip buttonTip = new Tooltip();
                buttonTip.setText(Main.I18N.getString("tree.actions.select-all-variables")); // NOI18N.
                final Button button = new Button();
                button.setText(Main.I18N.getString("icon.fa-check-square-o")); // NOI18N.
                button.setOnAction(actionEvent -> selectAllVariables());
                button.setTooltip(buttonTip);
                graphic = button;
            } else if (item instanceof VariableInfo) {
                pseudoClassStateChanged(VARIABLE_PSEUDO_CLASS, true);
                final VariableInfo info = (VariableInfo) item;
                text = info.toString();
                graphic = checkBox;
                if (object != info) {
                    object = info;
                    checkBox.selectedProperty().bindBidirectional(info.selectedProperty());
                }
            }
        }
        setText(text);
        setGraphic(graphic);
    }

    /**
     * Select all variables items underneath a file item.
     */
    private void selectAllVariables() {
        final TreeItem<Object> fileItem = getTreeItem();
        if (fileItem == null || !(fileItem.getValue() instanceof FileInfo)) {
            return;
        }
        fileItem.getChildren()
                .stream()
                .map(variableItem -> (VariableInfo) variableItem.getValue())
                .forEach(variableInfo -> variableInfo.setSelected(true));
    }
}
