/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.scene.control.extract;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import org.spc.ofp.project.netcdfextractor.Main;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParameters;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParametersBuilder;
import org.spc.ofp.project.netcdfextractor.task.NetCDFUtils;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateUnit;
import ucar.nc2.time.CalendarPeriod;
import ucar.nc2.time.CalendarPeriod.Field;

/**
 * FXML Controller class
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class ExtractConfigPaneController extends ControllerBase<ExtractConfigPane> {

    private static String CUSTOM_SEPARATOR = "custom"; // NOI18N.
    /**
     * Sets of default separators.
     */
    private static final Set<String> DEFAULT_SEPARATORS = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            // Note: using a null value causes an IndexOutOfBoundsException.
            CUSTOM_SEPARATOR, " ", "\t", ",", ";", ":", "|", "/", "\\", "?", "!"))); // NOI18N.

    @FXML
    private Node rootPane;
    @FXML
    private TextField dirField;
    @FXML
    private Tooltip dirFieldTip;
    @FXML
    private ComboBox<String> separatorCombo;
    @FXML
    private TextField separatorField;
    @FXML
    private CheckBox singleOutputCheck;
    @FXML
    private CheckBox includeColumnHeaderCheck;
    @FXML
    private Text timeDescriptionText;
    @FXML
    private Spinner<Integer> timeUnitSpinner;
    @FXML
    private ComboBox<ChronoUnit> timeUnitCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Spinner<Integer> timeHourSpinner;
    @FXML
    private Spinner<Integer> timeMinuteSpinner;
    @FXML
    private Spinner<Integer> timeSecondSpinner;
    @FXML
    private Spinner<Integer> timeOffsetSpinner;

    /**
     * Creates a new instance.
     */
    public ExtractConfigPaneController() {
    }

    @Override
    public void dispose() {
        try {
            if (dirFieldTip != null) {
                dirFieldTip.textProperty().unbind();
                dirFieldTip = null;
            }
            if (separatorField != null) {
                separatorField.textProperty().removeListener(separatorChangeListener);
                separatorField.editableProperty().unbind();
            }
            if (separatorCombo != null) {
                separatorCombo.valueProperty().removeListener(separatorChangeListener);
            }
            if (singleOutputCheck != null) {
                singleOutputCheck.selectedProperty().removeListener(singleOuputChangeListener);
                singleOutputCheck = null;
            }
            if (includeColumnHeaderCheck != null) {
                includeColumnHeaderCheck.selectedProperty().removeListener(includeColumnHeaderChangeListener);
                includeColumnHeaderCheck = null;
            }
            if (timeUnitSpinner != null) {
                timeUnitSpinner.valueProperty().removeListener(timePeriodSizeChangeListener);
                timeUnitSpinner.setValueFactory(null);
                timeUnitSpinner = null;
            }
            if (timeUnitCombo != null) {
                timeUnitCombo.valueProperty().removeListener(timePeriodUnitChangeListener);
                timeUnitCombo.getItems().clear();
                timeUnitCombo.setCellFactory(null);
                timeUnitCombo.setButtonCell(null);
                timeUnitCombo = null;
            }
            if (datePicker != null) {
                datePicker.valueProperty().removeListener(dateChangeListener);
                datePicker = null;
            }
            if (timeHourSpinner != null) {
                timeHourSpinner.valueProperty().removeListener(timeHourChangeListener);
                timeHourSpinner.setValueFactory(null);
                timeHourSpinner = null;
            }
            if (timeMinuteSpinner != null) {
                timeMinuteSpinner.valueProperty().removeListener(timeMinuteChangeListener);
                timeMinuteSpinner.setValueFactory(null);
                timeMinuteSpinner = null;
            }
            if (timeSecondSpinner != null) {
                timeSecondSpinner.valueProperty().removeListener(timeSecondChangeListener);
                timeSecondSpinner.setValueFactory(null);
                timeSecondSpinner = null;
            }
            if (timeOffsetSpinner != null) {
                timeOffsetSpinner.valueProperty().removeListener(timeOffsetChangeListener);
                timeOffsetSpinner.setValueFactory(null);
                timeOffsetSpinner = null;
            }
        } finally {
            super.dispose();
        }
    }

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        dirFieldTip.textProperty().bind(dirField.textProperty());
        //
        separatorField.textProperty().addListener(separatorChangeListener);
        separatorField.editableProperty().bind(new BooleanBinding() {
            {
                bind(separatorCombo.valueProperty());
            }

            @Override
            public void dispose() {
                unbind(separatorCombo.valueProperty());
            }

            @Override
            protected boolean computeValue() {
                final String separator = separatorCombo.getValue();
                return CUSTOM_SEPARATOR.equals(separator);
            }
        });
        //
        separatorCombo.getItems().setAll(DEFAULT_SEPARATORS);
        separatorCombo.getSelectionModel().select(BatchExtractToTxtParameters.DEFAULT_SEPARATOR);
        separatorCombo.valueProperty().addListener(separatorChangeListener);
        //
        singleOutputCheck.selectedProperty().addListener(singleOuputChangeListener);
        //
        includeColumnHeaderCheck.selectedProperty().addListener(includeColumnHeaderChangeListener);
        //
        timeDescriptionText.setText(null);
        //
        timeUnitSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE));
        timeUnitSpinner.valueProperty().addListener(timePeriodSizeChangeListener);
        //
        final ChronoUnit[] chronoUnits = Arrays.stream(Field.values())
                .map(NetCDFUtils.INSTANCE::fieldToJava)
                .toArray(ChronoUnit[]::new);
        timeUnitCombo.setButtonCell(new ChronoUnitListCell());
        timeUnitCombo.setCellFactory(listView -> new ChronoUnitListCell());
        timeUnitCombo.getItems().setAll(chronoUnits);
        timeUnitCombo.valueProperty().addListener(timePeriodUnitChangeListener);
        //
        datePicker.valueProperty().addListener(dateChangeListener);
        //
        timeHourSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 24));
        timeHourSpinner.valueProperty().addListener(timeHourChangeListener);
        //
        timeMinuteSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
        timeMinuteSpinner.valueProperty().addListener(timeMinuteChangeListener);
        //
        timeSecondSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59));
        timeSecondSpinner.valueProperty().addListener(timeSecondChangeListener);
        //
        final int minOffset = ZoneOffset.MIN.getTotalSeconds() / 3600;
        final int maxOffset = ZoneOffset.MAX.getTotalSeconds() / 3600;
        timeOffsetSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(minOffset, maxOffset));
        final TextFormatter<Integer> utcFormatter = new TextFormatter<>(new StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                if (object == null) {
                    return "";
                } else if (object >= 0) {
                    return "UTC+" + object;
                } else {
                    return "UTC" + object;
                }
            }

            @Override
            public Integer fromString(String string) {
                int result = 0;
                try {
                    string = string.replaceAll("UTC", "");
                    result = Integer.parseInt(string);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
                return result;
            }
        });
        timeOffsetSpinner.getEditor().setTextFormatter(utcFormatter);
        timeOffsetSpinner.valueProperty().addListener(timeOffsetChangeListener);
    }

    @Override
    protected void uninstallNode(final ExtractConfigPane node) {
    }

    @Override
    protected void installNode(final ExtractConfigPane node) {
    }

    @Override
    protected void updateUI() {
        applyDefaultDateConfig();
    }

    private boolean timeEditing = false;

    private void applyDefaultDateConfig() {
        parentNode().ifPresent(parent -> {
            // Move in an external task?
            final BatchExtractToTxtParameters parameters = parent.createParameters();
            final Iterator<Path> fileIterator = parameters.getFiles().iterator();
            if (!fileIterator.hasNext()) {
                return;
            }
            final Path source = fileIterator.next();
            timeEditing = true;            
            try (final NetcdfFile netcdf = NetcdfFile.open(source.toString())) {
                final Variable timeVariable = netcdf.findVariable("time"); // NOI18N.
                final String timeUnitString = timeVariable.getUnitsString();
                final CalendarDateUnit calendarDateUnit = CalendarDateUnit.of(ucar.nc2.time.Calendar.proleptic_gregorian.name(), timeUnitString);
                final CalendarPeriod calendarPeriod = calendarDateUnit.getTimeUnit();
                timeDescriptionText.setText(timeVariable.getUnitsString());
                final int periodLength = calendarPeriod.getValue();
                timeUnitSpinner.getValueFactory().setValue(periodLength);
                final ChronoUnit chronoUnit = NetCDFUtils.INSTANCE.fieldToJava(calendarPeriod.getField());
                timeUnitCombo.getSelectionModel().select(chronoUnit);
                final CalendarDate netcdfStartDate = calendarDateUnit.getBaseCalendarDate();
                final ZonedDateTime javaStartDate = ZonedDateTime.parse(netcdfStartDate.toString());
                datePicker.setValue(javaStartDate.toLocalDate());
                timeHourSpinner.getValueFactory().setValue(javaStartDate.getHour());
                timeMinuteSpinner.getValueFactory().setValue(javaStartDate.getMinute());
                timeSecondSpinner.getValueFactory().setValue(javaStartDate.getSecond());
                final ZoneOffset zoneOffset = javaStartDate.getOffset();
                // What about half offset???
                final int offset = zoneOffset.getTotalSeconds() / 3600;
                timeOffsetSpinner.getValueFactory().setValue(offset);
            } catch (Exception ex) {
                Logger.getLogger(ExtractConfigPaneController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            } finally {
                timeEditing = false;
            }
            updateTimePeriodInParameters();
            updateStartDateInParameters();
        });
    }

    @FXML
    private void handleDirButton() {
        browseForDirectory();
    }

    private void browseForDirectory() {
        final String path = dirField.getText();
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            final String homePath = System.getProperty("user.home"); // NOI18N.
            dir = new File(homePath);
        }
        final DirectoryChooser dialog = new DirectoryChooser();
        dialog.setInitialDirectory(dir);
        final Optional<File> directoryOptional = Optional.ofNullable(dialog.showDialog(rootPane.getScene().getWindow()));
        directoryOptional.ifPresent(directory -> {
            dirField.setText(directory.getAbsolutePath());
            final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
            builder.destinationDir(Paths.get(directory.toURI()));
        });
    }

    /**
     * Called whenever the separator selection changes.
     */
    private final ChangeListener<String> separatorChangeListener = (observable, oldValue, newValue) -> {
        final String comboSeparator = separatorCombo.getValue();
        final String fieldSeparator = separatorField.getText();
        final String separator = (CUSTOM_SEPARATOR.equals(comboSeparator)) ? fieldSeparator : comboSeparator;
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        builder.separator(separator);
    };

    /**
     * Called whenever the single output checkbox changes state.
     */
    private final ChangeListener<Boolean> singleOuputChangeListener = (observable, oldValue, newValue) -> {
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        builder.singleDocument(newValue);
    };

    /**
     * Called whenever the include column header checkbox changes state.
     */
    private final ChangeListener<Boolean> includeColumnHeaderChangeListener = (observable, oldValue, newValue) -> {
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        builder.includeColumnHeader(newValue);
    };

    /**
     * Called whenever the size of the time period changes.
     */
    private final ChangeListener<Integer> timePeriodSizeChangeListener = (observable, oldValue, newValue) -> {
        if (timeEditing) {
            return;
        }
        updateTimePeriodInParameters();
    };

    /**
     * Called whenever the unit of the time period changes.
     */
    private final ChangeListener<ChronoUnit> timePeriodUnitChangeListener = (observable, oldValue, newValue) -> {
        if (timeEditing) {
            return;
        }
        updateTimePeriodInParameters();
    };

    /**
     * Called whenever the epoch date of the time period changes.
     */
    private final ChangeListener<LocalDate> dateChangeListener = (observable, oldValue, newValue) -> {
        if (timeEditing) {
            return;
        }
        updateStartDateInParameters();
    };

    /**
     * Called whenever the origin hour of the time period changes.
     */
    private final ChangeListener<Integer> timeHourChangeListener = (observable, oldValue, newValue) -> {
        if (timeEditing) {
            return;
        }
        updateStartDateInParameters();
    };

    /**
     * Called whenever the origin minute of the hour of the time period changes.
     */
    private final ChangeListener<Integer> timeMinuteChangeListener = (observable, oldValue, newValue) -> {
        if (timeEditing) {
            return;
        }
        updateStartDateInParameters();
    };

    /**
     * Called whenever the origin second of the minute of the time period changes.
     */
    private final ChangeListener<Integer> timeSecondChangeListener = (observable, oldValue, newValue) -> {
        if (timeEditing) {
            return;
        }
        updateStartDateInParameters();
    };

    /**
     * Called whenever the origin offset of the time period changes.
     */
    private final ChangeListener<Integer> timeOffsetChangeListener = (observable, oldValue, newValue) -> {
        if (timeEditing) {
            return;
        }
        updateStartDateInParameters();
    };

    /**
     * Update the time period values in the parameter.
     */
    private void updateTimePeriodInParameters() {
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        final int timePeriodSize = timeUnitSpinner.getValue();
        final ChronoUnit timePeriodUnit = timeUnitCombo.getValue();
        builder.periodSize(timePeriodSize)
                .periodUnit(timePeriodUnit);
    }

    /**
     * Update the start date value in the parameter.
     */
    private void updateStartDateInParameters() {
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        final LocalDate localDate = datePicker.getValue();
        final int hour = timeHourSpinner.getValue();
        final int minute = timeMinuteSpinner.getValue();
        final int second = timeSecondSpinner.getValue();
        final ZoneOffset offset = ZoneOffset.ofHours(timeOffsetSpinner.getValue());
        final ZonedDateTime startDate = localDate.atTime(hour, minute, second)
                .atZone(offset);
        builder.startDate(startDate);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * List cell for chrono units.
     * @author Fabrice Bouyé (fabriceb@spc.int)
     */
    private static class ChronoUnitListCell extends ListCell<ChronoUnit> {

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
}
