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

package com.blazebit.persistence.parser.expression;

import com.blazebit.persistence.parser.expression.modifier.ArithmeticFactorExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.ArithmeticLeftExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.ArithmeticRightExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.ArrayExpressionBaseModifier;
import com.blazebit.persistence.parser.expression.modifier.ArrayExpressionIndexModifier;
import com.blazebit.persistence.parser.expression.modifier.BetweenPredicateEndModifier;
import com.blazebit.persistence.parser.expression.modifier.BetweenPredicateLeftModifier;
import com.blazebit.persistence.parser.expression.modifier.BetweenPredicateStartModifier;
import com.blazebit.persistence.parser.expression.modifier.BinaryExpressionPredicateLeftModifier;
import com.blazebit.persistence.parser.expression.modifier.BinaryExpressionPredicateRightModifier;
import com.blazebit.persistence.parser.expression.modifier.ExpressionListModifier;
import com.blazebit.persistence.parser.expression.modifier.ExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.GeneralCaseExpressionDefaultModifier;
import com.blazebit.persistence.parser.expression.modifier.InPredicateLeftModifier;
import com.blazebit.persistence.parser.expression.modifier.ListIndexExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.MapEntryExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.MapKeyExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.MapValueExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.SimpleCaseExpressionOperandModifier;
import com.blazebit.persistence.parser.expression.modifier.TreatExpressionModifier;
import com.blazebit.persistence.parser.expression.modifier.TrimExpressionCharacterModifier;
import com.blazebit.persistence.parser.expression.modifier.TrimExpressionSourceModifier;
import com.blazebit.persistence.parser.expression.modifier.UnaryExpressionPredicateModifier;
import com.blazebit.persistence.parser.expression.modifier.WhenClauseExpressionConditionModifier;
import com.blazebit.persistence.parser.expression.modifier.WhenClauseExpressionResultModifier;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;
import com.blazebit.persistence.parser.predicate.BinaryExpressionPredicate;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.ExistsPredicate;
import com.blazebit.persistence.parser.predicate.GePredicate;
import com.blazebit.persistence.parser.predicate.GtPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.LePredicate;
import com.blazebit.persistence.parser.predicate.LikePredicate;
import com.blazebit.persistence.parser.predicate.LtPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.predicate.UnaryExpressionPredicate;

import java.util.List;

/**
 * This is a visitor that can be used to collect expression modifier references into an expression.
 * When a visit method returns {@linkplain Boolean#TRUE}, an expression modifier for the expression is generated
 * and the {@link #onModifier(ExpressionModifier)} method is called. The modifier is bound to the embedding expression
 * i.e. the parent expression and can be used for reading or replacing the expression.
 *
 * @author Moritz Becker
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class ExpressionModifierCollectingResultVisitorAdapter implements Expression.ResultVisitor<Boolean> {

    protected abstract void onModifier(ExpressionModifier modifier);

    public void visit(ExpressionModifier parentModifier) {
        if (Boolean.TRUE == parentModifier.get().accept(this)) {
            onModifier(parentModifier);
        }
    }

    @Override
    public Boolean visit(ArrayExpression expression) {
        if (Boolean.TRUE == expression.getBase().accept(this)) {
            onModifier(new ArrayExpressionBaseModifier(expression));
        }
        if (Boolean.TRUE == expression.getIndex().accept(this)) {
            onModifier(new ArrayExpressionIndexModifier(expression));
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(TreatExpression expression) {
        if (Boolean.TRUE == expression.getExpression().accept(this)) {
            onModifier(new TreatExpressionModifier(expression));

        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(PropertyExpression expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(ParameterExpression expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(ListIndexExpression expression) {
        if (Boolean.TRUE == expression.getPath().accept(this)) {
            onModifier(new ListIndexExpressionModifier(expression));

        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(MapEntryExpression expression) {
        if (Boolean.TRUE == expression.getPath().accept(this)) {
            onModifier(new MapEntryExpressionModifier(expression));

        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(MapKeyExpression expression) {
        if (Boolean.TRUE == expression.getPath().accept(this)) {
            onModifier(new MapKeyExpressionModifier(expression));

        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(MapValueExpression expression) {
        if (Boolean.TRUE == expression.getPath().accept(this)) {
            onModifier(new MapValueExpressionModifier(expression));

        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(NullExpression expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        List<Expression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (Boolean.TRUE == expressions.get(i).accept(this)) {
                onModifier(new ExpressionListModifier(expressions, i));
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(TypeFunctionExpression expression) {
        return visit((FunctionExpression) expression);
    }

    @Override
    public Boolean visit(TrimExpression expression) {
        if (Boolean.TRUE == expression.getTrimSource().accept(this)) {
            onModifier(new TrimExpressionSourceModifier(expression));
        }
        final Expression trimCharacter = expression.getTrimCharacter();
        if (trimCharacter != null && Boolean.TRUE == trimCharacter.accept(this)) {
            onModifier(new TrimExpressionCharacterModifier(expression));
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(WhenClauseExpression expression) {
        if (Boolean.TRUE == expression.getCondition().accept(this)) {
            onModifier(new WhenClauseExpressionConditionModifier(expression));
        }
        if (Boolean.TRUE == expression.getResult().accept(this)) {
            onModifier(new WhenClauseExpressionResultModifier(expression));
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(GeneralCaseExpression expression) {
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (Boolean.TRUE == expressions.get(i).accept(this)) {
                onModifier(new ExpressionListModifier(expressions, i));
            }
        }
        if (expression.getDefaultExpr() != null && Boolean.TRUE == expression.getDefaultExpr().accept(this)) {
            onModifier(new GeneralCaseExpressionDefaultModifier(expression));
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(SimpleCaseExpression expression) {
        if (Boolean.TRUE == expression.getCaseOperand().accept(this)) {
            onModifier(new SimpleCaseExpressionOperandModifier(expression));
        }
        return visit((GeneralCaseExpression) expression);
    }

    @Override
    public Boolean visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (Boolean.TRUE == expressions.get(i).accept(this)) {
                onModifier(new ExpressionListModifier(expressions, i));
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(ArithmeticExpression expression) {
        if (Boolean.TRUE == expression.getLeft().accept(this)) {
            onModifier(new ArithmeticLeftExpressionModifier(expression));
        }
        if (Boolean.TRUE == expression.getRight().accept(this)) {
            onModifier(new ArithmeticRightExpressionModifier(expression));
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(ArithmeticFactor expression) {
        if (Boolean.TRUE == expression.getExpression().accept(this)) {
            onModifier(new ArithmeticFactorExpressionModifier(expression));
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(NumericLiteral expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(BooleanLiteral expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(StringLiteral expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(DateLiteral expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(TimeLiteral expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(TimestampLiteral expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(EnumLiteral expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(EntityLiteral expression) {
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(CompoundPredicate predicate) {
        List<Predicate> predicates = predicate.getChildren();
        int size = predicates.size();
        for (int i = 0; i < size; i++) {
            if (Boolean.TRUE == predicates.get(i).accept(this)) {
                onModifier(new ExpressionListModifier(predicates, i));
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(EqPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(IsNullPredicate predicate) {
        return visit((UnaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(IsEmptyPredicate predicate) {
        return visit((UnaryExpressionPredicate) predicate);
    }

    private Boolean visit(UnaryExpressionPredicate predicate) {
        if (Boolean.TRUE == predicate.getExpression().accept(this)) {
            onModifier(new UnaryExpressionPredicateModifier(predicate));
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(MemberOfPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(LikePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(BetweenPredicate predicate) {
        if (Boolean.TRUE == predicate.getLeft().accept(this)) {
            onModifier(new BetweenPredicateLeftModifier(predicate));
        }
        if (Boolean.TRUE == predicate.getStart().accept(this)) {
            onModifier(new BetweenPredicateStartModifier(predicate));
        }
        if (Boolean.TRUE == predicate.getEnd().accept(this)) {
            onModifier(new BetweenPredicateEndModifier(predicate));
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(InPredicate predicate) {
        if (Boolean.TRUE == predicate.getLeft().accept(this)) {
            onModifier(new InPredicateLeftModifier(predicate));
        }

        List<Expression> expressions = predicate.getRight();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            if (Boolean.TRUE == expressions.get(i).accept(this)) {
                onModifier(new ExpressionListModifier(expressions, i));
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(GtPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(GePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(LtPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(LePredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(ExistsPredicate predicate) {
        return visit((UnaryExpressionPredicate) predicate);
    }

    private Boolean visit(BinaryExpressionPredicate predicate) {
        if (Boolean.TRUE == predicate.getLeft().accept(this)) {
            onModifier(new BinaryExpressionPredicateLeftModifier(predicate));
        }
        if (Boolean.TRUE == predicate.getRight().accept(this)) {
            onModifier(new BinaryExpressionPredicateRightModifier(predicate));
        }
        return Boolean.FALSE;
    }

}
