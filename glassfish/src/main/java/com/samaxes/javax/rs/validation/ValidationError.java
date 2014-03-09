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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Default validation error entity to be included in {@code Response}.
 */
@XmlRootElement
public final class ValidationError {

    private String invalidValue;

    private String message;

    private String messageTemplate;

    private String path;

    public ValidationError() {
    }

    public ValidationError(final String invalidValue, final String message, final String messageTemplate,
            final String path) {
        this.invalidValue = invalidValue;
        this.message = message;
        this.messageTemplate = messageTemplate;
        this.path = path;
    }

    /**
     * Returns the string representation of the value failing to pass the constraint.
     *
     * @return the value failing to pass the constraint.
     */
    public String getInvalidValue() {
        return invalidValue;
    }

    /**
     * Set the value failing to pass the constraint.
     *
     * @param invalidValue the value failing to pass the constraint.
     */
    public void setInvalidValue(final String invalidValue) {
        this.invalidValue = invalidValue;
    }

    /**
     * Return the interpolated error message for this validation error.
     *
     * @return the interpolated error message for this validation error.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the interpolated error message for this validation error.
     *
     * @param message the interpolated error message for this validation error.
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Return the non-interpolated error message for this validation error.
     *
     * @return the non-interpolated error message for this validation error.
     */
    public String getMessageTemplate() {
        return messageTemplate;
    }

    /**
     * Set the non-interpolated error message for this validation error.
     *
     * @param messageTemplate the non-interpolated error message for this validation error.
     */
    public void setMessageTemplate(final String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    /**
     * Return the string representation of the property path to the value.
     *
     * @return the string representation of the property path to the value.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the string representation of the property path to the value.
     *
     * @param path the string representation of the property path to the value.
     */
    public void setPath(final String path) {
        this.path = path;
    }
}
