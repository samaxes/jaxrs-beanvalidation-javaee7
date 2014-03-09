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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import javax.validation.ParameterNameProvider;
import javax.validation.Validation;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.validation.ValidationConfig;

/**
 * Custom configuration of validation. This configuration can define custom:
 * <ul>
 * <li>MessageInterpolator - interpolates a given constraint violation message.</li>
 * <li>TraversableResolver - determines if a property can be accessed by the Bean Validation provider.</li>
 * <li>ConstraintValidatorFactory - instantiates a ConstraintValidator instance based off its class.
 * <li>ParameterNameProvider - provides names for method and constructor parameters.</li> *
 * </ul>
 */
@Provider
public class ValidationConfigurationContextResolver implements ContextResolver<ValidationConfig> {

    /**
     * Get a context of type {@code ValidationConfiguration} that is applicable to the supplied type.
     *
     * @param type the class of object for which a context is desired
     * @return a context for the supplied type or {@code null} if a context for the supplied type is not available from
     *         this provider.
     */
    @Override
    public ValidationConfig getContext(Class<?> type) {
        final ValidationConfig config = new ValidationConfig();

        config.setMessageInterpolator(new LocaleSpecificMessageInterpolator(Validation.byDefaultProvider().configure()
                .getDefaultMessageInterpolator()));
        config.setParameterNameProvider(new CustomParameterNameProvider());

        return config;
    }

    /**
     * If method input parameters are invalid, this class returns actual parameter names instead of the default ones (
     * {@code arg0, arg1, ...})
     */
    private class CustomParameterNameProvider implements ParameterNameProvider {

        private final ParameterNameProvider nameProvider;

        public CustomParameterNameProvider() {
            nameProvider = Validation.byDefaultProvider().configure().getDefaultParameterNameProvider();
        }

        @Override
        public List<String> getParameterNames(final Constructor<?> constructor) {
            return nameProvider.getParameterNames(constructor);
        }

        @Override
        public List<String> getParameterNames(final Method method) {
            if ("getPerson".equals(method.getName())) {
                return Arrays.asList("id");
            }
            if ("createPerson".equals(method.getName())) {
                return Arrays.asList("id", "name");
            }
            return nameProvider.getParameterNames(method);
        }
    }
}
