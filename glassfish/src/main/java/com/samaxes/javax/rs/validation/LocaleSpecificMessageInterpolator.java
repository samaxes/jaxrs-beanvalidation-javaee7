/*
 * Integrating Bean Validation with JAX-RS in Java EE 7
 * https://github.com/samaxes/jaxrs-beanvalidation-javaee7
 *
 * Copyright (c) 2013 samaxes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samaxes.javax.rs.validation;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.MessageInterpolator;

/**
 * Delegates to a MessageInterpolator implementation but enforces a given Locale.
 */
public class LocaleSpecificMessageInterpolator implements MessageInterpolator {

    private static final Logger LOGGER = Logger.getLogger(LocaleSpecificMessageInterpolator.class.getName());

    private final MessageInterpolator defaultInterpolator;

    public LocaleSpecificMessageInterpolator(MessageInterpolator interpolator) {
        this.defaultInterpolator = interpolator;
    }

    /**
     * Enforces the locale passed to the interpolator.
     */
    @Override
    public String interpolate(String message, Context context) {
        LOGGER.log(Level.CONFIG, "Selecting the language " + LocaleThreadLocal.get() + " for the error message.");
        return defaultInterpolator.interpolate(message, context, LocaleThreadLocal.get());
    }

    /*
     * (non-Javadoc)
     * @see javax.validation.MessageInterpolator#interpolate(java.lang.String,
     * javax.validation.MessageInterpolator.Context, java.util.Locale)
     */
    @Override
    public String interpolate(String message, Context context, Locale locale) {
        return defaultInterpolator.interpolate(message, context, locale);
    }
}
