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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * 对 JAXP 简单封装
 *
 * @author vacoor
 * @see javax.xml.parsers
 */
public abstract class JAXP {
    private static final String SCHEMA_FEATURE = "http://apache.org/xml/features/validation/schema";
    private static final Charset UTF_8 = Charset.forName("UTF-8");


    public static void parse(String xml, DefaultHandler handler) {
        parse(new StringReader(xml), handler);
    }

    public static void parse(File file, DefaultHandler handler) {
        try {
            parse(new FileInputStream(file), handler);
        } catch (FileNotFoundException e) {
            Throwables.unchecked(e);
        }
    }

    public static void parse(InputStream is, DefaultHandler handler) {
        parse(is, null, handler);
    }

    public static void parse(InputStream is, Charset charset, DefaultHandler handler) {
        charset = null != charset ? charset : UTF_8;
        parse(new InputStreamReader(is, charset), handler);
    }

    public static void parse(Reader reader, DefaultHandler handler) {
        parse(new InputSource(reader), handler);
    }

    public static void parse(InputSource inputSource, DefaultHandler handler) {
        try {
            SAXParser parser = createSAXParser();
            parser.parse(inputSource, handler);
        } catch (Exception ex) {
            Throwables.unchecked(ex);
        }
    }


    public static Document parse(String xml) {
        return parse(new StringReader(xml));
    }

    public static Document parse(File file) {
        Document doc;
        try {
            doc = parse(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            doc = Throwables.unchecked(e);
        }
        return doc;
    }

    public static Document parse(InputStream is) {
        return parse(is, (Charset) null);
    }

    public static Document parse(InputStream is, Charset charset) {
        charset = null != charset ? charset : UTF_8;
        return parse(new InputStreamReader(is, charset));
    }

    public static Document parse(Reader reader) {
        return parse(new InputSource(reader));
    }

    public static Document parse(InputSource inputSource) {
        Document doc;
        try {
            DocumentBuilder builder = newBuilder();
            doc = builder.parse(inputSource);
        } catch (Exception ex) {
            doc = Throwables.unchecked(ex);
        }
        return doc;
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
    public static SAXParser createSAXParser() {
        return createSAXParser(null);
    }

    //
    public static SAXParser createSAXParser(Schema schema) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setXIncludeAware(true);
            factory.setNamespaceAware(true);
            /**
             * @deprecated
             * factory.setValidating(true);
             * factory.setFeature(SCHEMA_FEATURE, true);
             */
            factory.setSchema(schema);
            return factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SAX Parser !", e);
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

    public static Schema createSchema(File file, boolean dtd) {
        Schema schema;
        try {
            schema = createSchema(new FileInputStream(file), dtd);
        } catch (FileNotFoundException e) {
            schema = Throwables.unchecked(e);
        }
        return schema;
    }

    public static Schema createSchema(InputStream is, boolean dtd) {
        return createSchema(is, (Charset) null, dtd);
    }

    public static Schema createSchema(InputStream schema, Charset charset, boolean dtd) {
        charset = null != charset ? charset : UTF_8;
        return createSchema(new InputStreamReader(schema, charset), dtd);
    }

    public static Schema createSchema(Reader reader, boolean dtd) {
        return createSchema(new StreamSource(reader), dtd);
    }

    public static Schema createSchema(Source schema, boolean dtd) {
        String schemaLang = dtd ? XMLConstants.XML_DTD_NS_URI : XMLConstants.W3C_XML_SCHEMA_NS_URI;
        try {
            return SchemaFactory.newInstance(schemaLang).newSchema(schema);
        } catch (SAXException e) {
            throw new RuntimeException("Failed to create schema, type is " + (dtd ? "DTD" : "SCHEMA"));
        }
    }

    // ---- 错误捕获处理器
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
