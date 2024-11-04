/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.PathTargetResolvingExpressionVisitor;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.DateLiteral;
import com.blazebit.persistence.parser.expression.EntityLiteral;
import com.blazebit.persistence.parser.expression.EnumLiteral;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.NumericLiteral;
import com.blazebit.persistence.parser.expression.ParameterExpression;
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
import com.blazebit.persistence.parser.predicate.BetweenPredicate;
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

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class UpdatableExpressionVisitor extends PathTargetResolvingExpressionVisitor {

    private final boolean updatable;

    public UpdatableExpressionVisitor(EntityMetamodel metamodel, Class<?> startClass, boolean updatable, Map<String, Type<?>> rootTypes) {
        super(metamodel, metamodel.type(startClass), null, null, rootTypes);
        this.updatable = updatable;
    }

    public Map<Attribute<?, ?>, Type<?>> getPossibleTargets() {
        Map<Attribute<?, ?>, Type<?>> possibleTargets = new HashMap<>();

        List<PathPosition> positions = pathPositions;
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            PathPosition position = positions.get(i);
            possibleTargets.put(position.getAttribute(), position.getRealCurrentType());
        }

        return possibleTargets;
    }
    
    @Override
    public void visit(PropertyExpression expression) {
        if (updatable && (currentPosition.attribute != null)) {
            throw new IllegalArgumentException("Invalid dereferencing of collection property '" + expression.getProperty() + "' in updatable expression!");
        }

        super.visit(expression);
    }

    @Override
    public void visit(ListIndexExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(MapEntryExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(MapKeyExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(MapValueExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(ArrayExpression expression) {
        // Only need the base to navigate down the path
//        expression.getBase().accept(this);
        // TODO: We should somehow support this
        invalid(expression);
    }

    @Override
    public void visit(TreatExpression expression) {
        // NOTE: JPQL does not support treat in the SET clause
        invalid(expression);
    }

    @Override
    public void visit(ParameterExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(NullExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(SubqueryExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(FunctionExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(TypeFunctionExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(TrimExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(WhenClauseExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        invalid(expression);
    }

    @Override
    public void visit(NumericLiteral expression) {
        invalid(expression);
    }

    @Override
    public void visit(BooleanLiteral expression) {
        invalid(expression);
    }

    @Override
    public void visit(StringLiteral expression) {
        invalid(expression);
    }

    @Override
    public void visit(DateLiteral expression) {
        invalid(expression);
    }

    @Override
    public void visit(TimeLiteral expression) {
        invalid(expression);
    }

    @Override
    public void visit(TimestampLiteral expression) {
        invalid(expression);
    }

    @Override
    public void visit(EnumLiteral expression) {
        invalid(expression);
    }

    @Override
    public void visit(EntityLiteral expression) {
        invalid(expression);
    }

    @Override
    public void visit(CompoundPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(EqPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(LikePredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(InPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(GtPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(GePredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(LtPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(LePredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(ExistsPredicate predicate) {
        invalid(predicate);
    }

}
