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
import java.util.*;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import com.blazebit.persistence.impl.expression.*;
import com.blazebit.persistence.impl.predicate.BooleanLiteral;
import com.blazebit.reflection.ReflectionUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class MetamodelTargetResolvingExpressionVisitor extends VisitorAdapter {

    private final ManagedType<?> managedType;
    private final Metamodel metamodel;
    private boolean parametersAllowed;
    private PathPosition currentPosition;
    private List<PathPosition> pathPositions;

    private static class PathPosition {

        private Class<?> currentClass;
        private Class<?> valueClass;
        private Method method;
        private boolean hasCollectionJoin;

        PathPosition(ManagedType<?> managedType, Method method) {
            this.currentClass = managedType.getJavaType();
            this.method = method;
        }
        
		private PathPosition(Class<?> currentClass, Class<?> valueClass, Method method, boolean hasCollectionJoin) {
            this.currentClass = currentClass;
            this.valueClass = valueClass;
            this.method = method;
            this.hasCollectionJoin = hasCollectionJoin;
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

		public boolean hasCollectionJoin() {
			return hasCollectionJoin;
		}

		void setValueClass(Class<?> valueClass) {
			this.valueClass = valueClass;
			
			if (valueClass != null && valueClass != currentClass) {
				hasCollectionJoin = true;
			}
		}
		
        PathPosition copy() {
            return new PathPosition(currentClass, valueClass, method,hasCollectionJoin);
        }
    }

    public MetamodelTargetResolvingExpressionVisitor(ManagedType<?> managedType, Metamodel metamodel) {
        this.managedType = managedType;
        this.metamodel = metamodel;
        this.parametersAllowed = false;
        this.pathPositions = new ArrayList<PathPosition>();
        this.pathPositions.add(currentPosition = new PathPosition(managedType, null));
    }

    private Method resolve(Class<?> currentClass, String property) {
    	Attribute<?, ?> attribute = null;
    	
    	try {
    		attribute = metamodel.managedType(currentClass).getAttribute(property);
    	} catch (IllegalArgumentException ex) {
    		attribute = null;
    	}
    	
        if (attribute == null) {
            throw new IllegalArgumentException("The property '" + property + "' could not be found on the type '" + currentClass.getName() + "'!");
        }
        
        return ReflectionUtils.getGetter(currentClass, property);
    }

    private Class<?> getType(Class<?> baseClass, Method element) {
        return ReflectionUtils.getResolvedMethodReturnType(baseClass, element);
    }
    
    public static interface TargetType {
    	
    	public boolean hasCollectionJoin();
    	
    	public Method getLeafMethod();
    	
    	public Class<?> getLeafBaseClass();
    	
    	public Class<?> getLeafBaseValueClass();
    	
    }
    
    private static class TargetTypeImpl implements TargetType {
    	
    	private final boolean hasCollectionJoin;
    	private final Method leafMethod;
    	private final Class<?> leafBaseClass;
    	private final Class<?> leafBaseValueClass;

    	public TargetTypeImpl(boolean hasCollectionJoin, Method leafMethod, Class<?> leafBaseClass, Class<?> leafBaseValueClass) {
			this.hasCollectionJoin = hasCollectionJoin;
			this.leafMethod = leafMethod;
			this.leafBaseClass = leafBaseClass;
			this.leafBaseValueClass = leafBaseValueClass;
		}

    	@Override
		public boolean hasCollectionJoin() {
			return hasCollectionJoin;
		}

    	@Override
		public Method getLeafMethod() {
			return leafMethod;
		}

    	@Override
		public Class<?> getLeafBaseClass() {
			return leafBaseClass;
		}

    	@Override
		public Class<?> getLeafBaseValueClass() {
			return leafBaseValueClass;
		}
    }

    public List<TargetType> getPossibleTargets() {
        List<PathPosition> positions = pathPositions;
        int size = positions.size();
        
        if (size == 1 && positions.get(0).getMethod() == null && managedType.getJavaType().equals(positions.get(0).getRealCurrentClass())) {
            // When we didn't resolve any property, the expression is probably static and we can't give types in that case
            return Collections.emptyList();
        }
        
        List<TargetType> possibleTargets = new ArrayList<TargetType>(size);
        for (int i = 0; i < size; i++) {
            PathPosition position = positions.get(i);
            possibleTargets.add(new TargetTypeImpl(position.hasCollectionJoin(), position.getMethod(), getBoxed(position.getRealCurrentClass()), getBoxed(position.getCurrentClass())));
        }
        
        return possibleTargets;
    }
    
    private Class<?> getBoxed(Class<?> clazz) {
        return ReflectionUtils.getObjectClassOfPrimitve(clazz);
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
        boolean wasParamsAllowed = parametersAllowed;
        List<PathPosition> currentPositions = pathPositions;
        PathPosition position = currentPosition;

        parametersAllowed = true;
        pathPositions = new ArrayList<PathPosition>();
        pathPositions.add(currentPosition = new PathPosition(managedType, null));
        
        // Validate index against metamodel
        expression.getIndex().accept(this);
        
        parametersAllowed = wasParamsAllowed;
		currentPosition = position;
		pathPositions = currentPositions;
    
        // Only need the base to navigate down the path
        expression.getBase().accept(this);
    }

    @Override
    public void visit(ParameterExpression expression) {
        // We can't infer a type here
        if (parametersAllowed) {
            // NOTE: We use null as marker for ANY TYPE
            currentPosition.setCurrentClass(null);
        } else {
            // NOTE: parameters are only supported in the select clause when having an insert!
            invalid(expression, "Parameters are not allowed as results in mapping. Please use @MappingParameter for this instead!");
        }
    }

    @Override
    public void visit(CompositeExpression expression) {
        resolveToAny(expression.getExpressions(), false);
    }
    
    private void resolveToAny(List<Expression> expressions, boolean allowParams) {
        List<PathPosition> currentPositions = pathPositions;
        List<PathPosition> newPositions = new ArrayList<PathPosition>();
        
        int positionsSize = currentPositions.size();
        for (int j = 0; j < positionsSize; j++) {
            int size = expressions.size();
            for (int i = 0; i < size; i++) {
                PathPosition position = currentPositions.get(j).copy();
                pathPositions = new ArrayList<PathPosition>();
                pathPositions.add(currentPosition = position);
                if (allowParams) {
                    parametersAllowed = true;
                }
                expressions.get(i).accept(this);
                if (allowParams) {
                    parametersAllowed = false;
                }
                newPositions.addAll(pathPositions);
            }
        }
        
        currentPosition = null;
        pathPositions = newPositions;
    }

    @Override
    public void visit(NullExpression expression) {
        // We can't infer a type here
        // TODO: Not sure what happens when this is the result node of a case when
    }

    @Override
    public void visit(FooExpression expression) {
        // We can't infer a type here
        // TODO: Not sure what happens when this is the result node of a case when
        super.visit(expression);
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentClass(expression.getNumericType().getJavaType());
        }
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentClass(expression.getNumericType().getJavaType());
        }
    }

    @Override
    public void visit(NumericLiteral expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentClass(expression.getNumericType().getJavaType());
        }
    }

    @Override
    public void visit(BooleanLiteral expression) {
        currentPosition.setCurrentClass(Boolean.class);
    }

    @Override
    public void visit(StringLiteral expression) {
        currentPosition.setCurrentClass(String.class);
    }

    @Override
    public void visit(DateLiteral expression) {
        currentPosition.setCurrentClass(Date.class);
    }

    @Override
    public void visit(TimeLiteral expression) {
        currentPosition.setCurrentClass(Date.class);
    }

    @Override
    public void visit(TimestampLiteral expression) {
        currentPosition.setCurrentClass(Date.class);
    }

    private boolean isNumber(String expressionString) {
    	String s = expressionString.trim();
    	
    	if (s.isEmpty()) {
    		return false;
    	}
    	
    	for (int i = 0; i < expressionString.length(); i++) {
    		char c = expressionString.charAt(i);
    		if (!Character.isDigit(c)) {
    			return false;
    		}
    	}
    	
    	return true;
	}

	@Override
    public void visit(SubqueryExpression expression) {
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
        } else if ("FUNCTION".equalsIgnoreCase(name)) {
            // Skip the function name
            resolveToAny(expression.getExpressions().subList(1, expression.getExpressions().size()), true);
        } else {
            // TODO: we could do better here by checking the actual return types of the functions or put a list of other "known" functions here, at least make it extendible
    	    // We can't just say it's invalid, we might just not know the function
//    		invalid(expression);
            resolveToAny(expression.getExpressions(), true);
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

    private void invalid(Object o) {
        throw new IllegalArgumentException("Illegal occurence of [" + o + "] in path chain resolver!");
    }

    private void invalid(Object o, String reason) {
        throw new IllegalArgumentException("Illegal occurence of [" + o + "] in path chain resolver! " + reason);
    }

}
