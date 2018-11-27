/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.web.util;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * 不依赖 web 环境的 javax.el 引擎(JSR 341).
 * <p/>
 *
 * @author vacoor
 * @see https://docs.oracle.com/cd/E19226-01/820-7627/gjddd/
 * @since 1.0
 */
public class JaxElEngine {
    /**
     * el 表达式工厂.
     */
    protected final ExpressionFactory factory;

    /**
     * el 解析器.
     */
    protected final CompositeELResolver resolver;

    /**
     * el上下文.
     */
    protected ELContext context;

    public JaxElEngine() {
        factory = ExpressionFactory.newInstance();
        resolver = new CompositeELResolver();
        resolver.add(new ArrayELResolver());
        resolver.add(new ListELResolver());
        resolver.add(new MapELResolver());
        resolver.add(new BeanELResolver());
        context = new SimpleContext(resolver);
    }

    /**
     * 将给定变量和值存入当前上下文.
     *
     * @param key   变量名称
     * @param value 变量值
     * @param <V>   变量值类型
     */
    public <V> void setVariable(final String key, final V value) {
        this.setVariable(key, value, value.getClass());
    }

    /**
     * 将给定类型变量和值存入当前上下文.
     *
     * @param key   变量名称
     * @param value 变量值
     * @param type  变量值class
     * @param <V>   变量值类型
     */
    public <V> void setVariable(final String key, final V value, final Class<? extends V> type) {
        final VariableMapper variableMapper = context.getVariableMapper();
        final ValueExpression valueExpression = factory.createValueExpression(value, type);
        variableMapper.setVariable(key, valueExpression);
    }

    /**
     * 使用给定的命名空间在上下文中注册方法, 注册名称为方法名称.
     *
     * @param namespace 命名空间
     * @param method    方法
     */
    public void setFunction(final String namespace, final Method method) {
        ((Functions) context.getFunctionMapper()).setFunction(namespace, method.getName(), method);
    }

    /**
     * 使用给定的命名空间和名称在上下文中注册方法.
     *
     * @param namespace 命名空间
     * @param name      方法名称
     * @param method    方法
     */
    public void setFunction(final String namespace, final String name, final Method method) {
        ((Functions) context.getFunctionMapper()).setFunction(namespace, name, method);
    }

    /**
     * 解析 EL 表达式 eg:<br>
     * "abc" --&gt; "abc" <br>
     * "${abc}" --&gt; Object <br>
     * "map.key: ${map.key} ${map.value}" --&gt; "map.key key value"
     *
     * @param expression 表达式
     * @return 表达式值
     */
    public String resolve(final String expression) {
        return resolve(expression, String.class);
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(final String expression, final Class<T> type) {
        final ValueExpression expr = factory.createValueExpression(context, expression, type);
        return (T) expr.getValue(context);
    }

    static class SimpleContext extends ELContext {
        private final Functions functions = new Functions();
        private final Variables variables = new Variables();
        private ELResolver resolver;

        SimpleContext(ELResolver resolver) {
            this.resolver = resolver;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ELResolver getELResolver() {
            return resolver;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FunctionMapper getFunctionMapper() {
            return functions;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public VariableMapper getVariableMapper() {
            return variables;
        }
    }

    /**
     * Functions mapper.
     */
    static class Functions extends FunctionMapper {
        final Map<String, Method> internal = new HashMap<String, Method>();

        /**
         * {@inheritDoc}
         */
        @Override
        public Method resolveFunction(final String namespace, final String localName) {
            return internal.get(namespace + ":" + localName);
        }

        /**
         * 使用给定的命名空间和名称在上下文中注册方法.
         *
         * @param namespace 命名空间
         * @param localName 方法名称
         * @param method    方法
         */
        public void setFunction(final String namespace, final String localName, final Method method) {
            if (0 == (Modifier.STATIC & method.getModifiers())) {
                throw new IllegalArgumentException("method is not a static method");
            }
            internal.put(namespace + ":" + localName, method);
        }
    }

    /**
     * Variables mapper.
     */
    static class Variables extends VariableMapper {
        final Map<String, ValueExpression> internal = new HashMap<String, ValueExpression>();

        /**
         * {@inheritDoc}
         */
        @Override
        public ValueExpression resolveVariable(final String variable) {
            return internal.get(variable);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ValueExpression setVariable(final String variable, final ValueExpression expression) {
            return internal.put(variable, expression);
        }
    }
}
