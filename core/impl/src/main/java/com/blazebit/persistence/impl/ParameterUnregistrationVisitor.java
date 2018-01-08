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

import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public class ParameterUnregistrationVisitor extends VisitorAdapter {

    private final ParameterManager parameterManager;
    private ClauseType clauseType;

    public ParameterUnregistrationVisitor(ParameterManager parameterManager) {
        this.parameterManager = parameterManager;
    }

    @Override
    public void visit(ParameterExpression expression) {
        parameterManager.unregisterParameterName(expression.getName(), clauseType);
    }

    public void setClauseType(ClauseType clauseType) {
        this.clauseType = clauseType;
    }
}
