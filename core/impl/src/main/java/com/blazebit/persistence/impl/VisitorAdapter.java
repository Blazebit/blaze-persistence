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

import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsMemberOfPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;

/**
 *
 * @author ccbem
 */
public abstract class VisitorAdapter implements Predicate.Visitor, Expression.Visitor {
    @Override
    public void visit(AndPredicate predicate) {
        for (Predicate p : predicate.getChildren()) {
            p.accept(this);
        }
    }

    @Override
    public void visit(OrPredicate predicate) {
        for (Predicate p : predicate.getChildren()) {
            p.accept(this);
        }
    }

    @Override
    public void visit(NotPredicate predicate) {
        predicate.getPredicate().accept(this);
    }

    @Override
    public void visit(EqPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        predicate.getExpression().accept(this);
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        predicate.getExpression().accept(this);
    }

    @Override
    public void visit(IsMemberOfPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(LikePredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getStart().accept(this);
        predicate.getEnd().accept(this);
    }

    @Override
    public void visit(InPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(GtPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(GePredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(LtPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(LePredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
    }

    @Override
    public void visit(PathExpression expression) {
        for(PathElementExpression pathElementExpression : expression.getExpressions()){
            pathElementExpression.accept(this);
        }
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        expression.getIndex().accept(this);
    }

    @Override
    public void visit(PropertyExpression expression) {
    }

    @Override
    public void visit(ParameterExpression expression) {
    }

    @Override
    public void visit(CompositeExpression expression) {
        for (Expression expr : expression.getExpressions()) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(FooExpression expression) {
    }

    @Override
    public void visit(SubqueryExpression expression) {
    }
    
    

    @Override
    public void visit(ExistsPredicate predicate) {
    }
    
}
