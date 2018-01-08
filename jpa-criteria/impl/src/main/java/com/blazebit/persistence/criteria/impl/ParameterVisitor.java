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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.criteria.impl.expression.AbstractSelection;

import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Selection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class ParameterVisitor {

    private final Set<ParameterExpression<?>> parameters = new LinkedHashSet<ParameterExpression<?>>();

    public Set<ParameterExpression<?>> getParameters() {
        return parameters;
    }

    public void add(ParameterExpression<?> parameter) {
        parameters.add(parameter);
    }

    public void visit(Selection<?> s) {
        if (s instanceof AbstractSelection<?>) {
            ((AbstractSelection<?>) s).visitParameters(this);
        }
    }
}
