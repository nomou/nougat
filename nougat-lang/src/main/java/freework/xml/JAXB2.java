package freework.xml;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.beans.Introspector;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Class that defines convenience methods for common, simple use of JAXB.
 * <p>
 * Unlike {@link javax.xml.bind.JAXB} that only cache the last {@link JAXBContext} used,
 * this class will cache the all {@link JAXBContext} used for improve the performance.
 * </p>
 *
 * @author vacoor
 * @see javax.xml.bind
 * @see javax.xml.bind.JAXB
 * @since 1.0
 */
public final class JAXB2 {
    /**
     * Empty properties.
     */
    public static final Map<String, Object> NONE = Collections.emptyMap();

    /**
     * Cache.
     */
    private static final ConcurrentMap<Class<?>, JAXBContext> JAXB_CONTEXT_MAP = new ConcurrentHashMap<>(64);

    /**
     * No instanciation is allowed.
     */
    private JAXB2() {
    }

    /**
     * Reads in a Java object tree from the given XML string.
     *
     * @param xml          the XML content to unmarshal
     * @param declaredType appropriate JAXB mapped class to hold xml root element
     * @param <T>          JAXB mapped object type
     * @return the JAXB object
     */
    public static <T> T unmarshal(final String xml, final Class<T> declaredType) {
        return unmarshal(new StringReader(xml), declaredType);
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param xml          the entire stream is read as an XML infoset.
     *                     upon a successful completion, the stream will be closed by this method.
     * @param declaredType appropriate JAXB mapped class to hold xml root element
     * @param <T>          JAXB mapped object type
     * @return the JAXB object
     */
    public static <T> T unmarshal(final InputStream xml, final Class<T> declaredType) {
        return unmarshal(new StreamSource(xml), declaredType);
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param xml          The character stream is read as an XML infoset.
     *                     The encoding declaration in the XML will be ignored.
     *                     Upon a successful completion, the stream will be closed by this method.
     * @param declaredType appropriate JAXB mapped class to hold xml root element
     * @param <T>          JAXB mapped object type
     * @return the JAXB object
     */
    public static <T> T unmarshal(final Reader xml, final Class<T> declaredType) {
        return unmarshal(new StreamSource(xml), declaredType);
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param source       the XML infoset that the {@link Source} represents is read.
     * @param declaredType appropriate JAXB mapped class to hold xml root element
     * @param <T>          JAXB mapped object type
     * @return the JAXB object
     */
    public static <T> T unmarshal(final Source source, final Class<T> declaredType) {
        return unmarshal(source, declaredType, Collections.<String, Object>emptyMap());
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param source       the XML infoset that the {@link Source} represents is read.
     * @param declaredType appropriate JAXB mapped class to hold xml root element
     * @param props        set the particular property in the underlying implementation of <tt>Marshaller</tt>.
     * @param <T>          JAXB mapped object type
     */
    public static <T> T unmarshal(final Source source, final Class<T> declaredType, final Map<String, ?> props) {
        try {
            return createUnmarshaller(declaredType, props).unmarshal(source, declaredType).getValue();
        } catch (final JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Reads in a Java object tree from the given XML data.
     *
     * @param node         the document/element to unmarshal XML data from.
     * @param declaredType appropriate JAXB mapped class to hold xml root element
     * @param <T>          JAXB mapped object type
     * @return the JAXB object
     */
    public static <T> T unmarshal(final Node node, final Class<T> declaredType, final Map<String, ?> props) {
        try {
            return createUnmarshaller(declaredType, props).unmarshal(node, declaredType).getValue();
        } catch (final JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Reads in a Java object tree from the given XML stream reader.
     *
     * @param reader       The parser to be read.
     * @param declaredType appropriate JAXB mapped class to hold xml root element
     * @param <T>          JAXB mapped object type
     * @return the JAXB object
     */
    public static <T> T unmarshal(final XMLStreamReader reader, final Class<T> declaredType, final Map<String, ?> props) {
        try {
            return createUnmarshaller(declaredType, props).unmarshal(reader, declaredType).getValue();
        } catch (final JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Reads in a Java object tree from the given XML event reader.
     *
     * @param reader       The parser to be read.
     * @param declaredType appropriate JAXB mapped class to hold xml root element
     * @param <T>          JAXB mapped object type
     * @return the JAXB object
     */
    public static <T> T unmarshal(final XMLEventReader reader, final Class<T> declaredType, final Map<String, ?> props) {
        try {
            return createUnmarshaller(declaredType, props).unmarshal(reader, declaredType).getValue();
        } catch (final JAXBException e) {
            throw new DataBindingException(e);
        }
    }


    /**
     * Writes a Java object tree to XML and store it to as string
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @return the xml content
     */
    public static String marshal(final Object jaxbObject) {
        return marshal(jaxbObject, false);
    }

    /**
     * Writes a Java object tree to XML and store it to as string
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param format     XML data is formatted with linefeeds and indentation
     * @return the xml content
     */
    public static String marshal(final Object jaxbObject, final boolean format) {
        final StringWriter writer = new StringWriter();
        marshal(jaxbObject, writer, format);
        return writer.toString();
    }

    /**
     * Writes a Java object tree to XML and store it to the specified output stream.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param xml        the output stream to store
     */
    public static void marshal(final Object jaxbObject, final OutputStream xml) {
        marshal(jaxbObject, new StreamResult(xml), false);
    }

    /**
     * Writes a Java object tree to XML and store it to the specified output stream.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param format     XML data is formatted with linefeeds and indentation
     * @param xml        the output stream to store
     */
    public static void marshal(final Object jaxbObject, final OutputStream xml, final boolean format) {
        marshal(jaxbObject, new StreamResult(xml), format);
    }

    /**
     * Writes a Java object tree to XML and store it to the specified writer.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param writer     the writer to store
     */
    public static void marshal(final Object jaxbObject, final Writer writer) {
        marshal(jaxbObject, writer, false);
    }

    /**
     * Writes a Java object tree to XML and store it to the specified writer.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param xml        the writer to store
     * @param format     XML data is formatted with linefeeds and indentation
     */
    public static void marshal(final Object jaxbObject, final Writer xml, final boolean format) {
        marshal(jaxbObject, new StreamResult(xml), format);
    }

    /**
     * Writes a Java object tree to XML and store it to the specified location.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param xml        the XML will be sent to the {@link Result} object.
     * @param format     XML data is formatted with linefeeds and indentation
     */
    public static void marshal(final Object jaxbObject, final Result xml, final boolean format) {
        marshal(jaxbObject, xml, Collections.singletonMap(Marshaller.JAXB_FORMATTED_OUTPUT, format));
    }

    /**
     * Writes a Java object tree to XML and store it to the specified location.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param xml        the XML will be sent to the {@link Result} object.
     * @param props      set the particular property in the underlying implementation of <tt>Unmarshaller</tt>.
     */
    @SuppressWarnings("unchecked")
    public static void marshal(final Object jaxbObject, final Result xml, final Map<String, ?> props) {
        try {
            Marshaller marshaller;
            Object jaxbObjectToUse = jaxbObject;
            if (jaxbObject instanceof JAXBElement) {
                final JAXBElement element = (JAXBElement) jaxbObject;
                final Class<?> declaredType = element.getDeclaredType();
                marshaller = createMarshaller(declaredType, props);
            } else {
                final Class<?> declaredType = jaxbObject.getClass();
                final XmlRootElement annotation = declaredType.getAnnotation(XmlRootElement.class);
                if (null == annotation) {
                    final String inferName = inferName(declaredType);
                    jaxbObjectToUse = new JAXBElement(new QName(inferName), declaredType, jaxbObject);
                }
                marshaller = createMarshaller(declaredType, props);
            }
            marshaller.marshal(jaxbObjectToUse, xml);
        } catch (final JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Writes a Java object tree into SAX2 events.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param handler    XML will be sent to this handler as SAX2 events
     * @param props      set the particular property in the underlying implementation of <tt>Unmarshaller</tt>.
     */
    public static void marshal(final Object jaxbObject, final ContentHandler handler, final Map<String, ?> props) {
        marshal(jaxbObject, new SAXResult(handler), props);
    }

    /**
     * Writes a Java object tree into a DOM tree.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param node       DOM nodes will be added as children of this node.
     *                   This parameter must be a Node that accepts children
     * @param props      set the particular property in the underlying implementation of <tt>Unmarshaller</tt>.
     */
    public static void marshal(final Object jaxbObject, final Node node, final Map<String, ?> props) {
        marshal(jaxbObject, new DOMResult(node), props);
    }

    /**
     * Writes a Java object tree into a {@link XMLStreamWriter}.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param writer     XML will be sent to this writer.
     * @param props      set the particular property in the underlying implementation of <tt>Unmarshaller</tt>.
     */
    @SuppressWarnings("unchecked")
    public static void marshal(final Object jaxbObject, final XMLStreamWriter writer, final Map<String, ?> props) {
        try {
            Marshaller marshaller;
            Object jaxbObjectToUse = jaxbObject;
            if (jaxbObject instanceof JAXBElement) {
                final JAXBElement<?> element = (JAXBElement<?>) jaxbObject;
                final Class<?> declaredType = element.getDeclaredType();
                marshaller = createMarshaller(declaredType, props);
            } else {
                final Class<?> declaredType = jaxbObject.getClass();
                final XmlRootElement annotation = declaredType.getAnnotation(XmlRootElement.class);
                if (null == annotation) {
                    final String inferName = inferName(declaredType);
                    jaxbObjectToUse = new JAXBElement(new QName(inferName), declaredType, jaxbObject);
                }
                marshaller = createMarshaller(declaredType, props);
            }
            marshaller.marshal(jaxbObjectToUse, writer);
        } catch (final JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * Writes a Java object tree into a {@link XMLEventWriter}.
     *
     * @param jaxbObject the Java object to be marshalled into XML
     * @param writer     XML will be sent to this writer.
     * @param props      set the particular property in the underlying implementation of <tt>Unmarshaller</tt>.
     */
    @SuppressWarnings("unchecked")
    public static void marshal(final Object jaxbObject, final XMLEventWriter writer, final Map<String, ?> props) {
        try {
            Marshaller marshaller;
            Object jaxbObjectToUse = jaxbObject;
            if (jaxbObject instanceof JAXBElement) {
                final JAXBElement<?> element = (JAXBElement<?>) jaxbObject;
                final Class<?> declaredType = element.getDeclaredType();
                marshaller = createMarshaller(declaredType, props);
            } else {
                final Class<?> declaredType = jaxbObject.getClass();
                final XmlRootElement annotation = declaredType.getAnnotation(XmlRootElement.class);
                if (null == annotation) {
                    final String inferName = inferName(declaredType);
                    jaxbObjectToUse = new JAXBElement(new QName(inferName), declaredType, jaxbObject);
                }
                marshaller = createMarshaller(declaredType, props);
            }
            marshaller.marshal(jaxbObjectToUse, writer);
        } catch (final JAXBException e) {
            throw new DataBindingException(e);
        }
    }


    private static String inferName(Class<?> clazz) {
        return Introspector.decapitalize(clazz.getSimpleName());
    }

    private static Unmarshaller createUnmarshaller(final Class<?> classToBeBound) throws JAXBException {
        return createUnmarshaller(classToBeBound, NONE);
    }

    private static Unmarshaller createUnmarshaller(final Class<?> classToBeBound, final Map<String, ?> props) throws JAXBException {
        final Unmarshaller unmarshaller = getJaxbContext(classToBeBound).createUnmarshaller();
        for (Map.Entry<String, ?> prop : nullSafe(props).entrySet()) {
            unmarshaller.setProperty(prop.getKey(), prop.getValue());
        }
        return unmarshaller;
    }

    private static Marshaller createMarshaller(final Class<?> classToBeBound) throws JAXBException {
        return createMarshaller(classToBeBound, Collections.<String, Object>emptyMap());
    }

    private static Marshaller createMarshaller(final Class<?> classToBeBound, final Map<String, ?> props) throws JAXBException {
        final Marshaller marshaller = getJaxbContext(classToBeBound).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        for (Map.Entry<String, ?> prop : nullSafe(props).entrySet()) {
            marshaller.setProperty(prop.getKey(), prop.getValue());
        }
        return marshaller;
    }

    /**
     * Obtains the {@link JAXBContext} from the given type, by using the cache if possible.
     *
     * @param classToBeBound class to be bound
     */
    private static JAXBContext getJaxbContext(final Class<?> classToBeBound) throws JAXBException {
        if (null == classToBeBound) {
            throw new NullPointerException("classToBeBound must not be null");
        }

        JAXBContext jaxbContext = JAXB_CONTEXT_MAP.get(classToBeBound);
        if (null != jaxbContext) {
            return jaxbContext;
        }
        synchronized (JAXB_CONTEXT_MAP) {
            jaxbContext = JAXB_CONTEXT_MAP.get(classToBeBound);
            if (null == jaxbContext) {
                jaxbContext = createJaxbContext(classToBeBound);
                JAXB_CONTEXT_MAP.putIfAbsent(classToBeBound, jaxbContext);
            }
        }
        return jaxbContext;
    }

    /**
     * @param classesToBeBound list of java classes to be recognized by the new {@link JAXBContext}.
     */
    private static JAXBContext createJaxbContext(final Class<?>... classesToBeBound) throws JAXBException {
        return JAXBContext.newInstance(classesToBeBound);
    }

    private static <K, V> Map<K, V> nullSafe(final Map<K, V> props) {
        return null != props ? props : Collections.<K, V>emptyMap();
    }
}