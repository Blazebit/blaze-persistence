/*
 * Copyright 2014 Blazebit.
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

import java.util.Iterator;
import java.util.Set;

import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class AbstractManager {

    protected final ResolvingQueryGenerator queryGenerator;
    protected final ParameterManager parameterManager;
    private final VisitorAdapter parameterRegistrationVisitor;

    protected AbstractManager(ResolvingQueryGenerator queryGenerator, ParameterManager parameterManager) {
        this.queryGenerator = queryGenerator;
        this.parameterManager = parameterManager;
        this.parameterRegistrationVisitor = new ParameterRegistrationVisitor(parameterManager);
    }

    protected void registerParameterExpressions(Expression expression) {
        expression.accept(parameterRegistrationVisitor);
    }
    
    protected void build(StringBuilder sb, Set<String> clauses) {
        Iterator<String> iter = clauses.iterator();
        if (iter.hasNext()) {
            sb.append(iter.next());
        }
        while (iter.hasNext()) {
            sb.append(", ");
            sb.append(iter.next());
        }
    }
}
