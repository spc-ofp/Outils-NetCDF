/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 * Pre-load NetCDF files for the navigator UI.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public final class VariableHTMLReportTask extends Task<String> {

    private final Path file;
    private final String variableName;

    public VariableHTMLReportTask(final Path file, final String variableName) {
        Objects.requireNonNull(file);
        Objects.requireNonNull(variableName);
        this.file = file;
        this.variableName = variableName;
    }

    @Override
    protected String call() throws Exception {
        try (final NetcdfFile netcdf = NetcdfFile.open(file.toString());
                final StringWriter writer = new StringWriter();
                final PrintWriter out = new PrintWriter(writer)) {
            final Variable variable = netcdf.findVariable(variableName);
            out.println("<html>");
            out.println("<head>");
            out.println("</head>");
            out.println("<body>");
            out.printf("<h1>%s</h1>%n", variable.getFullName());
            out.printf("<br>Description: %s%n", variable.getDescription());
            out.printf("<br>Type: %s%n", variable.getDataType());
            out.printf("<br>Rank: %s%n", variable.getRank());
            final String dimensions = variable.getDimensionsAll()
                    .stream()
                    .map(Dimension::getFullName)
                    .collect(Collectors.joining(" * "));
            out.printf("<br>Dimensions: %s%n", dimensions);
            final String shape = Arrays.stream(variable.getShapeAll())
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(" * "));
            out.printf("<br>Shape: %s%n", shape);
            out.println("<h2>Attributes</h2>");
            out.println("<table width=\"100%\">");
            out.println("<tr><th>Name</th><th>Type</th><th>Length</th><th>Value(s)</th></tr>");
            variable.getAttributes()
                    .stream()
                    .forEach(attribute -> reportAttribute(out, attribute));
            out.println("</table>");
            out.println("</body>");
            out.println("</html>");
            return writer.toString();
        }
    }

    private void reportAttribute(final PrintWriter out, final Attribute attribute) {
        out.printf("<tr><td>%s</td><td>%s</td><td>%d</td><td>%s</td></tr>%n", attribute.getFullName(), attribute.getDataType(), attribute.getLength(), attribute.getValues());
    }
}
