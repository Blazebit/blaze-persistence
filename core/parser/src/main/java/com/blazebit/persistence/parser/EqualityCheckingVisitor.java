/*
 * Copyright 2014 - 2021 Blazebit.
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

package com.blazebit.persistence.parser;

import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.DateLiteral;
import com.blazebit.persistence.parser.expression.EntityLiteral;
import com.blazebit.persistence.parser.expression.EnumLiteral;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.NumericLiteral;
import com.blazebit.persistence.parser.expression.OrderByItem;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PathReference;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.SimpleCaseExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TimeLiteral;
import com.blazebit.persistence.parser.expression.TimestampLiteral;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.TrimExpression;
import com.blazebit.persistence.parser.expression.TypeFunctionExpression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.expression.WindowDefinition;
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
import com.blazebit.persistence.parser.util.ExpressionUtils;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class EqualityCheckingVisitor implements Expression.ResultVisitor<Boolean> {

    private String alias;
    private Expression referenceExpression;

    public boolean isEqual(Expression referenceExpression, Expression expression, String thisAlias) {
        this.alias = thisAlias;
        this.referenceExpression = referenceExpression;
        Boolean result = expression.accept(this);
        return result == Boolean.FALSE;
    }

    @Override
    public Boolean visit(PathExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        PathExpression reference = (PathExpression) referenceExpression;
        List<PathElementExpression> referenceExpressions = reference.getExpressions();
        List<PathElementExpression> expressions = expression.getExpressions();
        PathExpression leftMostPathExpression = ExpressionUtils.getLeftMostPathExpression(expression);
        int size = expressions.size();
        if (leftMostPathExpression.getExpressions().get(0) instanceof PropertyExpression) {
            PropertyExpression propertyExpression = (PropertyExpression) leftMostPathExpression.getExpressions().get(0);
            if (ArrayExpression.ELEMENT_NAME.equals(propertyExpression.getProperty())) {
                try {
                    leftMostPathExpression.getExpressions().set(0, new PropertyExpression(alias));
                    for (int i = 0; i < size; i++) {
                        referenceExpression = referenceExpressions.get(i);
                        if (expressions.get(i).accept(this)) {
                            return Boolean.TRUE;
                        }
                    }
                    return Boolean.FALSE;
                } finally {
                    leftMostPathExpression.getExpressions().set(0, propertyExpression);
                }
            }
        }
        PathReference referencePathReference = reference.getPathReference();
        PathReference pathReference = expression.getPathReference();
        if (referencePathReference == null || pathReference == null) {
            return reference.equals(expression) ? Boolean.FALSE : Boolean.TRUE;
        }
        if (referencePathReference.getBaseNode() != pathReference.getBaseNode()) {
            return Boolean.TRUE;
        }
        return Objects.equals(referencePathReference.getField(), pathReference.getField()) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(ArrayExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        ArrayExpression reference = (ArrayExpression) referenceExpression;
        referenceExpression = reference.getBase();
        if (expression.getBase().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = reference.getIndex();
        return expression.getIndex().accept(this);
    }

    @Override
    public Boolean visit(TreatExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        TreatExpression reference = (TreatExpression) referenceExpression;
        if (!reference.getType().equals(expression.getType())) {
            return Boolean.TRUE;
        }
        referenceExpression = reference.getExpression();
        return expression.getExpression().accept(this);
    }

    @Override
    public Boolean visit(PropertyExpression expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(ParameterExpression expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(ListIndexExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        ListIndexExpression reference = (ListIndexExpression) referenceExpression;
        referenceExpression = reference.getPath();
        return expression.getPath().accept(this);
    }

    @Override
    public Boolean visit(MapEntryExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        MapEntryExpression reference = (MapEntryExpression) referenceExpression;
        referenceExpression = reference.getPath();
        return expression.getPath().accept(this);
    }

    @Override
    public Boolean visit(MapKeyExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        MapKeyExpression reference = (MapKeyExpression) referenceExpression;
        referenceExpression = reference.getPath();
        return expression.getPath().accept(this);
    }

    @Override
    public Boolean visit(MapValueExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        MapValueExpression reference = (MapValueExpression) referenceExpression;
        referenceExpression = reference.getPath();
        return expression.getPath().accept(this);
    }

    @Override
    public Boolean visit(NullExpression expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        FunctionExpression reference = (FunctionExpression) referenceExpression;
        if (!reference.getFunctionName().equals(expression.getFunctionName())) {
            return Boolean.TRUE;
        }
        List<Expression> referenceExpressions = reference.getExpressions();
        List<Expression> expressions = expression.getExpressions();
        int size = expressions.size();
        if (referenceExpressions.size() != size) {
            return Boolean.TRUE;
        }
        for (int i = 0; i < size; i++) {
            referenceExpression = referenceExpressions.get(i);
            if (expressions.get(i).accept(this)) {
                return Boolean.TRUE;
            }
        }
        WindowDefinition referenceWindowDefinition = reference.getWindowDefinition();
        WindowDefinition windowDefinition = expression.getWindowDefinition();
        if (windowDefinition == null) {
            return referenceWindowDefinition == null ? Boolean.FALSE : Boolean.TRUE;
        } else {
            if (referenceWindowDefinition == null) {
                return Boolean.TRUE;
            }
            Predicate filterPredicate = windowDefinition.getFilterPredicate();
            if (filterPredicate == null) {
                return referenceWindowDefinition.getFilterPredicate() == null ? Boolean.FALSE : Boolean.TRUE;
            } else {
                referenceExpression = referenceWindowDefinition.getFilterPredicate();
                if (filterPredicate.accept(this)) {
                    return Boolean.TRUE;
                }
            }

            List<Expression> referencePartitionExpressions = referenceWindowDefinition.getPartitionExpressions();
            List<Expression> partitionExpressions = windowDefinition.getPartitionExpressions();
            size = partitionExpressions.size();
            if (referencePartitionExpressions.size() != size) {
                return Boolean.TRUE;
            }
            for (int i = 0; i < size; i++) {
                referenceExpression = referencePartitionExpressions.get(i);
                if (partitionExpressions.get(i).accept(this)) {
                    return Boolean.TRUE;
                }
            }

            List<OrderByItem> referenceOrderByExpressions = referenceWindowDefinition.getOrderByExpressions();
            List<OrderByItem> orderByExpressions = windowDefinition.getOrderByExpressions();
            size = orderByExpressions.size();
            if (referenceOrderByExpressions.size() != size) {
                return Boolean.TRUE;
            }
            for (int i = 0; i < size; i++) {
                OrderByItem referenceOrderByItem = referenceOrderByExpressions.get(i);
                OrderByItem orderByItem = orderByExpressions.get(i);
                if (referenceOrderByItem.isAscending() != orderByItem.isAscending() || referenceOrderByItem.isNullFirst() != orderByItem.isNullFirst()) {
                    return Boolean.TRUE;
                }
                referenceExpression = referenceOrderByItem.getExpression();
                if (orderByItem.getExpression().accept(this)) {
                    return Boolean.TRUE;
                }
            }

            if (referenceWindowDefinition.getFrameMode() != windowDefinition.getFrameMode()
                    || referenceWindowDefinition.getFrameStartType() != windowDefinition.getFrameStartType()
                    || referenceWindowDefinition.getFrameEndType() != windowDefinition.getFrameEndType()
                    || referenceWindowDefinition.getFrameExclusionType() != windowDefinition.getFrameExclusionType()) {
                return Boolean.TRUE;
            }

            Expression frameStartExpression = windowDefinition.getFrameStartExpression();
            if (frameStartExpression == null) {
                return referenceWindowDefinition.getFrameStartExpression() == null ? Boolean.FALSE : Boolean.TRUE;
            } else {
                referenceExpression = referenceWindowDefinition.getFrameStartExpression();
                if (frameStartExpression.accept(this)) {
                    return Boolean.TRUE;
                }
            }

            Expression frameEndExpression = windowDefinition.getFrameEndExpression();
            if (frameEndExpression == null) {
                return referenceWindowDefinition.getFrameEndExpression() == null ? Boolean.FALSE : Boolean.TRUE;
            } else {
                referenceExpression = referenceWindowDefinition.getFrameEndExpression();
                if (frameEndExpression.accept(this)) {
                    return Boolean.TRUE;
                }
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
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        TrimExpression reference = (TrimExpression) referenceExpression;
        if (reference.getTrimspec() != expression.getTrimspec()) {
            return Boolean.TRUE;
        }
        if (expression.getTrimCharacter() == null) {
            return reference.getTrimCharacter() == null ? Boolean.FALSE : Boolean.TRUE;
        } else {
            referenceExpression = reference.getTrimCharacter();
            if (referenceExpression == null) {
                return Boolean.TRUE;
            }
            if (expression.getTrimCharacter().accept(this)) {
                return Boolean.TRUE;
            }
        }
        referenceExpression = reference.getTrimSource();
        return expression.getTrimSource().accept(this);
    }

    @Override
    public Boolean visit(GeneralCaseExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        GeneralCaseExpression reference = (GeneralCaseExpression) referenceExpression;
        List<WhenClauseExpression> referenceWhenClauses = reference.getWhenClauses();
        List<WhenClauseExpression> expressions = expression.getWhenClauses();
        int size = expressions.size();
        if (referenceWhenClauses.size() != size) {
            return Boolean.TRUE;
        }
        for (int i = 0; i < size; i++) {
            referenceExpression = referenceWhenClauses.get(i);
            if (expressions.get(i).accept(this)) {
                return Boolean.TRUE;
            }
        }

        if (expression.getDefaultExpr() == null) {
            return reference.getDefaultExpr() == null ? Boolean.FALSE : Boolean.TRUE;
        } else {
            referenceExpression = reference.getDefaultExpr();
            if (referenceExpression == null) {
                return Boolean.TRUE;
            }
            return expression.getDefaultExpr().accept(this);
        }
    }

    @Override
    public Boolean visit(SimpleCaseExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        SimpleCaseExpression reference = (SimpleCaseExpression) referenceExpression;
        referenceExpression = reference.getCaseOperand();
        if (expression.getCaseOperand().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = reference;
        return visit((GeneralCaseExpression) expression);
    }

    @Override
    public Boolean visit(WhenClauseExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        WhenClauseExpression reference = (WhenClauseExpression) referenceExpression;
        referenceExpression = reference.getCondition();
        if (expression.getCondition().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = reference.getResult();
        return expression.getResult().accept(this);
    }

    @Override
    public Boolean visit(ArithmeticExpression expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        ArithmeticExpression reference = (ArithmeticExpression) referenceExpression;
        if (reference.getOp() != expression.getOp()) {
            return Boolean.TRUE;
        }
        referenceExpression = reference.getLeft();
        if (expression.getLeft().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = reference.getRight();
        if (expression.getRight().accept(this)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean visit(ArithmeticFactor expression) {
        if (referenceExpression.getClass() != expression.getClass()) {
            return Boolean.TRUE;
        }
        ArithmeticFactor reference = (ArithmeticFactor) referenceExpression;
        if (reference.isInvertSignum() != expression.isInvertSignum()) {
            return Boolean.TRUE;
        }
        referenceExpression = reference.getExpression();
        return expression.getExpression().accept(this);
    }

    @Override
    public Boolean visit(NumericLiteral expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(BooleanLiteral expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(StringLiteral expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(DateLiteral expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(TimeLiteral expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(TimestampLiteral expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(EnumLiteral expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(EntityLiteral expression) {
        return expression.equals(referenceExpression) ? Boolean.FALSE : Boolean.TRUE;
    }

    @Override
    public Boolean visit(CompoundPredicate predicate) {
        if (referenceExpression.getClass() != predicate.getClass()) {
            return Boolean.TRUE;
        }
        CompoundPredicate referencePredicate = (CompoundPredicate) referenceExpression;
        if (referencePredicate.isNegated() != predicate.isNegated() || referencePredicate.getOperator() != predicate.getOperator()) {
            return Boolean.TRUE;
        }
        List<Predicate> referencePredicateChildren = referencePredicate.getChildren();
        List<Predicate> children = predicate.getChildren();
        if (referencePredicateChildren.size() != children.size()) {
            return Boolean.TRUE;
        }
        int size = children.size();
        for (int i = 0; i < size; i++) {
            referenceExpression = referencePredicateChildren.get(i);
            if (children.get(i).accept(this)) {
                return Boolean.TRUE;
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
        if (referenceExpression.getClass() != predicate.getClass()) {
            return Boolean.TRUE;
        }
        IsNullPredicate referencePredicate = (IsNullPredicate) referenceExpression;
        if (referencePredicate.isNegated() != predicate.isNegated()) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getExpression();
        return predicate.getExpression().accept(this);
    }

    @Override
    public Boolean visit(IsEmptyPredicate predicate) {
        if (referenceExpression.getClass() != predicate.getClass()) {
            return Boolean.TRUE;
        }
        IsEmptyPredicate referencePredicate = (IsEmptyPredicate) referenceExpression;
        if (referencePredicate.isNegated() != predicate.isNegated()) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getExpression();
        return predicate.getExpression().accept(this);
    }

    @Override
    public Boolean visit(MemberOfPredicate predicate) {
        return visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public Boolean visit(LikePredicate predicate) {
        if (referenceExpression.getClass() != predicate.getClass()) {
            return Boolean.TRUE;
        }
        LikePredicate referencePredicate = (LikePredicate) referenceExpression;
        if (referencePredicate.isNegated() != predicate.isNegated()) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getLeft();
        if (predicate.getLeft().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getRight();
        if (predicate.getRight().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getEscapeCharacter();
        if (referenceExpression == null && predicate.getEscapeCharacter() == null) {
            return Boolean.TRUE;
        }
        return referenceExpression != null && predicate.getEscapeCharacter() != null && predicate.getEscapeCharacter().accept(this);
    }

    @Override
    public Boolean visit(BetweenPredicate predicate) {
        if (referenceExpression.getClass() != predicate.getClass()) {
            return Boolean.TRUE;
        }
        BetweenPredicate referencePredicate = (BetweenPredicate) referenceExpression;
        if (referencePredicate.isNegated() != predicate.isNegated()) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getLeft();
        if (predicate.getLeft().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getStart();
        if (predicate.getStart().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getEnd();
        return predicate.getEnd().accept(this);
    }

    @Override
    public Boolean visit(InPredicate predicate) {
        if (referenceExpression.getClass() != predicate.getClass()) {
            return Boolean.TRUE;
        }
        InPredicate referencePredicate = (InPredicate) referenceExpression;
        if (referencePredicate.isNegated() != predicate.isNegated()) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getLeft();
        if (predicate.getLeft().accept(this)) {
            return Boolean.TRUE;
        }
        List<Expression> referencePredicateRight = referencePredicate.getRight();
        List<Expression> predicateRight = predicate.getRight();
        if (referencePredicateRight.size() != predicateRight.size()) {
            return Boolean.TRUE;
        }
        for (int i = 0; i < predicateRight.size(); i++) {
            Expression right = predicateRight.get(i);
            referenceExpression = referencePredicateRight.get(i);
            if (right.accept(this)) {
                return Boolean.TRUE;
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

    protected Boolean visit(BinaryExpressionPredicate predicate) {
        if (referenceExpression.getClass() != predicate.getClass()) {
            return Boolean.TRUE;
        }
        BinaryExpressionPredicate referencePredicate = (BinaryExpressionPredicate) referenceExpression;
        if (referencePredicate.isNegated() != predicate.isNegated()) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getLeft();
        if (predicate.getLeft().accept(this)) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getRight();
        return predicate.getRight().accept(this);
    }

    @Override
    public Boolean visit(ExistsPredicate predicate) {
        if (referenceExpression.getClass() != predicate.getClass()) {
            return Boolean.TRUE;
        }
        ExistsPredicate referencePredicate = (ExistsPredicate) referenceExpression;
        if (referencePredicate.isNegated() != predicate.isNegated()) {
            return Boolean.TRUE;
        }
        referenceExpression = referencePredicate.getExpression();
        return predicate.getExpression().accept(this);
    }

}
