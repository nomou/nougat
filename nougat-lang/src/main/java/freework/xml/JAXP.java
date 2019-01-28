package freework.xml;

import freework.util.Throwables;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Class that defines convenience methods for common, simple use of JAXP.
 *
 * @author vacoor
 * @see javax.xml.parsers
 * @since 1.0
 */
@SuppressWarnings({"PMD.ClassNamingShouldBeCamelRule", "PMD.AbstractClassShouldStartWithAbstractNamingRule"})
public abstract class JAXP {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Non-instantiate.
     */
    private JAXP() {
    }

    /**
     * Parse the content given string as XML using the specified {@link org.xml.sax.helpers.DefaultHandler}.
     *
     * @param xml     The string content to be parsed.
     * @param handler The SAX DefaultHandler to use.
     */
    public static void parse(final String xml, final DefaultHandler handler) {
        parse(new StringReader(xml), handler);
    }

    /**
     * Parse the content given input stream as XML using the specified {@link org.xml.sax.helpers.DefaultHandler}.
     *
     * @param in      The input stream containing the content to be parsed.
     * @param charset The charset of input stream
     * @param handler The SAX DefaultHandler to use.
     */
    public static void parse(final InputStream in, final Charset charset, final DefaultHandler handler) {
        parse(new InputStreamReader(in, null != charset ? charset : UTF_8), handler);
    }

    /**
     * Parse the content given reader as XML using the specified {@link org.xml.sax.helpers.DefaultHandler}.
     *
     * @param reader  The reader containing the content to be parsed.
     * @param handler The SAX DefaultHandler to use.
     */
    public static void parse(final Reader reader, final DefaultHandler handler) {
        parse(new InputSource(reader), handler);
    }

    /**
     * Parse the content given {@link org.xml.sax.InputSource} as XML using the specified {@link org.xml.sax.helpers.DefaultHandler}.
     *
     * @param source  The InputSource containing the content to be parsed.
     * @param handler The SAX DefaultHandler to use.
     */
    public static void parse(final InputSource source, final DefaultHandler handler) {
        try {
            final SAXParser parser = newParser();
            parser.parse(source, handler);
        } catch (final Exception ex) {
            Throwables.unchecked(ex);
        }
    }

    /**
     * Parses the content of the given input stream as an XML document.
     *
     * @param xml the string of xml
     * @return A new DOM Document object.
     */
    public static Document read(final String xml) {
        return read(new StringReader(xml));
    }

    /**
     * Parses the content of the given input stream as an XML document.
     *
     * @param in      the input stream
     * @param charset the charset
     * @return A new DOM Document object.
     */
    public static Document read(final InputStream in, Charset charset) {
        charset = null != charset ? charset : UTF_8;
        return read(new InputStreamReader(in, charset));
    }

    /**
     * Parses the content of the given reader as an XML document.
     *
     * @param reader the reader
     * @return A new DOM Document object.
     */
    public static Document read(final Reader reader) {
        return read(new InputSource(reader));
    }

    /**
     * Parses the content of the given input source as an XML document.
     *
     * @param source the input source
     * @return A new DOM Document object.
     */
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
     * Creates a new instance of a SAXParser.
     *
     * @return the SAXParser
     */
    public static SAXParser newParser() {
        return newParser(null);
    }

    /**
     * Creates a new instance of a SAXParser.
     *
     * @param schema <code>Schema</code> to use, <code>null</code> to remove a schema.
     * @return the SAXParser
     */
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
     * Creates a new instance of {@code DocumentBuilder} using default configuration.
     *
     * @return the new DocumentBuilder object
     */
    public static DocumentBuilder newBuilder() {
        return newBuilder(null);
    }

    /**
     * Creates a new instance of <code>DocumentBuilder</code>.
     *
     * @param schema the schema
     * @return the {@code DocumentBuilder} instance
     */
    public static DocumentBuilder newBuilder(final Schema schema) {
        try {
            final DocumentBuilderFactory factory = newBuilderFactory(schema);
            return factory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException("failed to create DocumentBuilder ~!", e);
        }
    }

    /**
     * Creates a new instance of <code>DocumentBuilder</code>.
     *
     * @param ignoreComments          true if the parser will ignore comments
     * @param ignoreContentWhitespace true if the factory must eliminate whitespace in element content
     *                                (sometimes known loosely as 'ignorable whitespace') when parsing XML documents.
     *                                Note that only whitespace which is directly contained within element content that
     *                                has an element only content model (see XML Rec 3.2.1) will be eliminated.
     * @param coalescing              true if the parser produced will convert CDATA nodes
     *                                to Text nodes and append it to the adjacent (if any)
     *                                text node; false otherwise.
     * @param allowedExternalEntity   true if the parser produced will expand entity reference nodes; false otherwise.
     * @param schema                  <code>Schema</code> to use, <code>null</code> to remove a schema.
     * @return the {@code DocumentBuilder} instance
     */
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

    /**
     * Creates a new instance of <code>DocumentBuilderFactory</code>.
     *
     * @return the {@code DocumentBuilderFactory} instance
     */
    public static DocumentBuilderFactory newBuilderFactory() {
        return newBuilderFactory(null);
    }

    /**
     * Creates a new instance of <code>DocumentBuilderFactory</code>.
     *
     * @param schema <code>Schema</code> to use, <code>null</code> to remove a schema.
     * @return the {@code DocumentBuilderFactory} instance
     */
    public static DocumentBuilderFactory newBuilderFactory(final Schema schema) {
        return newBuilderFactory(false, false, true, false, schema);
    }

    /**
     * Creates a new instance of <code>DocumentBuilderFactory</code>.
     *
     * @param ignoreComments          true if the parser will ignore comments
     * @param ignoreContentWhitespace true if the factory must eliminate whitespace in element content
     *                                (sometimes known loosely as 'ignorable whitespace') when parsing XML documents.
     *                                Note that only whitespace which is directly contained within element content that
     *                                has an element only content model (see XML Rec 3.2.1) will be eliminated.
     * @param coalescing              true if the parser produced will convert CDATA nodes
     *                                to Text nodes and append it to the adjacent (if any)
     *                                text node; false otherwise.
     * @param allowedExternalEntity   true if the parser produced will expand entity reference nodes; false otherwise.
     * @param schema                  <code>Schema</code> to use, <code>null</code> to remove a schema.
     * @return the {@code DocumentBuilderFactory} instance
     */
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

    /**
     * Creates a schema instance with the given schema reader.
     *
     * @param input   the input stream of schema
     * @param charset the charset of schema
     * @param dtd     true if the source is dtd
     * @return the schema instance
     */
    public static Schema newSchema(final InputStream input, final Charset charset, boolean dtd) {
        return newSchema(new InputStreamReader(input, null != charset ? charset : UTF_8), dtd);
    }

    /**
     * Creates a schema instance with the given schema reader.
     *
     * @param reader the reader of schema
     * @param dtd    true if the source is dtd
     * @return the schema instance
     */
    public static Schema newSchema(final Reader reader, final boolean dtd) {
        return newSchema(new StreamSource(reader), dtd);
    }

    /**
     * Creates a schema instance with the given schema source.
     *
     * @param schema the source of schema
     * @param dtd    true if the source is dtd
     * @return the schema instance
     */
    public static Schema newSchema(final Source schema, final boolean dtd) {
        final String schemaLang = dtd ? XMLConstants.XML_DTD_NS_URI : XMLConstants.W3C_XML_SCHEMA_NS_URI;
        try {
            return SchemaFactory.newInstance(schemaLang).newSchema(schema);
        } catch (final SAXException e) {
            return Throwables.unchecked("Failed to create schema, type is " + (dtd ? "DTD" : "SCHEMA"), e);
        }
    }
}
