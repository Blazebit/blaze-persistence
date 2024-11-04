/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.hibernate.base;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.spi.QueryEngine;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.query.sqm.sql.SqmTranslatorFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.6.10
 */
public final class HibernateAccessUtils {

    // Hibernate 6.3 made QueryEngine an interface, whereas before it was a class
    // Since this is a binary incompatible change, we have to resort to reflection for calling methods on that type
    private static final Method GET_SQM_FUNCTION_REGISTRY;
    private static final Method GET_SQM_TRANSLATOR_FACTORY;

    static {
        Method getSqmFunctionRegistry;
        Method getSqmTranslatorFactory;
        try {
            getSqmFunctionRegistry = QueryEngine.class.getMethod("getSqmFunctionRegistry");
            getSqmTranslatorFactory = QueryEngine.class.getMethod("getSqmTranslatorFactory");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Couldn't setup the Blaze-Persistence Hibernate integration. Please report this problem!", e);
        }
        GET_SQM_FUNCTION_REGISTRY = getSqmFunctionRegistry;
        GET_SQM_TRANSLATOR_FACTORY = getSqmTranslatorFactory;
    }

    private HibernateAccessUtils() {
    }

    public static SqmFunctionRegistry getSqmFunctionRegistry(SessionFactoryImplementor factory) {
//        return factory.getQueryEngine().getSqmFunctionRegistry();
        try {
            return (SqmFunctionRegistry) GET_SQM_FUNCTION_REGISTRY.invoke(factory.getQueryEngine());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Couldn't access SqmFunctionRegistry", e);
        }
    }

    public static SqmTranslatorFactory getSqmTranslatorFactory(SessionFactoryImplementor factory) {
//        return factory.getQueryEngine().getSqmTranslatorFactory();
        try {
            return (SqmTranslatorFactory) GET_SQM_TRANSLATOR_FACTORY.invoke(factory.getQueryEngine());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Couldn't access SqmTranslatorFactory", e);
        }
    }
}
