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
package com.blazebit.persistence.view.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.CompositeExpression;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FooExpression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.LiteralExpression;
import com.blazebit.persistence.impl.expression.NullExpression;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.QuantifierExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.InPredicate;
import com.blazebit.persistence.impl.predicate.IsEmptyPredicate;
import com.blazebit.persistence.impl.predicate.IsNullPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.MemberOfPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.OrPredicate;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class UpdatableExpressionVisitor implements Expression.Visitor {

    private PathPosition currentPosition;
    private List<PathPosition> pathPositions;

    private static class PathPosition {

        private Class<?> currentClass;
        private Class<?> valueClass;
        private Method method;

        PathPosition(Class<?> currentClass, Method method) {
            this.currentClass = currentClass;
            this.method = method;
        }

		Class<?> getValueClass() {
			return valueClass;
		}

		Class<?> getCurrentClass() {
			return currentClass;
		}

		void setCurrentClass(Class<?> currentClass) {
			this.currentClass = currentClass;
			this.valueClass = null;
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
    }

    public UpdatableExpressionVisitor(Class<?> startClass) {
        this.pathPositions = new ArrayList<PathPosition>();
        this.pathPositions.add(currentPosition = new PathPosition(startClass, null));
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
            possibleTargets.put(position.getMethod(), new Class[]{ position.getCurrentClass(), position.getValueClass() });
        }
        
        return possibleTargets;
    }
    
    @Override
    public void visit(PropertyExpression expression) {
        if (currentPosition.getValueClass() != null) {
            throw new IllegalArgumentException("Invalid dereferencing of collection property '" + expression.getProperty() + "' in updatable expression!");
        }
        
        currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), expression.getProperty()));
        if (currentPosition.getMethod() == null) {
            currentPosition.setCurrentClass(null);
        } else {
            Class<?> type = getType(currentPosition.getCurrentClass(), currentPosition.getMethod());
            Class<?> valueType = null;
            
            if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getCurrentClass(), currentPosition.getMethod());
                valueType = typeArguments[typeArguments.length - 1];
            } else {
            	valueType = type;
            }
            
            currentPosition.setCurrentClass(type);
            currentPosition.setValueClass(valueType);
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
    public void visit(ArrayExpression expression) {
        // Only need the base to navigate down the path
//        expression.getBase().accept(this);
        // TODO: We should somehow support this
        invalid(expression);
    }

    @Override
    public void visit(ParameterExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(CompositeExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(LiteralExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(NullExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(FooExpression expression) {
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
    public void visit(QuantifierExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(AndPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(OrPredicate predicate) {
        invalid(predicate);
    }

    @Override
    public void visit(NotPredicate predicate) {
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
