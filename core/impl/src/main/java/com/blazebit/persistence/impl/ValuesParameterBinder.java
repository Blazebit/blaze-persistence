/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl;

import com.blazebit.persistence.spi.AttributeAccessor;

import javax.persistence.Query;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ValuesParameterBinder {

    private final String[][] parameterNames;
    private final AttributeAccessor<Object, Object>[] pathExpressions;

    public ValuesParameterBinder(String[][] parameterNames, AttributeAccessor<Object, Object>[] pathExpressions) {
        this.parameterNames = parameterNames;
        this.pathExpressions = pathExpressions;
    }

    public void bind(Query query, Collection<Object> value) {
        Iterator<Object> iterator = value.iterator();
        for (int i = 0; i < parameterNames.length; i++) {
            Object element;
            if (iterator.hasNext() && (element = iterator.next()) != null) {
                for (int j = 0; j < parameterNames[i].length; j++) {
                    if (pathExpressions[j] == null) {
                        query.setParameter(parameterNames[i][j], element);
                    } else {
                        query.setParameter(parameterNames[i][j], pathExpressions[j].getNullSafe(element));
                    }
                }
            } else {
                for (int j = 0; j < parameterNames[i].length; j++) {
                    query.setParameter(parameterNames[i][j], null);
                }
            }
        }
    }

    public String[][] getParameterNames() {
        return parameterNames;
    }

    public AttributeAccessor<Object, Object>[] getPathExpressions() {
        return pathExpressions;
    }

    public int size() {
        return parameterNames.length;
    }
}
