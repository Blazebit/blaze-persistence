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

package com.blazebit.persistence.view.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
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
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class UpdatableExpressionVisitor implements Expression.Visitor {

    private PathPosition currentPosition;
    private List<PathPosition> pathPositions;

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class PathPosition {

        private Class<?> currentClass;
        private Class<?> valueClass;
        private Class<?> keyClass;
        private Method method;

        PathPosition(Class<?> currentClass, Method method) {
            this.currentClass = currentClass;
            this.method = method;
        }

        Class<?> getValueClass() {
            return valueClass;
        }

        public Class<?> getKeyClass() {
            return keyClass;
        }

        Class<?> getRealCurrentClass() {
            return currentClass;
        }

        Class<?> getCurrentClass() {
            if (valueClass != null) {
                return valueClass;
            }
            if (keyClass != null) {
                return keyClass;
            }

            return currentClass;
        }

        void setCurrentClass(Class<?> currentClass) {
            this.currentClass = currentClass;
            this.valueClass = null;
            this.keyClass = null;
        }

        Method getMethod() {
            return method;
        }

        void setMethod(Method method) {
            this.method = method;
        }

        void setValueClass(Class<?> valueClass) {
            this.valueClass = valueClass;
        }

        public void setKeyClass(Class<?> keyClass) {
            this.keyClass = keyClass;
        }
    }

    public UpdatableExpressionVisitor(Class<?> startClass) {
        this.pathPositions = new ArrayList<PathPosition>();
        this.currentPosition = new PathPosition(startClass, null);
        this.pathPositions.add(currentPosition);
    }

    private Method resolve(Class<?> currentClass, String property) {
        return ReflectionUtils.getGetter(currentClass, property);
    }

    private Class<?> getType(Class<?> baseClass, Method element) {
        return ReflectionUtils.getResolvedMethodReturnType(baseClass, element);
    }

    public Map<Method, Class<?>[]> getPossibleTargets() {
        Map<Method, Class<?>[]> possibleTargets = new HashMap<Method, Class<?>[]>();

        List<PathPosition> positions = pathPositions;
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            PathPosition position = positions.get(i);
            possibleTargets.put(position.getMethod(), new Class[]{ position.getRealCurrentClass(), position.getCurrentClass() });
        }
        
        return possibleTargets;
    }
    
    @Override
    public void visit(PropertyExpression expression) {
        if (currentPosition.getValueClass() != null || currentPosition.getKeyClass() != null) {
            throw new IllegalArgumentException("Invalid dereferencing of collection property '" + expression.getProperty() + "' in updatable expression!");
        }
        
        currentPosition.setMethod(resolve(currentPosition.getRealCurrentClass(), expression.getProperty()));
        if (currentPosition.getMethod() == null) {
            currentPosition.setCurrentClass(null);
        } else {
            Class<?> type = getType(currentPosition.getRealCurrentClass(), currentPosition.getMethod());
            Class<?> valueType = null;
            Class<?> keyType = null;

            if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getRealCurrentClass(), currentPosition.getMethod());
                valueType = typeArguments[typeArguments.length - 1];
                if (typeArguments.length > 1) {
                    keyType = typeArguments[0];
                }
            } else {
                valueType = type;
            }

            currentPosition.setCurrentClass(type);
            currentPosition.setValueClass(valueType);
            currentPosition.setKeyClass(keyType);
        }
    }

    @Override
    public void visit(PathExpression expression) {
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();
        for (int i = 0; i < size; i++) {
            expressions.get(i).accept(this);
        }
    }

    @Override
    public void visit(ListIndexExpression expression) {
        // NOTE: JPQL does not support treat in the SET clause
        invalid(expression);
    }

    @Override
    public void visit(MapEntryExpression expression) {
        // NOTE: JPQL does not support treat in the SET clause
        invalid(expression);
    }

    @Override
    public void visit(MapKeyExpression expression) {
        // NOTE: JPQL does not support treat in the SET clause
        invalid(expression);
    }

    @Override
    public void visit(MapValueExpression expression) {
        // NOTE: JPQL does not support treat in the SET clause
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

    private void invalid(Object o) {
        throw new IllegalArgumentException("Illegal occurence of [" + o + "] in path chain resolver!");
    }

}
