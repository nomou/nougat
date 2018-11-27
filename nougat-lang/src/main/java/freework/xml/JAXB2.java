/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.xml;

import freework.util.StringUtils2;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * JAXB 2 工具类
 * 简单封装 XML &lt;--&gt; JAXB Object
 * 支持没有使用注解的 Java Bean, JavaBean[] --&gt; XML
 * <p>
 * 没有支持 {@link javax.xml.validation.Schema} 验证
 *
 * @author vacoor
 * @see javax.xml.bind
 */
public abstract class JAXB2 {
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static volatile Map<Class, WeakReference<JAXBContext>> contextCache = new WeakHashMap<Class, WeakReference<JAXBContext>>();
    private static final Object CONTEXT_MONITOR = new Object();


    /**
     * obj --&gt; xml string
     *
     * @param obj
     * @return
     */
    public static String marshal(Object obj) {
        return marshal(obj, DEFAULT_ENCODING);
    }

    /**
     * obj --&gt; xml string
     *
     * @param obj
     * @param encoding
     * @return
     */
    public static String marshal(Object obj, String encoding) {
        StringWriter writer = new StringWriter();
        marshal(obj, writer, encoding);

        return writer.toString();
    }

    /**
     * @param obj
     * @param os
     * @param encoding
     */
    public static void marshal(Object obj, OutputStream os, String encoding) {
        marshal(obj, new StreamResult(os), encoding);
    }

    public static void marshal(Object obj, Writer writer, String encoding) {
        marshal(obj, new StreamResult(writer), encoding);
    }

    public static void marshal(Object obj, URL url, String encoding) {
        try {
            URLConnection conn = url.openConnection();
            conn.setDoInput(false);
            conn.setDoOutput(true);
            conn.connect();
            marshal(obj, conn.getOutputStream(), encoding);
        } catch (IOException e) {
            throw new DataBindingException(e);
        }
    }

    public static void marshal(Object obj, File file, String encoding) {
        marshal(obj, new StreamResult(file), encoding);
    }


    /**
     * 将 JAXB 对象转换为 XML
     * 也支持普通JavaBean / JavaBean[] 转换为XML
     * <p>
     * 注:
     * JAXB 中要求编组对象必须是JAXBElement或 @XmlRootElement标注的对象
     * 这里进行处理,可以自动将普通JavaBean转换为JAXBElement进行编组
     * <p>
     *
     * @param obj
     * @param result
     * @param encoding
     * @see javax.xml.transform.Result
     */
    @SuppressWarnings("unchecked")
    public static void marshal(Object obj, Result result, String encoding) {
        try {
            JAXBContext context;

            /* 如果是JAXBElement,则获取定义类型的上下文 */
            if (obj instanceof JAXBElement) {
                context = getContext(((JAXBElement) obj).getDeclaredType());
            } else {

                /* 对于使用Annotation 和 普通JavaBean 直接获取定义类的context */
                Class<?> clazz = obj.getClass();
                context = getContext(clazz);

                /* 如果没有使用@XmlRootElement注解,需要转换为JAXBElement才能marshal */
                if (!clazz.isInterface() && !clazz.isAnnotationPresent(XmlRootElement.class)) {
                    String qname = StringUtils2.uncapitalize(clazz.getSimpleName());

                    /* any +"s"作为QName */
                    if (clazz.isArray()) {
                        qname = StringUtils2.uncapitalize(clazz.getComponentType().getSimpleName()) + 's';
                    } /*else if (obj instanceof Collection<?>) {
                        qname = "items";
                        obj = AnyWrapper.wrap((Collection<?>)obj);
                    }
                    */

                    obj = new JAXBElement(new QName(qname), clazz, obj);
                }
            }
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            marshaller.marshal(obj, result);
        } catch (JAXBException je) {
            throw new DataBindingException(je);
        }
    }

    /**
     * xml String --&gt; object
     *
     * @param xml
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T unmarshal(String xml, Class<T> type) {
        StringReader reader = new StringReader(xml);
        return unmarshal(reader, type);
    }

    /**
     * 解组 xml 到给定 JAXB 对象
     *
     * @param xml
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T unmarshal(File xml, Class<T> type) {
        return unmarshal(new StreamSource(xml), type);
    }

    public static <T> T unmarshal(InputStream is, Class<T> type) {
        return unmarshal(new StreamSource(is), type);
    }

    public static <T> T unmarshal(Reader reader, Class<T> type) {
        return unmarshal(new StreamSource(reader), type);
    }

    public static <T> T unmarshal(URL url, Class<T> type) {
        return unmarshal(new StreamSource(url.toExternalForm()), type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(InputSource is, Class<T> type) {
        try {
            return (T) getContext(type).createUnmarshaller().unmarshal(is);
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * 将javax.xml.transform.Source 转换为给定的类实例
     *
     * @param source
     * @param type
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(Source source, Class<T> type) {
        try {
            // 解组时如果传入类型,则会返回一个泛型JAXBElement对象value为解组后对象,
            // 否则返回一个解组后的对象,但是需要强转,
            // return (S) getContext(type).createUnmarshaller().unmarshal(source);
            /*-
              该方式始终都会返回一个对象, 即使失败，当一个对象可能是A,B时unmarshal 失败无法判定
              JAXBElement<T> je = getContext(type).createUnmarshaller().unmarshal(source, type);
            return je.getValue();
             */
            Object obj = getContext(type).createUnmarshaller().unmarshal(source);
            if (!type.isInstance(obj)) {
                throw new JAXBException(String.format("cannot be unmarshal %s to %s", source, type));
            }
            return (T) obj;
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        }
    }

    /**
     * 使用 给定 QName 来包装数组并编组为 xml
     *
     * @param elements  要序列化的对象数组
     * @param wrapQName 根节点 QName
     * @param <T>
     * @return
     */
    public static <T> String marshalAny(T[] elements, String wrapQName) {
        return marshalAny(elements, wrapQName, DEFAULT_ENCODING);
    }

    /**
     * 使用 给定 QName 来包装数组并编组为 xml
     *
     * @param elements  要序列化的对象数组
     * @param wrapQName 根节点 QName
     * @param encoding  字符编码
     * @param <T>
     * @return
     */
    public static <T> String marshalAny(T[] elements, String wrapQName, String encoding) {
        StringWriter writer = new StringWriter();
        marshalAny(elements, wrapQName, writer, encoding);
        return writer.toString();
    }

    /**
     * 使用 给定 QName 来包装数组并编组为 xml
     *
     * @param elements  要序列化的对象数组
     * @param wrapQName 根节点 QName
     * @param writer    输出流
     * @param encoding  字符编码
     * @param <T>       T
     */
    public static <T> void marshalAny(T[] elements, String wrapQName, Writer writer, String encoding) {
        try {
            Class<?> compType = elements.getClass().getComponentType();
            if (Object.class == compType) {
                throw new UnsupportedOperationException("unsupported object array");
            }

            AnyWrapper<T> wrapper = AnyWrapper.wrap(elements);
            JAXBElement<AnyWrapper> root = new JAXBElement<AnyWrapper>(new QName(wrapQName), AnyWrapper.class, wrapper);

            JAXBContext context = getContext(compType);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
            marshaller.marshal(root, writer);
        } catch (JAXBException je) {
            throw new DataBindingException(je);
        }
    }

    public static <T> T[] unmarshalAny(String xml, Class<T> type, String ignore) {
        try {
            JAXBContext context = getContext(type);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.getUnmarshallerHandler().skippedEntity(ignore);
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 允许获取 Context,进行其他操作eg: 验证
     *
     * @param type
     * @return
     * @throws javax.xml.bind.JAXBException
     */
    public static JAXBContext getContext(Class<?> type) throws JAXBException {
        JAXBContext context = null;
        WeakReference<JAXBContext> ref = contextCache.get(type);
        if (ref != null) {
            context = ref.get();
            if (context != null) {
                return context;
            }
        }
        synchronized (CONTEXT_MONITOR) {
            ref = contextCache.get(type);
            if (ref == null || ref.get() == null) {
                context = JAXBContext.newInstance(type, AnyWrapper.class);
                contextCache.put(type, new WeakReference<JAXBContext>(context));
            }
        }
        return context;
    }

    /**
     * 封装Root Element 是 Collection 的情况.
     */
    public static class AnyWrapper<T> {
        @XmlAnyElement
        protected T[] any;

        static <T> AnyWrapper<T> wrap(T[] any) {
            AnyWrapper<T> root = new AnyWrapper<T>();
            root.any = any;
            return root;
        }
    }

    private JAXB2() {
    }
}
