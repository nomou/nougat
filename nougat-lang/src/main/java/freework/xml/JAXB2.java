/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.xml;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.beans.Introspector;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Class that defines convenience methods for common, simple use of JAXB.
 *
 * <p>NOTE: This class provides the following features compared to {@link javax.xml.bind.JAXB}:
 * <br>1. Added support for XML string literals
 * <br>2. {@code marshal} method supports arrays
 * <br>3. Added the {@code marshalAny} method to process the array</p>
 *
 * @author vacoor
 * @see javax.xml.bind
 * @see javax.xml.bind.JAXB
 * @since 1.0
 */
@SuppressWarnings({"PMD.ClassNamingShouldBeCamelRule", "PMD.AbstractClassShouldStartWithAbstractNamingRule"})
public abstract class JAXB2 {
    /**
     * UTF-8 charset.
     */
    private static final String UTF_8 = "UTF-8";

    /**
     * The JAXB context monitor.
     */
    private static final Object CONTEXT_MONITOR = new Object();

    /**
     * The JAXB context cache.
     */
    private static volatile Map<Class, WeakReference<JAXBContext>> CACHE = new WeakHashMap<Class, WeakReference<JAXBContext>>();

    /**
     * Non-instantiate.
     */
    private JAXB2() {
    }

    /**
     * Writes a Java object tree into XML string.
     *
     * @param obj the Java object to be marshalled into XML
     * @return the xml string
     */
    public static String marshal(final Object obj) {
        return marshal(obj, UTF_8);
    }

    /**
     * Writes a Java object tree into XML string.
     *
     * @param obj      the Java object to be marshalled into XML
     * @param encoding the xml encoding
     * @return the xml string
     */
    public static String marshal(final Object obj, final String encoding) {
        final StringWriter writer = new StringWriter();
        marshal(obj, writer, encoding);
        return writer.toString();
    }

    /**
     * Writes a Java object tree to XML and store it to the specified location.
     *
     * @param obj      the Java object to be marshalled into XML
     * @param file     XML will be written to this file. If it already exists, it will be overwritten
     * @param encoding the xml encoding
     */
    public static void marshal(final Object obj, final File file, final String encoding) {
        marshal(obj, new StreamResult(file), encoding);
    }

    /**
     * Writes a Java object tree to XML and store it to the specified output stream.
     *
     * @param obj      the Java object to be marshalled into XML
     * @param out      the output stream to store
     * @param encoding the xml encoding
     */
    public static void marshal(final Object obj, final OutputStream out, final String encoding) {
        marshal(obj, new StreamResult(out), encoding);
    }

    /**
     * Writes a Java object tree to XML and store it to the specified writer.
     *
     * @param obj      the Java object to be marshalled into XML
     * @param writer   the writer stream to store
     * @param encoding the xml encoding
     */
    public static void marshal(final Object obj, final Writer writer, final String encoding) {
        marshal(obj, new StreamResult(writer), encoding);
    }


    /**
     * Writes a Java object tree to XML and store it to the specified location.
     *
     * @param obj      the Java object to be marshalled into XML
     * @param result   represents the receiver of XML
     * @param encoding the xml encoding
     * @see javax.xml.transform.Result
     */
    @SuppressWarnings("unchecked")
    public static void marshal(final Object obj, final Result result, final String encoding) {
        try {
            JAXBContext context;
            Object source = obj;
            if (obj instanceof JAXBElement) {
                // if obj is a instance of JAXBElement, using its definition type
                context = getContext(((JAXBElement<?>) obj).getDeclaredType());
            } else {
                // using getClass() if it is annotation bean / normal bean
                final Class<?> clazz = obj.getClass();
                final XmlRootElement annotation = clazz.getAnnotation(XmlRootElement.class);

                context = getContext(clazz);
                if (null == annotation) {
                    String qname;
                    if (clazz.isArray()) {
                        qname = clazz.getComponentType().getSimpleName() + "s";
                    } else {
                        qname = clazz.getSimpleName();
                    }
                    qname = Introspector.decapitalize(qname);
                    source = new JAXBElement(new QName(qname), clazz, obj);
                }
            }

            final Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            marshaller.marshal(source, result);
        } catch (final JAXBException je) {
            throw new DataBindingException(je);
        }
    }

    /**
     * Reads in a Java object tree from the given XML string.
     *
     * @param xml  the XML string
     * @param type the jaxb object class
     * @param <T>  the jaxb object type
     * @return the jaxb object
     */
    public static <T> T unmarshal(final String xml, final Class<T> type) {
        return unmarshal(new StringReader(xml), type);
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param xml  reads the entire file as XML.
     * @param type the jaxb object class
     * @param <T>  the jaxb object type
     * @return the jaxb object
     */
    public static <T> T unmarshal(File xml, Class<T> type) {
        return unmarshal(new StreamSource(xml), type);
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param in   the entire stream is read as an XML infoset.
     *             upon a successful completion, the stream will be closed by this method.
     * @param type the jaxb object class
     * @param <T>  the jaxb object type
     * @return the jaxb object
     */
    public static <T> T unmarshal(final InputStream in, final Class<T> type) {
        return unmarshal(new StreamSource(in), type);
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param reader the character stream is read as an XML infoset.
     * @param type   the jaxb object class
     * @param <T>    the jaxb object type
     * @return the jaxb object
     */
    public static <T> T unmarshal(final Reader reader, final Class<T> type) {
        return unmarshal(new StreamSource(reader), type);
    }

    /**
     * Reads in a Java object tree from the given XML input.
     *
     * @param source the XML infoset that the {@link Source} represents is read.
     * @param type   the jaxb object class
     * @param <T>    the jaxb object type
     * @return the jaxb object
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(final Source source, final Class<T> type) {
        try {
            return getContext(type).createUnmarshaller().unmarshal(source, type).getValue();
        } catch (final JAXBException e) {
            throw new DataBindingException(e);
        }
    }


    /**
     * Writes a Java array using the QName wrapper into a string.
     *
     * @param elements  the Java array to be marshalled into XML
     * @param wrapQName the QName of wrapper
     */
    public static <T> String marshalAny(final T[] elements, final String wrapQName) {
        return marshalAny(elements, wrapQName, UTF_8);
    }

    /**
     * Writes a Java array using the QName wrapper into a string.
     *
     * @param elements  the Java array to be marshalled into XML
     * @param wrapQName the QName of wrapper
     * @param encoding  the xml encoding
     */
    public static <T> String marshalAny(final T[] elements, final String wrapQName, final String encoding) {
        final StringWriter writer = new StringWriter();
        marshalAny(elements, wrapQName, writer, encoding);
        return writer.toString();
    }

    /**
     * Writes a Java array using the QName wrapper into the writer.
     *
     * @param elements  the Java array to be marshalled into XML
     * @param wrapQName the QName of wrapper
     * @param writer    the writer to store
     * @param encoding  the xml encoding
     */
    public static <T> void marshalAny(final T[] elements, final String wrapQName, final Writer writer, final String encoding) {
        try {
            final Class<?> componentType = elements.getClass().getComponentType();
            if (Object.class == componentType) {
                throw new UnsupportedOperationException("unsupported object array");
            }

            final AnyWrapper<T> wrapper = AnyWrapper.wrap(elements);
            final JAXBElement<AnyWrapper> root = new JAXBElement<AnyWrapper>(new QName(wrapQName), AnyWrapper.class, wrapper);
            final Marshaller marshaller = getContext(componentType).createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            marshaller.marshal(root, writer);
        } catch (final JAXBException je) {
            throw new DataBindingException(je);
        }
    }

    /**
     * Obtains the {@link JAXBContext} from the given type, by using the cache if possible.
     *
     * @param type the bound type
     * @return the jaxb context
     * @throws javax.xml.bind.JAXBException if an error occurs
     */
    public static JAXBContext getContext(final Class<?> type) throws JAXBException {
        JAXBContext context = null;
        WeakReference<JAXBContext> ref = CACHE.get(type);
        if (ref != null) {
            context = ref.get();
            if (context != null) {
                return context;
            }
        }
        synchronized (CONTEXT_MONITOR) {
            ref = CACHE.get(type);
            if (ref == null || ref.get() == null) {
                context = JAXBContext.newInstance(type, AnyWrapper.class);
                CACHE.put(type, new WeakReference<JAXBContext>(context));
            }
        }
        return context;
    }

    /**
     * Wrapper for array.
     */
    private static class AnyWrapper<T> {
        @XmlAnyElement
        protected T[] any;

        static <T> AnyWrapper<T> wrap(final T[] any) {
            final AnyWrapper<T> root = new AnyWrapper<T>();
            root.any = any;
            return root;
        }
    }
}
