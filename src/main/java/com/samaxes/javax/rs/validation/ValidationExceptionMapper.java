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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.ValidationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * {@link ExceptionMapper} for {@link ValidationException}.
 * <p>
 * Send a list of {@link ValidationError} instances in {@link Response} in addition to HTTP 400/500 status code.
 * Supported media types are: {@code application/json} / {@code application/xml} (if appropriate provider is registered
 * on server).
 * </p>
 */
@javax.ws.rs.ext.Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOGGER = Logger.getLogger(ValidationExceptionMapper.class.getName());

    @Context
    private Configuration config;

    @Context
    private Provider<Request> request;

    @Override
    public Response toResponse(final ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            LOGGER.log(Level.FINER, "Following ConstraintViolations has been encountered.", exception);
            final ConstraintViolationException cve = (ConstraintViolationException) exception;
            final Response.ResponseBuilder response = Response.status(getStatus(cve));

            // Entity
            final List<Variant> variants = Variant.mediaTypes(MediaType.APPLICATION_XML_TYPE,
                    MediaType.APPLICATION_JSON_TYPE).build();
            final Variant variant = request.get().selectVariant(variants);
            if (variant != null) {
                response.type(variant.getMediaType());
            }
            response.entity(
                    new GenericEntity<List<ValidationError>>(
                            getEntity(cve.getConstraintViolations()),
                            new GenericType<List<ValidationError>>() {}.getType()
                    )
            );

            return response.build();
        } else {
            LOGGER.log(Level.WARNING, "Unexpected Bean Validation problem.", exception);

            return Response.serverError().entity(exception.getMessage()).build();
        }
    }

    private List<ValidationError> getEntity(final Set<ConstraintViolation<?>> violations) {
        final List<ValidationError> errors = new ArrayList<ValidationError>();

        for (final ConstraintViolation<?> violation : violations) {
            errors.add(new ValidationError(getInvalidValue(violation.getInvalidValue()), violation.getMessage(),
                    violation.getMessageTemplate(), getPath(violation)));
        }

        return errors;
    }

    private String getInvalidValue(final Object invalidValue) {
        if (invalidValue == null) {
            return null;
        }

        if (invalidValue.getClass().isArray()) {
            return Arrays.toString((Object[]) invalidValue);
        }

        return invalidValue.toString();
    }

    private Response.Status getStatus(final ConstraintViolationException exception) {
        return getResponseStatus(exception.getConstraintViolations());
    }

    private Response.Status getResponseStatus(final Set<ConstraintViolation<?>> constraintViolations) {
        final Iterator<ConstraintViolation<?>> iterator = constraintViolations.iterator();

        if (iterator.hasNext()) {
            return getResponseStatus(iterator.next());
        } else {
            return Response.Status.BAD_REQUEST;
        }
    }

    private Response.Status getResponseStatus(final ConstraintViolation<?> constraintViolation) {
        for (final Path.Node node : constraintViolation.getPropertyPath()) {
            final ElementKind kind = node.getKind();

            if (ElementKind.RETURN_VALUE.equals(kind)) {
                return Response.Status.INTERNAL_SERVER_ERROR;
            }
        }

        return Response.Status.BAD_REQUEST;
    }

    private String getPath(final ConstraintViolation<?> violation) {
        final String leafBeanName = violation.getLeafBean().getClass().getSimpleName();
        final String leafBeanCleanName = (leafBeanName.contains("$")) ? leafBeanName.substring(0,
                leafBeanName.indexOf("$")) : leafBeanName;
        final String propertyPath = violation.getPropertyPath().toString();

        return leafBeanCleanName + (!"".equals(propertyPath) ? '.' + propertyPath : "");
    }
}
