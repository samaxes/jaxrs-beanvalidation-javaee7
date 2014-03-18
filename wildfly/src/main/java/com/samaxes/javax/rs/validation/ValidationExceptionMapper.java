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

import java.util.Iterator;
import java.util.List;

import javax.validation.ConstraintDeclarationException;
import javax.validation.ConstraintDefinitionException;
import javax.validation.GroupDefinitionException;
import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.jboss.resteasy.api.validation.Validation;
import org.jboss.resteasy.api.validation.ViolationReport;

/**
 * {@link ExceptionMapper} for {@link ValidationException}.
 * <p>
 * Send a {@link ViolationReport} in {@link Response} in addition to HTTP 400/500 status code. Supported media types
 * are: {@code application/json} / {@code application/xml} (if appropriate provider is registered on server).
 * </p>
 *
 * @see org.jboss.resteasy.api.validation.ResteasyViolationExceptionMapper The original WildFly class:
 *      {@code org.jboss.resteasy.api.validation.ResteasyViolationExceptionMapper}
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Override
    public Response toResponse(ValidationException exception) {
        if (exception instanceof ConstraintDefinitionException) {
            return buildResponse(unwrapException(exception), MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
        }
        if (exception instanceof ConstraintDeclarationException) {
            return buildResponse(unwrapException(exception), MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
        }
        if (exception instanceof GroupDefinitionException) {
            return buildResponse(unwrapException(exception), MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
        }
        if (exception instanceof ResteasyViolationException) {
            ResteasyViolationException resteasyViolationException = ResteasyViolationException.class.cast(exception);
            Exception e = resteasyViolationException.getException();
            if (e != null) {
                return buildResponse(unwrapException(e), MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
            } else if (resteasyViolationException.getReturnValueViolations().size() == 0) {
                return buildViolationReportResponse(resteasyViolationException, Status.BAD_REQUEST);
            } else {
                return buildViolationReportResponse(resteasyViolationException, Status.INTERNAL_SERVER_ERROR);
            }
        }
        return buildResponse(unwrapException(exception), MediaType.TEXT_PLAIN, Status.INTERNAL_SERVER_ERROR);
    }

    protected Response buildResponse(Object entity, String mediaType, Status status) {
        ResponseBuilder builder = Response.status(status).entity(entity);
        builder.type(MediaType.TEXT_PLAIN);
        builder.header(Validation.VALIDATION_HEADER, "true");
        return builder.build();
    }

    protected Response buildViolationReportResponse(ResteasyViolationException exception, Status status) {
        ResponseBuilder builder = Response.status(status);
        builder.header(Validation.VALIDATION_HEADER, "true");

        // Check standard media types.
        MediaType mediaType = getAcceptMediaType(exception.getAccept());
        if (mediaType != null) {
            builder.type(mediaType);
            builder.entity(new ViolationReport(exception));
            return builder.build();
        }

        // Default media type.
        builder.type(MediaType.TEXT_PLAIN);
        builder.entity(exception.toString());
        return builder.build();
    }

    protected String unwrapException(Throwable t) {
        StringBuffer sb = new StringBuffer();
        doUnwrapException(sb, t);
        return sb.toString();
    }

    private void doUnwrapException(StringBuffer sb, Throwable t) {
        if (t == null) {
            return;
        }
        sb.append(t.toString());
        if (t.getCause() != null && t != t.getCause()) {
            sb.append('[');
            doUnwrapException(sb, t.getCause());
            sb.append(']');
        }
    }

    private MediaType getAcceptMediaType(List<MediaType> accept) {
        Iterator<MediaType> it = accept.iterator();
        while (it.hasNext()) {
            MediaType mt = it.next();
            /*
             * application/xml media type causes an exception:
             * org.jboss.resteasy.core.NoMessageBodyWriterFoundFailure: Could not find MessageBodyWriter for response
             * object of type: org.jboss.resteasy.api.validation.ViolationReport of media type: application/xml
             */
            /*if (MediaType.APPLICATION_XML_TYPE.getType().equals(mt.getType())
                    && MediaType.APPLICATION_XML_TYPE.getSubtype().equals(mt.getSubtype())) {
                return MediaType.APPLICATION_XML_TYPE;
            }*/
            if (MediaType.APPLICATION_JSON_TYPE.getType().equals(mt.getType())
                    && MediaType.APPLICATION_JSON_TYPE.getSubtype().equals(mt.getSubtype())) {
                return MediaType.APPLICATION_JSON_TYPE;
            }
        }
        return null;
    }
}
