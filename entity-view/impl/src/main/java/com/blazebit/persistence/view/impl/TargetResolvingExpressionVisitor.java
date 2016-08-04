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

import com.blazebit.persistence.impl.expression.*;
import com.blazebit.persistence.impl.predicate.*;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class TargetResolvingExpressionVisitor implements Expression.Visitor {

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
        
        private PathPosition(Class<?> currentClass, Class<?> valueClass, Method method) {
            this.currentClass = currentClass;
            this.valueClass = valueClass;
            this.method = method;
        }

		Class<?> getRealCurrentClass() {
			return currentClass;
		}

		Class<?> getCurrentClass() {
			if (valueClass != null) {
				return valueClass;
			}
			
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
        
        PathPosition copy() {
            return new PathPosition(currentClass, valueClass, method);
        }
    }

    public TargetResolvingExpressionVisitor(Class<?> startClass) {
        this.pathPositions = new ArrayList<PathPosition>();
        this.pathPositions.add(currentPosition = new PathPosition(startClass, null));
    }

    private Method resolve(Class<?> currentClass, String property) {
        return ReflectionUtils.getGetter(currentClass, property);
    }

    private Class<?> getType(Class<?> baseClass, Method element) {
        return ReflectionUtils.getResolvedMethodReturnType(baseClass, element);
    }

    public Map<Method, Class<?>> getPossibleTargets() {
        Map<Method, Class<?>> possibleTargets = new HashMap<Method, Class<?>>();

        List<PathPosition> positions = pathPositions;
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            PathPosition position = positions.get(i);
            possibleTargets.put(position.getMethod(), position.getRealCurrentClass());
        }
        
        return possibleTargets;
    }
    
    @Override
    public void visit(PropertyExpression expression) {
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
    public void visit(GeneralCaseExpression expression) {
        List<PathPosition> currentPositions = pathPositions;
        List<PathPosition> newPositions = new ArrayList<PathPosition>();
        
        int positionsSize = currentPositions.size();
        for (int j = 0; j < positionsSize; j++) {
            List<WhenClauseExpression> expressions = expression.getWhenClauses();
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                PathPosition position = currentPositions.get(j).copy();
                pathPositions = new ArrayList<PathPosition>();
                pathPositions.add(currentPosition = position);
                expressions.get(i).accept(this);
                newPositions.addAll(pathPositions);
            }

            PathPosition position = currentPositions.get(j).copy();
            pathPositions = new ArrayList<PathPosition>();
            pathPositions.add(currentPosition = position);
            expression.getDefaultExpr().accept(this);
            newPositions.addAll(pathPositions);
        }
        
        currentPosition = null;
        pathPositions = newPositions;
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
        expression.getBase().accept(this);
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
    public void visit(FunctionExpression expression) {
    	String name = expression.getFunctionName();
    	if ("KEY".equalsIgnoreCase(name)) {
    		PropertyExpression property = resolveBase(expression);
    		currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), property.getProperty()));
    		Class<?> type = ReflectionUtils.getResolvedMethodReturnType(currentPosition.getCurrentClass(), currentPosition.getMethod());
            Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getCurrentClass(), currentPosition.getMethod());

    		if (!Map.class.isAssignableFrom(type)) {
            	invalid(expression, "Does not resolve to java.util.Map!");
            } else {
            	currentPosition.setCurrentClass(type);
            	currentPosition.setValueClass(typeArguments[0]);
            }
    	} else if ("INDEX".equalsIgnoreCase(name)) {
    		PropertyExpression property = resolveBase(expression);
    		currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), property.getProperty()));
    		Class<?> type = ReflectionUtils.getResolvedMethodReturnType(currentPosition.getCurrentClass(), currentPosition.getMethod());
    		
    		if (!List.class.isAssignableFrom(type)) {
    			invalid(expression, "Does not resolve to java.util.List!");
    		} else {
            	currentPosition.setCurrentClass(type);
            	currentPosition.setValueClass(Integer.class);
    		}
    	} else if ("VALUE".equalsIgnoreCase(name)) {
    		PropertyExpression property = resolveBase(expression);
    		currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), property.getProperty()));
    		Class<?> type = ReflectionUtils.getResolvedMethodReturnType(currentPosition.getCurrentClass(), currentPosition.getMethod());
            Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getCurrentClass(), currentPosition.getMethod());

    		if (!Map.class.isAssignableFrom(type)) {
            	invalid(expression, "Does not resolve to java.util.Map!");
            } else {
            	currentPosition.setCurrentClass(type);
            	currentPosition.setValueClass(typeArguments[1]);
            }
    	} else {
    		invalid(expression);
    	}
    }
    
    private PropertyExpression resolveBase(FunctionExpression expression) {
		// According to our grammar, we can only get a path here
		PathExpression path = (PathExpression) expression.getExpressions().get(0);
		int lastIndex = path.getExpressions().size() - 1;
		
		for (int i = 0; i < lastIndex; i++) {
			path.getExpressions().get(i).accept(this);
		}
		
		// According to our grammar, the last element must be a property
		return (PropertyExpression) path.getExpressions().get(lastIndex);
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        visit((GeneralCaseExpression) expression);
    }

    @Override
    public void visit(WhenClauseExpression expression) {
        expression.getResult().accept(this);
    }
    
    @Override
    public void visit(CompoundPredicate predicate) {
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

    private void invalid(Object o, String reason) {
        throw new IllegalArgumentException("Illegal occurence of [" + o + "] in path chain resolver! " + reason);
    }

}
