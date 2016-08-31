/***********************************************************************
 *  Copyright - Secretariat of the Pacific Community                   *
 *  Droit de copie - Secrétariat Général de la Communauté du Pacifique *
 *  http://www.spc.int/                                                *
 ***********************************************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import javafx.concurrent.Task;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Test task: walk folder and detail all NetCDF content.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class FolderWalkTask extends Task<Void> {

    /**
     * The target directory.
     */
    private final File sourceDir;

    /**
     * Creates a new instance.
     * @param sourceDir The sourceDir directory.
     * @throws NullPointerException If {@code sourceDir} is {@code null}.
     * @throws IllegalArgumentException If {@code sourceDir} does not exist or is not a directory.
     */
    public FolderWalkTask(final File sourceDir) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(sourceDir);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException();
        }
        this.sourceDir = sourceDir;
    }
    
    @Override
    protected Void call() throws Exception {
        walkFolder(sourceDir);
        return null;
    }
    
    private void walkFolder(final File directory) throws IOException {
        final String directoryPath = directory.getAbsolutePath();
        System.out.printf("> Directory %s%n", directoryPath);
        final File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        for (final File file : files) {
            if (file.isDirectory()) {
                walkFolder(file);
            } else if (file.isFile()) {
                final String filePath = file.getAbsolutePath();
                if (filePath.endsWith(".nc") || filePath.endsWith(".cdf")) { // NOI18N.
                    System.out.printf(">> File %s%n", filePath);
                    try (final NetcdfFile netcdf = NetcdfFile.open(filePath)) {
                        describeNetCDF(netcdf);
                    }
                }
            }
        }
        System.out.println();
    }
    
    private void describeNetCDF(final NetcdfFile netcdf) {
        final List<Variable> variables = netcdf.getVariables();
        variables.stream()
                .forEach(variable -> describeVariable(variable));
        System.out.println();
    }
    
    private void describeVariable(final Variable variable) {
        System.out.println(variable.getNameAndDimensions());
        System.out.printf(">>> %s%n", variable.getDescription());
    }
    
}
