/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.webflux.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class Sort {

    public static final org.springframework.data.domain.Sort UNSORTED;

    static {
        org.springframework.data.domain.Sort unsorted = null;
        try {
            for (Method unsortedCandidate : org.springframework.data.domain.Sort.class.getDeclaredMethods()) {
                if ("unsorted".equals(unsortedCandidate.getName())) {
                    unsorted = (org.springframework.data.domain.Sort) unsortedCandidate.invoke(null);
                    break;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        UNSORTED = unsorted;
    }

    private Sort() {
    }

    public static org.springframework.data.domain.Sort asc(String... properties) {
        return of(org.springframework.data.domain.Sort.Direction.ASC, properties);
    }

    public static org.springframework.data.domain.Sort desc(String... properties) {
        return of(org.springframework.data.domain.Sort.Direction.DESC, properties);
    }

    public static org.springframework.data.domain.Sort of(org.springframework.data.domain.Sort.Direction direction, String... properties) {
        org.springframework.data.domain.Sort.Order[] orders = new org.springframework.data.domain.Sort.Order[properties.length];
        for (int i = 0; i < properties.length; i++) {
            orders[i] = new org.springframework.data.domain.Sort.Order(direction, properties[i]);
        }

        return of(orders);
    }

    public static org.springframework.data.domain.Sort of(org.springframework.data.domain.Sort.Order... orders) {
        Object[] arguments = new Object[]{ orders };
        try {
            Method by = org.springframework.data.domain.Sort.class.getMethod("by", org.springframework.data.domain.Sort.Order[].class);
            return (org.springframework.data.domain.Sort) by.invoke(null, arguments);
        } catch (Exception ex) {
            try {
                Constructor constructor = org.springframework.data.domain.Sort.class.getConstructor(org.springframework.data.domain.Sort.Order[].class);
                return (org.springframework.data.domain.Sort) constructor.newInstance(arguments);
            } catch (Exception ex1) {
                throw new RuntimeException(ex1);
            }
        }
    }
}
