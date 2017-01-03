/*
 * Copyright 2014 - 2017 Blazebit.
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

import java.util.LinkedHashSet;
import java.util.Set;

import com.blazebit.persistence.impl.expression.AggregateExpression;
import com.blazebit.persistence.impl.expression.ArithmeticExpression;
import com.blazebit.persistence.impl.expression.ArithmeticFactor;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.EntityLiteral;
import com.blazebit.persistence.impl.expression.EnumLiteral;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.NullExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.TreatExpression;
import com.blazebit.persistence.impl.expression.VisitorAdapter;

class GroupByExpressionGatheringVisitor extends VisitorAdapter {

    private final GroupByUsableDetectionVisitor groupByUsableDetectionVisitor;
    private Set<Expression> expressions = new LinkedHashSet<Expression>();

    public GroupByExpressionGatheringVisitor() {
        this(false);
    }
    
    public GroupByExpressionGatheringVisitor(boolean treatSizeAsAggregate) {
        this.groupByUsableDetectionVisitor = new GroupByUsableDetectionVisitor(treatSizeAsAggregate);
    }

    public Set<Expression> getExpressions() {
        return expressions;
    }

    private boolean handleExpression(Expression expression) {
        if (expression.accept(groupByUsableDetectionVisitor)) {
            return true;
        } else {
            expressions.add(expression);
            return false;
        }
    }

    @Override
    public void visit(PathExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(ArrayExpression expression) {
        throw new IllegalArgumentException("At this point array expressions are not allowed anymore!");
    }

    @Override
    public void visit(TreatExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(PropertyExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(ParameterExpression expression) {
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(EntityLiteral expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(EnumLiteral expression) {
        handleExpression(expression);
    }

    @Override
    public void visit(NullExpression expression) {
        // We skip this, because grouping by null does not make sense
    }

    @Override
    public void visit(SubqueryExpression expression) {
        // We skip subqueries since we can't use them in group bys
    }

    @Override
    public void visit(FunctionExpression expression) {
        if (!(expression instanceof AggregateExpression)) {
            handleExpression(expression);
        }
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        super.visit(expression);
        handleExpression(expression);
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        super.visit(expression);
        handleExpression(expression);
    }
}