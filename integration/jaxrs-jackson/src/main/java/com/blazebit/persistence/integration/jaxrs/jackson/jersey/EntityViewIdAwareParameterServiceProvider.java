/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
