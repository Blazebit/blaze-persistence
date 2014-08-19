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
package com.blazebit.persistence.impl.predicate;

import com.blazebit.persistence.impl.expression.ExpressionVisitorAdapter;

/**
 *
 * @author Moritz Becker
 * @since 1.0
 */
public abstract class VisitorAdapter extends ExpressionVisitorAdapter implements Predicate.Visitor {

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
    public void visit(NotInPredicate predicate) {
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
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
    public void visit(ExistsPredicate predicate) {
    }

}
