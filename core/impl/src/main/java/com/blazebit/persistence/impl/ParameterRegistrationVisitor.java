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

import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
public class ParameterRegistrationVisitor extends VisitorAdapter {

    private final ParameterManager parameterManager;
    private ClauseType clauseType;
    private ClauseType secondClauseType;
    private AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder;

    public ParameterRegistrationVisitor(ParameterManager parameterManager) {
        this.parameterManager = parameterManager;
    }

    @Override
    public void visit(ParameterExpression expression) {
        // Value was not set so we only have an unsatisfied parameter name which we register
        if (AbstractFullQueryBuilder.ID_PARAM_NAME.equals(expression.getName())) {
            throw new IllegalArgumentException("The parameter name '" + expression.getName() + "' is reserved - use a different name");
        } else {
            parameterManager.registerParameterName(expression.getName(), expression.isCollectionValued(), clauseType, queryBuilder);
            if (secondClauseType != null) {
                parameterManager.registerParameterName(expression.getName(), expression.isCollectionValued(), secondClauseType, queryBuilder);
            }
        }
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        // The index will always end up in the on clause
        ClauseType oldClauseType = secondClauseType;
        secondClauseType = ClauseType.JOIN;
        expression.getIndex().accept(this);
        secondClauseType = oldClauseType;
    }

    public ClauseType getClauseType() {
        return clauseType;
    }

    public void setClauseType(ClauseType clauseType) {
        this.clauseType = clauseType;
    }

    public AbstractCommonQueryBuilder<?, ?, ?, ?, ?> getQueryBuilder() {
        return queryBuilder;
    }

    public void setQueryBuilder(AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        this.queryBuilder = queryBuilder;
    }
}
