/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.integration.jaxrs.jackson.jersey;

import com.blazebit.persistence.integration.jaxrs.EntityViewId;
import org.glassfish.jersey.model.internal.spi.ParameterServiceProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class EntityViewIdAwareParameterServiceProvider implements ParameterServiceProvider {
    @Override
    public Map<Class, Parameter.ParamAnnotationHelper> getParameterAnnotationHelperMap() {
        Map<Class, org.glassfish.jersey.model.Parameter.ParamAnnotationHelper> m = new WeakHashMap<>();
        m.put(EntityViewId.class, new org.glassfish.jersey.model.Parameter.ParamAnnotationHelper<EntityViewId>() {
            public String getValueOf(EntityViewId a) {
                return a.value().isEmpty() ? a.name() : a.value();
            }

            public org.glassfish.jersey.model.Parameter.Source getSource() {
                return org.glassfish.jersey.model.Parameter.Source.ENTITY;
            }
        });
        return m;
    }

    @Override
    public Parameter.ParamCreationFactory<? extends org.glassfish.jersey.model.Parameter> getParameterCreationFactory() {
        return new org.glassfish.jersey.model.Parameter.ParamCreationFactory<org.glassfish.jersey.model.Parameter>() {
            public boolean isFor(Class<?> clazz) {
                return clazz == org.glassfish.jersey.model.Parameter.class;
            }

            public org.glassfish.jersey.model.Parameter createParameter(Annotation[] markers, Annotation marker, org.glassfish.jersey.model.Parameter.Source source, String sourceName, Class<?> rawType, Type type, boolean encoded, String defaultValue) {
                return new EntityViewIdAwareParameterServiceProvider.Parameter(markers, marker, source, sourceName, rawType, type, encoded, defaultValue);
            }

            public org.glassfish.jersey.model.Parameter createBeanParameter(Annotation[] markers, Annotation marker, org.glassfish.jersey.model.Parameter.Source source, String sourceName, Class<?> rawType, Type type, boolean encoded, String defaultValue) {
                return this.createParameter(markers, marker, source, sourceName, rawType, type, encoded, defaultValue);
            }
        };
    }

    /**
     * Custom parameter implementation required because construtor of org.glassfish.jersey.model.Parameter is protected.
     */
    private static class Parameter extends org.glassfish.jersey.model.Parameter {

        Parameter(Annotation[] markers, Annotation marker, Source source, String sourceName, Class<?> rawType, Type type, boolean encoded, String defaultValue) {
            super(markers, marker, source, sourceName, rawType, type, encoded, defaultValue);
        }
    }
}
