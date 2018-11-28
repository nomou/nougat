package freework.xml;

import freework.util.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * TODO complete me.
 * 对 JAXP 简单封装
 *
 * @author vacoor
 * @see javax.xml.parsers
 */
@SuppressWarnings("PMD")
abstract class JAXP {
    private static final Charset UTF_8 = Charset.forName("UTF-8");


    public static void parse(final String xml, final DefaultHandler handler) {
        parse(new StringReader(xml), handler);
    }

    public static void parse(final InputStream in, final Charset charset, final DefaultHandler handler) {
        parse(new InputStreamReader(in, null != charset ? charset : UTF_8), handler);
    }

    public static void parse(final Reader reader, final DefaultHandler handler) {
        parse(new InputSource(reader), handler);
    }

    public static void parse(final InputSource source, final DefaultHandler handler) {
        try {
            final SAXParser parser = newParser();
            parser.parse(source, handler);
        } catch (final Exception ex) {
            Throwables.unchecked(ex);
        }
    }

    public static Document read(final String xml) {
        return read(new StringReader(xml));
    }

    public static Document read(final InputStream in, Charset charset) {
        charset = null != charset ? charset : UTF_8;
        return read(new InputStreamReader(in, charset));
    }

    public static Document read(final Reader reader) {
        return read(new InputSource(reader));
    }

    public static Document read(final InputSource source) {
        try {
            DocumentBuilder builder = newBuilder();
            return builder.parse(source);
        } catch (final Exception ex) {
            return Throwables.unchecked(ex);
        }
    }

    /* ************************************
     *            SAXParser
     * ************************************/

    /**
     * 根据给定的 DTD 或 Schema 创建一个 SAXParser
     * 如果希望捕获错误, Handler 可以继承 {@link ErrorCaptureHandler}
     *
     * @return
     */
    public static SAXParser newParser() {
        return newParser(null);
    }

    public static SAXParser newParser(final Schema schema) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setXIncludeAware(true);
            factory.setNamespaceAware(true);
            factory.setSchema(schema);
            return factory.newSAXParser();
        } catch (final Exception e) {
            return Throwables.unchecked("Failed to create SAX Parser!", e);
        }
    }

    /* **************************************
     *          Document Builder
     * **************************************/

    /**
     * Creates a new {@code DocumentBuilder} using default configuration.
     *
     * @return the new DocumentBuilder object
     */
    public static DocumentBuilder newBuilder() {
        return newBuilder(null);
    }

    public static DocumentBuilder newBuilder(final Schema schema) {
        try {
            final DocumentBuilderFactory factory = newBuilderFactory(schema);
            return factory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException("failed to create DocumentBuilder ~!", e);
        }
    }

    public static DocumentBuilder newBuilder(final boolean ignoreComments, final boolean ignoreContentWhitespace,
                                             final boolean coalescing, final boolean allowedExternalEntity,
                                             final Schema schema) {
        try {
            final DocumentBuilderFactory factory = newBuilderFactory(
                    ignoreComments, ignoreContentWhitespace, coalescing, allowedExternalEntity, schema
            );
            return factory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException("failed to create DocumentBuilder ~!", e);
        }
    }

    public static DocumentBuilderFactory newBuilderFactory() {
        return newBuilderFactory(null);
    }

    public static DocumentBuilderFactory newBuilderFactory(final Schema schema) {
        return newBuilderFactory(true, true, true, false, schema);
    }

    public static DocumentBuilderFactory newBuilderFactory(final boolean ignoreComments, final boolean ignoreContentWhitespace,
                                                           final boolean coalescing, final boolean allowedExternalEntity,
                                                           final Schema schema) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(ignoreComments);
        factory.setIgnoringElementContentWhitespace(ignoreContentWhitespace);
        factory.setCoalescing(coalescing);
        factory.setSchema(schema);

        if (!allowedExternalEntity) {
            doXxePrevention(factory);
        }
        return factory;
    }

    /**
     * Disable outer entity for XEE attach.
     *
     * @param dbf DocumentBuilderFactory
     * @return DocumentBuilderFactory
     * @see <a href="https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=23_5">微信XML解析存在的安全问题指引</a>
     * @see <a href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#C.2FC.2B.2B">XML External Entity (XXE) Prevention Cheat Sheet</a>
     */
    private static DocumentBuilderFactory doXxePrevention(final DocumentBuilderFactory dbf) {
        dbf.setExpandEntityReferences(false);

        String feature = null;
        try {
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, true);

            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities

            // JDK7+ - http://xml.org/sax/features/external-general-entities
            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, false);

            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities

            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);

            // Disable external DTDs as well
            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            dbf.setFeature(feature, false);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            /*-
             * And, per Timothy Morgan: "If for some reason support for inline DOCTYPEs are a requirement, then
             * ensure the entity settings are disabled (as shown above) and beware that SSRF attacks
             * (http://cwe.mitre.org/data/definitions/918.html) and denial
             * of service attacks (such as billion laughs or decompression bombs via "jar:") are a risk."
             */

            // remaining parser logic
        } catch (final ParserConfigurationException e) {
            // This should catch a failed setFeature feature
            // logger.info("ParserConfigurationException was thrown. The feature '{}' is probably not supported by your XML processor.", feature);
        }
        return dbf;
    }

    /* ***************************************
     *            DTD / Schema
     * ***************************************/

    public static Schema newSchema(final InputStream schema, final Charset charset, boolean dtd) {
        return newSchema(new InputStreamReader(schema, null != charset ? charset : UTF_8), dtd);
    }

    public static Schema newSchema(final Reader reader, final boolean dtd) {
        return newSchema(new StreamSource(reader), dtd);
    }

    public static Schema newSchema(final Source schema, final boolean dtd) {
        final String schemaLang = dtd ? XMLConstants.XML_DTD_NS_URI : XMLConstants.W3C_XML_SCHEMA_NS_URI;
        try {
            return SchemaFactory.newInstance(schemaLang).newSchema(schema);
        } catch (final SAXException e) {
            return Throwables.unchecked("Failed to create schema, type is " + (dtd ? "DTD" : "SCHEMA"), e);
        }
    }

    /**
     * The error capture handler for SAX.
     */
    private static class ErrorCaptureHandler extends DefaultHandler {
        private static final Logger LOG = LoggerFactory.getLogger(ErrorCaptureHandler.class);

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            LOG.debug("tag: {}", qName);
        }

        @Override
        public void warning(SAXParseException e) throws SAXException {
            // you can choose not to handle it
            throw new SAXException(getMessage("Warning", e));
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw new SAXException(getMessage("Error", e));
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw new SAXException(getMessage("Fatal Error", e));
        }

        private String getMessage(String level, SAXParseException e) {
            return ("Parsing " + level + "\n" + "Line:    " + e.getLineNumber() + "\n"
                    + "Message: " + e.getMessage());
        }
    }
}
