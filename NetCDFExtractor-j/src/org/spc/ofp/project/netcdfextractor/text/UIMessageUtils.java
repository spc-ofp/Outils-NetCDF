/*********************************************
 *  Copyright - Pacific Community            *
 *  Droit de copie - Communauté du Pacifique *
 *  http://www.spc.int/                      *
 *********************************************/
package org.spc.ofp.project.netcdfextractor.text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utils class for dealing with long messages.
 * @author Fabrice Bouyé (fabriceb@spc.int)
 */
public enum UIMessageUtils {
    INSTANCE;
    private static final String CONTENT = "content"; // NOI18N.
    private static final String LINE_BREAK = "br"; // NOI18N.
    private static final String PARAGRAPH = "p"; // NOI18N.
    private static final String BOLD = "b"; // NOI18N.
    private static final String INFO = "info"; // NOI18N.
    private static final String H1 = "h1"; // NOI18N.
    private static final String H2 = "h2"; // NOI18N.
    private static final String H3 = "h3"; // NOI18N.
    private static final String H4 = "h4"; // NOI18N.
    private static final String LIST = "ul"; // NOI18N.
    private static final String LIST_ITEM = "li"; // NOI18N.
    private static final String FONT_AWESOME = "font-awesome"; // NOI18N.
    private static final String LINK = "a"; // NOI18N.

    private final PseudoClass BOLD_PSEUDO_CLASS = PseudoClass.getPseudoClass("strong"); // NOI18N.
    private final PseudoClass FONT_AWESOME_PSEUDO_CLASS = PseudoClass.getPseudoClass(FONT_AWESOME);

    /**
     * Split rich text content into nodes to display on screen.
     * @param string The source rich text, may be {@code null}.
     * @return A non-modifiable {@code List<Node>}, never {@code null}.
     */
    public List<Node> split(final String string) {
        return split(string, null);
    }

    /**
     * Split rich text content into nodes to display on screen.
     * <br>given text is imported as XML and given to a SAX parser.
     * @param string The source rich text, may be {@code null}.
     * @param linkActivator A {@code Consumer<String>} called when clicking on a {@code Hyperlink}, may be {@code null}.
     * @return A non-modifiable {@code List<Node>}, never {@code null}.
     */
    public List<Node> split(final String string, final Consumer<String> linkActivator) {
        List<Node> result = Collections.emptyList();
        if (string != null) {
            final List<Node> nodeList = new LinkedList();
            try {
                final SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setValidating(false);
                final SAXParser saxParser = spf.newSAXParser();
                String escapedContent = string.replaceAll("&", "&amp;"); // NOI18N.
                final String xmlString = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?><%s>%s</%s>", CONTENT, escapedContent, CONTENT); // NOI18N.
                final Map<String, Boolean> styleAttributes = initializeAttributeMap();
                try (final InputStream source = new ByteArrayInputStream(xmlString.getBytes("UTF-8"))) { // NOI18N.
                    saxParser.parse(source, new DefaultHandler() {
                        private Attributes attributes;

                        @Override
                        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
                            this.attributes = attributes;
                            switch (qName) {
                                case BOLD:
                                case FONT_AWESOME:
                                case LINK:
                                case INFO:
                                case H1:
                                case H2:
                                case H3:
                                case H4: {
                                    styleAttributes.put(qName, Boolean.TRUE);
                                }
                                break;
                                case LIST_ITEM: {
                                    final Node listItemBreak = new Text("\n"); // NOI18N.
                                    listItemBreak.setId("Text"); // NOI18N.
                                    listItemBreak.getStyleClass().add("mfcl-label"); // NOI18N.
                                    final Node preSpacer = new Text("    "); // NOI18N.
                                    preSpacer.setId("Text"); // NOI18N.
                                    preSpacer.getStyleClass().add("mfcl-label"); // NOI18N.
                                    final Node bullet = new Text("•"); // NOI18N.
                                    bullet.setId("Text"); // NOI18N.
                                    bullet.getStyleClass().addAll("mfcl-label");
                                    bullet.pseudoClassStateChanged(FONT_AWESOME_PSEUDO_CLASS, true);
                                    final Node postSpacer = new Text("  "); // NOI18N.
                                    postSpacer.setId("Text"); // NOI18N.
                                    postSpacer.getStyleClass().add("mfcl-label"); // NOI18N.
                                    nodeList.addAll(Arrays.asList(listItemBreak, preSpacer, bullet, postSpacer));
                                }
                                break;
                                case PARAGRAPH: {
                                    final Node paragraphBreak = new Text("\n\n"); // NOI18N.
                                    paragraphBreak.setId("Text"); // NOI18N.
                                    paragraphBreak.getStyleClass().add("mfcl-label"); // NOI18N.
                                    nodeList.add(paragraphBreak);
                                }
                                break;
                                case CONTENT: // NOI18N.
                                default:
                            }
                        }

                        @Override
                        public void characters(char[] ch, int start, int length) throws SAXException {
                            final String string = new String(ch, start, length);
                            Node node = null;
                            if (styleAttributes.get(LINK)) {
                                final Hyperlink hyperlink = new Hyperlink(string);
                                hyperlink.setId("Hyperlink"); // NOI18N.
                                final String url = attributes.getValue("href"); // NOI18N.
                                if (url != null && linkActivator != null) {
                                    hyperlink.setOnAction(actionEvent -> linkActivator.accept(url));
                                }
                                node = hyperlink;
                            } else {
                                final Text text = new Text(string);
                                text.setId("Text"); // NOI18N.
                                text.getStyleClass().add("text"); // NOI18N.
                                node = text;
                            }
                            node.pseudoClassStateChanged(BOLD_PSEUDO_CLASS, styleAttributes.get(BOLD));
                            node.pseudoClassStateChanged(FONT_AWESOME_PSEUDO_CLASS, styleAttributes.get(FONT_AWESOME));
                            final Node zeNode = node;
                            Arrays.asList(INFO, H1, H2, H3, H4)
                                    .stream()
                                    .forEach(style -> {
                                        if (styleAttributes.get(style)) {
                                            zeNode.getStyleClass().add(style);
                                        }
                                    });
                            nodeList.add(node);
                        }

                        @Override
                        public void endElement(String uri, String localName, String qName) throws SAXException {
                            switch (qName) {
                                case BOLD:
                                case FONT_AWESOME:
                                case LINK:
                                case INFO: {
                                    styleAttributes.put(qName, Boolean.FALSE);
                                }
                                break;
                                case H1:
                                case H2:
                                case H3:
                                case H4: {
                                    styleAttributes.put(qName, Boolean.FALSE);
                                    final Node headerBreak = new Text("\n"); // NOI18N.
                                    headerBreak.setId("Text"); // NOI18N.
                                    headerBreak.getStyleClass().add("mfcl-label"); // NOI18N.
                                    nodeList.add(headerBreak);
                                }
                                break;
                                case LIST: {
                                    final Node listBreak = new Text("\n"); // NOI18N.
                                    listBreak.setId("Text"); // NOI18N.
                                    listBreak.getStyleClass().add("mfcl-label"); // NOI18N.
                                    nodeList.add(listBreak);
                                }
                                break;
                                case LINE_BREAK: {
                                    final Node lineBreak = new Text("\n"); // NOI18N.
                                    lineBreak.setId("Text"); // NOI18N.
                                    lineBreak.getStyleClass().add("mfcl-label"); // NOI18N.
                                    nodeList.add(lineBreak);
                                }
                                break;
                                case CONTENT: // NOI18N.
                                default:
                            }
                        }

                    });
                }
            } catch (ParserConfigurationException | SAXException | IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
            result = nodeList;
        }
        return Collections.unmodifiableList(result);
    }

    private Map<String, Boolean> initializeAttributeMap() {
        final Map<String, Boolean> result = new HashMap();
        result.put(BOLD, Boolean.FALSE);
        result.put(FONT_AWESOME, Boolean.FALSE);
        result.put(LINK, Boolean.FALSE);
        result.put(INFO, Boolean.FALSE);
        result.put(H1, Boolean.FALSE);
        result.put(H2, Boolean.FALSE);
        result.put(H3, Boolean.FALSE);
        result.put(H4, Boolean.FALSE);
        return result;
    }

    public String toStrong(final String value) {
        return String.format("<%s>%s</%s>", BOLD, value, BOLD); // NOI18N.        
    }

    public String toInfo(final String value) {
        return String.format("<%s>%s</%s>", INFO, value, INFO); // NOI18N.        
    }

    public String toFontAwesome(final String value) {
        return String.format("<%s>%s</%s>", FONT_AWESOME, value, FONT_AWESOME); // NOI18N.        
    }

    public String lineBreak() {
        return String.format("<%s/>", LINE_BREAK); // NOI18N.      
    }

    public String toListItem(final String value) {
        return String.format("<%s>%s</%s>", LIST_ITEM, value, LIST_ITEM); // NOI18N.        
    }
}
