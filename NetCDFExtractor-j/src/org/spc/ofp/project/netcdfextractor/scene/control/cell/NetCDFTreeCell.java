/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.cell;

import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeCell;
import org.spc.ofp.project.netcdfextractor.data.FileInfo;
import org.spc.ofp.project.netcdfextractor.data.VariableInfo;

/**
 * Tree cell for the NetCDF tree.
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

    private static final PseudoClass FILE_PSEUDO_CLASS = PseudoClass.getPseudoClass("file"); // NOI18N.    
    private static final PseudoClass VARIABLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("variable"); // NOI18N.    

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
                final FileInfo info = (FileInfo) object;
                info.selectedStateProperty().removeListener(fileSelectedStateChangeListener);
                checkBox.selectedProperty().removeListener(fileSelectionChangeListener);
            } else if (object instanceof VariableInfo) {
                final VariableInfo info = (VariableInfo) object;
                checkBox.selectedProperty().unbindBidirectional(info.selectedProperty());
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
                graphic = checkBox;
                if (object != info) {
                    object = info;
                    updateFileCheckBox(info.getSelectedState());
                    info.selectedStateProperty().addListener(fileSelectedStateChangeListener);
                    checkBox.selectedProperty().addListener(fileSelectionChangeListener);
                }
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
     * Select / unselect all variables items underneath a file item.
     * @param selected If {@code true}, the variable is selected.
     */
    private void selectAllVariables(final boolean selected) {
        final Object item = getItem();
        if (item instanceof FileInfo) {
            final FileInfo fileInfo = (FileInfo) item;
            fileInfo.selectAllVariables(selected);
        }
    }

    /**
    * Update a file info check box state.
    * @param state The file info selected state.
    */
    private void updateFileCheckBox(final FileInfo.SelectedSate state) {
        switch (state) {
            case ALL:
                checkBox.setIndeterminate(false);
                checkBox.setSelected(true);
                break;
            case NONE:
                checkBox.setIndeterminate(false);
                checkBox.setSelected(false);
                break;
            case SOME:
                checkBox.setIndeterminate(true);
                break;
        }
    }

    /**
     * Called when check box of a file info is clicked.
     */
    private final ChangeListener<Boolean> fileSelectionChangeListener = (observable, oldValue, newValue) -> {
        selectAllVariables(newValue);
    };

    /**
     * Called when the selected state of a file info changes.
     */
    private final ChangeListener<FileInfo.SelectedSate> fileSelectedStateChangeListener = (observable, oldValue, newValue) -> {
        updateFileCheckBox(newValue);
    };
}
