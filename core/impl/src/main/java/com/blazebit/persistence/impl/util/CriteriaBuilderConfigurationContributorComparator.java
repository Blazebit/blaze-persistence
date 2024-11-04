/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.util;

import com.blazebit.persistence.spi.CriteriaBuilderConfigurationContributor;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * A comparator for comparing CriteriaBuilderConfigurationContributor implementations.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.2
 */
public class CriteriaBuilderConfigurationContributorComparator implements Comparator<CriteriaBuilderConfigurationContributor> {

    @Override
    public int compare(CriteriaBuilderConfigurationContributor o1, CriteriaBuilderConfigurationContributor o2) {
        Integer o1Priority = getPriority(o1.getClass());
        Integer o2Priority = getPriority(o2.getClass());

        int result = o1Priority == null ?
                o2Priority == null ? 0 : 1 :
                o2Priority == null ? -1 : Integer.compare(o1Priority, o2Priority);

        if (result == 0) {
            result = o1.getClass().getName().compareTo(o2.getClass().getName());
        }

        return result;
    }

    private static Integer getPriority(Class<?> clasz) {
        for (Annotation annotation : clasz.getAnnotations()) {
            final Class<? extends Annotation> annotationClass = annotation.annotationType();
            if ("Priority".equals(annotationClass.getSimpleName())) {
                try {
                    Method value = annotationClass.getMethod("value");
                    return (Integer) value.invoke(annotation);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassCastException e) {
                    continue;
                }
            }
        }
        return null;
    }

}
