/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author vacoor
 * / @see org.hibernate.cfg.beanvalidation.BeanValidationEventListener
 * / @see http://docs.jboss.org/hibernate/validator/4.2/reference/zh-CN/html/validator-integration.html#validator-checkconstraints-orm-hibernateevent
 */
@SuppressWarnings("unused")
public abstract class Validators {

    public static <T> void validate(Validator validator, T object, Class<?>... groups) throws ConstraintViolationException {
        final Set<ConstraintViolation<T>> constraintViolations = validator.validate(object, groups);

        if (constraintViolations.size() > 0) {
            Set<ConstraintViolation<?>> propagatedViolations = new HashSet<ConstraintViolation<?>>(constraintViolations.size());
            Set<String> classNames = new HashSet<String>();
            for (ConstraintViolation<?> violation : constraintViolations) {
                propagatedViolations.add(violation);
                classNames.add(violation.getLeafBean().getClass().getName());
            }

            StringBuilder builder = new StringBuilder();
            builder.append("Validation failed for classes ");
            builder.append(classNames);
            builder.append(" during ");
            builder.append("validate");
            builder.append(" time for groups ");
            builder.append(toString(groups));
            builder.append("\nList of constraint violations:[\n");
            for (ConstraintViolation<?> violation : constraintViolations) {
                builder.append("\t").append(violation.toString()).append("\n");
            }
            builder.append("]");

            throw new ConstraintViolationException(builder.toString(), propagatedViolations);
        }
    }

    public static <T> void validateProperty(Validator validator, T object, String propertyName, Class<?>... groups) throws ConstraintViolationException {
        Set<ConstraintViolation<T>> constraintViolations = validator.validateProperty(object, propertyName, groups);

        if (constraintViolations.size() > 0) {
            Set<ConstraintViolation<?>> propagatedViolations = new HashSet<ConstraintViolation<?>>(constraintViolations.size());
            Set<String> classNames = new HashSet<String>();
            for (ConstraintViolation<?> violation : constraintViolations) {
                propagatedViolations.add(violation);
                classNames.add(violation.getLeafBean().getClass().getName());
            }

            StringBuilder builder = new StringBuilder();
            builder.append("Validation failed for property ");
            builder.append(classNames);
            builder.append(".");
            builder.append(propertyName);
            builder.append(" during ");
            builder.append("validate");
            builder.append(" time for groups ");
            builder.append(toString(groups));
            builder.append("\nList of constraint violations:[\n");
            for (ConstraintViolation<?> violation : constraintViolations) {
                builder.append("\t").append(violation.toString()).append("\n");
            }
            builder.append("]");

            throw new ConstraintViolationException(builder.toString(), propagatedViolations);
        }
    }

    public static <T> void validateValue(Validator validator, Class<T> beanType, String propertyName, Object value, Class<?>... groups) throws ConstraintViolationException {
        Set<ConstraintViolation<T>> constraintViolations = validator.validateValue(beanType, propertyName, value, groups);

        if (constraintViolations.size() > 0) {
            Set<ConstraintViolation<?>> propagatedViolations = new HashSet<ConstraintViolation<?>>(constraintViolations.size());
            for (ConstraintViolation<?> violation : constraintViolations) {
                propagatedViolations.add(violation);
            }

            StringBuilder builder = new StringBuilder();
            builder.append("Validation failed for property value ");
            builder.append(beanType.getName());
            builder.append(".");
            builder.append(propertyName);
            builder.append(" = '");
            builder.append(value);
            builder.append("' during ");
            builder.append("validate");
            builder.append(" time for groups ");
            builder.append(toString(groups));
            builder.append("\nList of constraint violations:[\n");
            for (ConstraintViolation<?> violation : constraintViolations) {
                builder.append("\t").append(violation.toString()).append("\n");
            }
            builder.append("]");

            throw new ConstraintViolationException(builder.toString(), propagatedViolations);
        }
    }

    public static Map<String, String> getFieldReasons(ConstraintViolationException e) {
        return getFieldReasons(e.getConstraintViolations());
    }

    public static Map<String, String> getFieldReasons(Set<? extends ConstraintViolation<?>> constraintViolations) {
        Map<String, String> messages = Maps.newHashMap();
        for (ConstraintViolation<?> v : constraintViolations) {
            messages.put(v.getPropertyPath().toString(), v.getMessage());
        }
        return messages;
    }

    public static Set<String> getReasons(ConstraintViolationException e) {
        return getReasons(e.getConstraintViolations());
    }

    public static Set<String> getReasons(Set<? extends ConstraintViolation<?>> constraintViolations) {
        return getReasons(constraintViolations, " ");
    }

    public static Set<String> getReasons(ConstraintViolationException e, String fieldMessageSeparator) {
        return getReasons(e.getConstraintViolations(), fieldMessageSeparator);
    }

    public static Set<String> getReasons(Set<? extends ConstraintViolation<?>> constraintViolations, String fieldMessageSeparator) {
        Set<String> reasons = Sets.newHashSet();

        for (ConstraintViolation<?> v : constraintViolations) {
            reasons.add(v.getPropertyPath() + fieldMessageSeparator + v.getMessage());
        }
        return reasons;
    }

    private static String toString(Class<?>[] groups) {
        StringBuilder toString = new StringBuilder("[");
        for (int i = 0; i < groups.length; i++) {
            toString.append(i > 0 ? ", " : "").append(groups[i].getName());
        }
        toString.append("]");
        return toString.toString();
    }

    private Validators() {
    }
}
