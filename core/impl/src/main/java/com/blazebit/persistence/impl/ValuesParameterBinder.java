/*
 * Copyright 2014 - 2018 Blazebit.
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
            Object element;
            if (iterator.hasNext() && (element = iterator.next()) != null) {
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

    public String[][] getParameterNames() {
        return parameterNames;
    }

    public ValueRetriever<Object, Object>[] getPathExpressions() {
        return pathExpressions;
    }

    public int size() {
        return parameterNames.length;
    }
}
