/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.spc.ofp.project.netcdfextractor.data.FileInfo;
import org.spc.ofp.project.netcdfextractor.data.VariableInfo;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Pre-load NetCDF files for the navigator UI.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class NavigationTreeConstructionTask extends Task<TreeItem> {

    /**
     * The target directory.
     */
    private final Path sourceDir;

    /**
     * Creates a new instance.
     * @param sourceDir The source directory.
     * @throws NullPointerException If {@code sourceDir} is {@code null}.
     * @throws IllegalArgumentException If {@code sourceDir} does not exist or is not a directory.
     */
    public NavigationTreeConstructionTask(final Path sourceDir) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(sourceDir);
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException();
        }
        this.sourceDir = sourceDir;
    }

    /**
     * Filter file.
     * @param path The file to filter.
     * @return {@code True} if file is kept, {@code false} otherwise.
     */
    private boolean filterFile(final Path path) {
        final String pathName = path.getFileName().toString();
        return (pathName.endsWith(".nc") || pathName.endsWith(".cdf")); // NOI18N.        
    }

    /**
     * Filter variable.
     * @param variable The source variable.
     * @return {@code True} if variable is kept, {@code false} otherwise.
     */
    private boolean filterVariable(final Variable variable) {
        return variable.getRank() > 1;
    }

    private long progress = 0;
    private long totalProgress = 100;

    @Override
    protected TreeItem call() throws Exception {
        // Task initialization.
        updateProgress(progress, totalProgress);
        if (isCancelled()) {
            return null;
        }
        // List all NetCDF file in source folder.
        final Path[] files = Files.list(sourceDir)
                .filter(this::filterFile)
                .toArray(Path[]::new);
        totalProgress = 2 + 2 * files.length;
        progress++;
        updateProgress(progress, totalProgress);
        if (isCancelled()) {
            return null;
        }
        // Create tree.
        final List<TreeItem> fileItemList = new ArrayList<>(files.length);
        for (final Path file : files) {
            try {
                final TreeItem fileItem = fileToTreeItem(file);
                fileItemList.add(fileItem);
            } catch (IOException ex) {
                System.err.printf("%s ERROR: %s%n", file, ex.getClass().getName());
                exceptions.add(ex);
            }
            if (isCancelled()) {
                return null;
            }
        }
        // Create tree root;
        final TreeItem result = new TreeItem(null);
        result.getChildren().setAll(fileItemList);
        result.setExpanded(true);
        progress++;
        updateProgress(progress, totalProgress);
        if (isCancelled()) {
            return null;
        }
        //
        return result;
    }

    /**
     * Adapts a netcdf file into a tree item.
     * @param file The source file.
     * @return A {@code TreeItem} instance, never {@code null} (unless the task has been cancelled).
     * @throws IOException In case of IO error. 
     */
    private TreeItem fileToTreeItem(final Path file) throws IOException {
        try (final NetcdfFile netcdf = NetcdfFile.open(file.toAbsolutePath().toString())) {
            // List all variables.
            final Variable[] variables = netcdf.getVariables()
                    .stream()
                    .filter(this::filterVariable)
                    .toArray(Variable[]::new);
            totalProgress += variables.length;
            progress++;
            updateProgress(progress, totalProgress);
            if (isCancelled()) {
                return null;
            }
            // Create items for each variable
            final List<TreeItem> variableItemList = new ArrayList<>(variables.length);
            for (final Variable variable : variables) {
                final TreeItem variableItem = variableToTreeItem(variable);
                if (isCancelled()) {
                    return null;
                }
                variableItemList.add(variableItem);
            }
            // Create file info.
            final String[] variableNames = Arrays.stream(variables)
                    .map(variable -> variable.getShortName())
                    .toArray(String[]::new);
            final FileInfo fileInfo = new FileInfo(file, variableNames);
            final TreeItem result = new TreeItem(fileInfo);
            result.getChildren().setAll(variableItemList);
            result.setExpanded(true);
            progress++;
            updateProgress(progress, totalProgress);
            if (isCancelled()) {
                return null;
            }
            return result;
        }
    }

    /**
     * Adapts a variable into a tree item.
     * @param variable The source variable.
     * @return A {@code TreeItem} instance, never {@code null} (unless the task has been cancelled).
     * @throws IOException In case of IO error. 
     */
    private TreeItem variableToTreeItem(final Variable variable) throws IOException {
        final VariableInfo variableInfo = new VariableInfo(variable.getShortName(), variable.getFullName(), variable.getDescription());
        final TreeItem result = new TreeItem(variableInfo);
        progress++;
        updateProgress(progress, totalProgress);
        if (isCancelled()) {
            return null;
        }
        return result;
    }

    /**
     * Stores non-blocking exceptions during tree construction.
     */
    private final List<Exception> exceptions = new LinkedList();
    private final List<Exception> exceptionsUnmodifiable = Collections.unmodifiableList(exceptions);

    /**
     * Gets the list of ignored exceptions that have been raised by this task.
     * @return A non-modifiable {@code List<Exception>} instance, never {@code null}.
     */
    public List<Exception> getExceptions() {
        return exceptionsUnmodifiable;
    }
}
