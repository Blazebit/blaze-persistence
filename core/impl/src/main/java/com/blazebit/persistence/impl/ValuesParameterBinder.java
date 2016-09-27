package com.blazebit.persistence.impl;

import com.blazebit.lang.ValueRetriever;

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
    private final ValueRetriever<Object, Object>[] pathExpressions;

    public ValuesParameterBinder(String[][] parameterNames, ValueRetriever<Object, Object>[] pathExpressions) {
        this.parameterNames = parameterNames;
        this.pathExpressions = pathExpressions;
    }

    public void bind(Query query, Collection<Object> value) {
        Iterator<Object> iterator = value.iterator();
        for (int i = 0; i < parameterNames.length; i++) {
            if (iterator.hasNext()) {
                Object element = iterator.next();
                for (int j = 0; j < parameterNames[i].length; j++) {
                    query.setParameter(parameterNames[i][j], pathExpressions[j].getValue(element));
                }
            } else {
                for (int j = 0; j < parameterNames[i].length; j++) {
                    query.setParameter(parameterNames[i][j], null);
                }
            }
        }
    }

    public int size() {
        return parameterNames.length;
    }
}
