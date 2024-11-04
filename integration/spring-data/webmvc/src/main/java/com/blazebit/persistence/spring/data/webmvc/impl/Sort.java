/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.webmvc.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author Christian Beikov
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class Sort {

    private Sort() {
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
