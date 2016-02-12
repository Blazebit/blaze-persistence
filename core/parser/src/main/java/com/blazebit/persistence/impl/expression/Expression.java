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
package com.blazebit.persistence.impl.expression;

import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.MemberOfPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0
 */
public interface Expression {

    public static interface Visitor {

        // Expressions
        public void visit(PathExpression expression);

        public void visit(PropertyExpression expression);

        public void visit(ParameterExpression expression);

        public void visit(ArrayExpression expression);

        public void visit(CompositeExpression expression);

        public void visit(LiteralExpression expression);

        public void visit(NullExpression expression);

        public void visit(FooExpression expression);

        public void visit(SubqueryExpression expression);

        public void visit(FunctionExpression expression);

        public void visit(WhenClauseExpression expression);

        public void visit(GeneralCaseExpression expression);

        public void visit(SimpleCaseExpression expression);

        // Predicates
        public void visit(AndPredicate predicate);

        public void visit(OrPredicate predicate);

        public void visit(NotPredicate predicate);

        public void visit(EqPredicate predicate);

        public void visit(IsNullPredicate predicate);

        public void visit(IsEmptyPredicate predicate);

        public void visit(MemberOfPredicate predicate);

        public void visit(LikePredicate predicate);

        public void visit(BetweenPredicate predicate);

        public void visit(InPredicate predicate);

        public void visit(GtPredicate predicate);

        public void visit(GePredicate predicate);

        public void visit(LtPredicate predicate);

        public void visit(LePredicate predicate);

        public void visit(ExistsPredicate predicate);
    }

    public static interface ResultVisitor<T> {

        // Expressions
        public T visit(PathExpression expression);

        public T visit(PropertyExpression expression);

        public T visit(ParameterExpression expression);

        public T visit(ArrayExpression expression);

        public T visit(CompositeExpression expression);

        public T visit(LiteralExpression expression);

        public T visit(NullExpression expression);

        public T visit(FooExpression expression);

        public T visit(SubqueryExpression expression);

        public T visit(FunctionExpression expression);

        public T visit(WhenClauseExpression expression);

        public T visit(GeneralCaseExpression expression);

        public T visit(SimpleCaseExpression expression);

        // Predicates
        public T visit(AndPredicate predicate);

        public T visit(OrPredicate predicate);

        public T visit(NotPredicate predicate);

        public T visit(EqPredicate predicate);

        public T visit(IsNullPredicate predicate);

        public T visit(IsEmptyPredicate predicate);

        public T visit(MemberOfPredicate predicate);

        public T visit(LikePredicate predicate);

        public T visit(BetweenPredicate predicate);

        public T visit(InPredicate predicate);

        public T visit(GtPredicate predicate);

        public T visit(GePredicate predicate);

        public T visit(LtPredicate predicate);

        public T visit(LePredicate predicate);

        public T visit(ExistsPredicate predicate);
    }

    public Expression clone();

    /**
     * The expression tree is traversed in pre-order.
     *
     * @param visitor
     */
    public void accept(Visitor visitor);

    public <T> T accept(ResultVisitor<T> visitor);

    /**
     * Returns the trimmed original string representation of the expression.
     *
     * @return The string representation of the expression
     */
    @Override
    public String toString();
}
