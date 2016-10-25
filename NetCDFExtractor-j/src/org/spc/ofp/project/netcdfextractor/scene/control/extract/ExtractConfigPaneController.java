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
import java.time.format.DateTimeFormatter;
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
import java.util.prefs.Preferences;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import org.spc.ofp.project.netcdfextractor.scene.ControllerBase;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParameters;
import org.spc.ofp.project.netcdfextractor.task.BatchExtractToTxtParametersBuilder;
import org.spc.ofp.project.netcdfextractor.task.NetCDFUtils;
import ucar.nc2.Dimension;
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
    private TextField missingValueField;
    @FXML
    private CheckBox singleOutputCheck;
    @FXML
    private CheckBox includeColumnHeaderCheck;
    @FXML
    private Text timeDescriptionText;
    @FXML
    private ComboBox<String> timeVariableCombo;
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
    @FXML
    private ComboBox<DateTimeFormatter> timeOutputCombo;

    /**
     * Creates a new instance.
     */
    public ExtractConfigPaneController() {
    }

    @Override
    public void dispose() {
        try {
            if (dirField != null) {
                dirField.textProperty().removeListener(dirChangeListener);
                dirField = null;
            }
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
            if (missingValueField != null) {
                missingValueField.textProperty().removeListener(missingValueChangeListener);
                missingValueField = null;
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
            if (timeOutputCombo != null) {
                timeOutputCombo.valueProperty().removeListener(timeOutputFormatChangeListener);
                timeOutputCombo.getItems().clear();
                timeOutputCombo.setButtonCell(null);
                timeOutputCombo.setCellFactory(null);
                timeOutputCombo = null;
            }
        } finally {
            super.dispose();
        }
    }

    private final Preferences prefs = Preferences.userNodeForPackage(getClass());

    @Override
    public void initialize(final URL url, final ResourceBundle bundle) {
        dirField.textProperty().addListener(dirChangeListener);
        //
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
        final String missingValue = prefs.get("missing.value", BatchExtractToTxtParameters.DEFAULT_MISSING_VALUE); // NOI18N.
        missingValueField.setText(missingValue);
        missingValueField.textProperty().addListener(missingValueChangeListener);
        //
        final String separator = prefs.get("separator", BatchExtractToTxtParameters.DEFAULT_SEPARATOR); // NOI18N.
        separatorCombo.getItems().setAll(DEFAULT_SEPARATORS);
        if (DEFAULT_SEPARATORS.contains(separator)) {
            separatorCombo.getSelectionModel().select(separator);
        } else {
            separatorCombo.getSelectionModel().select(CUSTOM_SEPARATOR);
            separatorField.setText(separator);
        }
        separatorCombo.valueProperty().addListener(separatorChangeListener);
        //
        final boolean singleDocument = prefs.getBoolean("single.document", BatchExtractToTxtParameters.DEFAULT_SINGLE_DOCUMENT); // NOI18N.
        singleOutputCheck.setSelected(singleDocument);
        singleOutputCheck.selectedProperty().addListener(singleOuputChangeListener);
        //
        final boolean includeColumnHeader = prefs.getBoolean("include.column.header", BatchExtractToTxtParameters.DEFAULT_INCLUDE_COLUMN_HEADER); // NOI18N.
        includeColumnHeaderCheck.setSelected(includeColumnHeader);
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
        //
        final int timeFormatterIndex = prefs.getInt("time.formatter.index", 0); // NOI18N.
        final String customPattern = bundle.getString("extract.time.output-format.custom1.label"); // NOI18N.
        timeOutputCombo.setButtonCell(new DateTimeFormatterListCell());
        timeOutputCombo.setCellFactory(listView -> new DateTimeFormatterListCell());
        timeOutputCombo.getItems().setAll(BatchExtractToTxtParameters.DEFAULT_DATE_TIME_FORMATTER,
                DateTimeFormatter.ofPattern(customPattern)); // NOI18N.
        timeOutputCombo.getSelectionModel().select(timeFormatterIndex);
        timeOutputCombo.valueProperty().addListener(timeOutputFormatChangeListener);
    }

    @Override
    protected void uninstallNode(final ExtractConfigPane node) {
    }

    @Override
    protected void installNode(final ExtractConfigPane node) {
    }

    @Override
    protected void updateUI() {
        applyDefaultFolderConfig();
        applyDefaultDateConfig();
    }

    private boolean baseEditing = false;

    private void applyDefaultFolderConfig() {
        parentNode().ifPresent(parent -> {
            final BatchExtractToTxtParameters parameters = parent.createParameters();
            final Iterator<Path> fileIterator = parameters.getFiles().iterator();
            if (!fileIterator.hasNext()) {
                return;
            }
            baseEditing = true;
            try {
                final Path source = fileIterator.next();
                dirField.setText(source.getParent().toString());
            } finally {
                baseEditing = false;
            }
            updateBaseParameters();
        });
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
                // Select time variable.
                final String timeVariableName = parameters.getTimeVariable();
                Variable timeVariable = netcdf.findVariable((timeVariableName == null) ? BatchExtractToTxtParameters.DEFAULT_TIME_VARIABLE : timeVariableName);
                if (timeVariable != null) {
                    if (timeVariableCombo.getItems().isEmpty()) {
                        timeVariableCombo.valueProperty().removeListener(timeVariableChangeListener);
                        timeVariableCombo.getItems().setAll(timeVariableName);
                        timeVariableCombo.getSelectionModel().select(0);
                        timeVariableCombo.valueProperty().addListener(timeVariableChangeListener);
                    }
                } else {
                    final String[] variables = netcdf.getDimensions()
                            .stream()
                            .map(Dimension::getShortName)
                            .map(netcdf::findVariable)
                            .filter(variable -> {
                                final String standardName = variable.findAttribute("standard_name").getStringValue();  // NOI18N.
                                return BatchExtractToTxtParameters.DEFAULT_TIME_VARIABLE.equals(standardName);
                            })
                            .map(Variable::getShortName)
                            .toArray(String[]::new);
                    timeVariableCombo.valueProperty().removeListener(timeVariableChangeListener);
                    timeVariableCombo.getItems().setAll(variables);
                    timeVariableCombo.getSelectionModel().select(0);
                    timeVariableCombo.valueProperty().addListener(timeVariableChangeListener);
                    timeVariable = netcdf.findVariable(timeVariableCombo.getValue());
                }
                // Apply date config.
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
            updateTimeFormatterInParameters();
        });
    }

    /**
     * Called whenever the time variable changes.
     */
    private ChangeListener<String> timeVariableChangeListener = (observable, oldValue, newValue) -> {
        parentNode().ifPresent(parent -> {
            parent.getParametersBuilder().timeVariable(newValue);
            applyDefaultDateConfig();
        });
    };

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
        directoryOptional.ifPresent(directory -> dirField.setText(directory.getAbsolutePath()));
    }

    /**
     * Called whenever the directory field text is changed.
     */
    private final ChangeListener<String> dirChangeListener = (observable, oldValue, newValue) -> {
        if (baseEditing) {
            return;
        }
        updateBaseParameters();
    };

    /**
     * Called whenever the separator selection changes.
     */
    private final ChangeListener<String> separatorChangeListener = (observable, oldValue, newValue) -> {
        if (baseEditing) {
            return;
        }
        updateBaseParameters();
    };

    /**
     * Called whenever the missing value changes.
     */
    private final ChangeListener<String> missingValueChangeListener = (observable, oldValue, newValue) -> {
        if (baseEditing) {
            return;
        }
        updateBaseParameters();
    };

    /**
     * Called whenever the single output checkbox changes state.
     */
    private final ChangeListener<Boolean> singleOuputChangeListener = (observable, oldValue, newValue) -> {
        if (baseEditing) {
            return;
        }
        updateBaseParameters();
    };

    /**
     * Called whenever the include column header checkbox changes state.
     */
    private final ChangeListener<Boolean> includeColumnHeaderChangeListener = (observable, oldValue, newValue) -> {
        if (baseEditing) {
            return;
        }
        updateBaseParameters();
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
     * Called whenever the date time formatter changes.
     */
    private final ChangeListener<DateTimeFormatter> timeOutputFormatChangeListener = (observable, oldValue, newValue) -> {
        if (timeEditing) {
            return;
        }
        updateTimeFormatterInParameters();
    };

    /**
     * Update base parameters.
     */
    private void updateBaseParameters() {
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        final Path dir = Paths.get(dirField.getText());
        final boolean singleDocument = singleOutputCheck.isSelected();
        final boolean includeColumnHeader = includeColumnHeaderCheck.isSelected();
        final String comboSeparator = separatorCombo.getValue();
        final String fieldSeparator = separatorField.getText();
        final String separator = (CUSTOM_SEPARATOR.equals(comboSeparator)) ? fieldSeparator : comboSeparator;
        final String missingValue = missingValueField.getText();
        builder.destinationDir(dir)
                .singleDocument(singleDocument)
                .includeColumnHeader(includeColumnHeader)
                .separator(separator)
                .missingValue(missingValue);
        prefs.putBoolean("single.document", singleDocument); // NOI18N.
        prefs.putBoolean("include.column.header", includeColumnHeader); // NOI18N.
        prefs.put("separator", separator); // NOI18N.
        prefs.put("missing.value", missingValue); // NOI18N.
    }

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

    private void updateTimeFormatterInParameters() {
        final BatchExtractToTxtParametersBuilder builder = parentNode().get().getParametersBuilder();
        final DateTimeFormatter dateTimeFormatter = timeOutputCombo.getValue();
        builder.dateTimeFormatter(dateTimeFormatter);
        final int timeFormatterIndex = timeOutputCombo.getItems().indexOf(dateTimeFormatter);
        prefs.putInt("time.formatter.index", timeFormatterIndex); // NOI18N.
    }

}
