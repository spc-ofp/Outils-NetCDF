/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Defines an application child.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public enum NetCDFExtractorConstants {
    INSTANCE;

    private Properties properties = new Properties();

    private NetCDFExtractorConstants() {
        final URL url = getClass().getResource("version.properties"); // NOI18N.
        if (url != null) {
            try (final InputStream input = url.openStream()) {
                properties.load(input);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    public String getMajorVersion() {
        return properties.getProperty("version.major"); // NOI18N.
    }

    public String getMinorVersion() {
        return properties.getProperty("version.minor"); // NOI18N.
    }

    public String getReleaseVersion() {
        return properties.getProperty("version.release"); // NOI18N.
    }

    public String getBuildNumber() {
        return properties.getProperty("build.number"); // NOI18N.
    }

    public String getVersion() {
        return String.format("%s.%s.%s.%s", // NOI18N.
                getMajorVersion(),
                getMinorVersion(),
                getReleaseVersion(),
                getBuildNumber());
    }
}
