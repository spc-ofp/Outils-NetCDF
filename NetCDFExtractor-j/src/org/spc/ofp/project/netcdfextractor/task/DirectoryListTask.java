/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javafx.concurrent.Task;

/**
 *
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class DirectoryListTask extends Task<List<Path>> {

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
    public DirectoryListTask(final Path sourceDir) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(sourceDir);
        if (!Files.exists(sourceDir) || !Files.isDirectory(sourceDir)) {
            throw new IllegalArgumentException();
        }
        this.sourceDir = sourceDir;
    }

    @Override
    protected List<Path> call() throws Exception {
        return walkFolder(sourceDir.toFile());
    }

    private List<Path> walkFolder(final File directory) throws IOException {
        final File[] files = directory.listFiles();
        if (files == null) {
            return Collections.EMPTY_LIST;
        }
        final List<Path> result = new LinkedList();
        for (final File file : files) {
            if (file.isDirectory()) {
                result.addAll(walkFolder(file));
            } else if (file.isFile()) {
                final String filePath = file.getAbsolutePath();
                if (filePath.endsWith(".nc") || filePath.endsWith(".cdf")) { // NOI18N.
                    result.add(file.toPath());
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
}
