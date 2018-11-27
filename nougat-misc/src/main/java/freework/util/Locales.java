/*
 * Copyright (c) 2005, 2014 vacoor
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package freework.util;

import java.util.Locale;

/**
 * @author vacoor
 */
public abstract class Locales {

    public static Locale parse(String locale) {
        // null, blank, _ return default
        if (locale == null || 0 == locale.trim().length() || "_".equals(locale)) {
            return null;
        }

        int index = locale.indexOf('_');
        if (index < 0) {
            return new Locale(locale);
        }

        // eg: zh
        String language = locale.substring(0, index);
        locale = locale.substring(++index);

        index = locale.indexOf('_');
        if (index < 0) {
            return new Locale(language, locale);
        }

        String country = locale.substring(0, index);
        locale = locale.substring(++index); // variant

        return new Locale(language, country, locale);
    }

    private Locales() {
    }

}
