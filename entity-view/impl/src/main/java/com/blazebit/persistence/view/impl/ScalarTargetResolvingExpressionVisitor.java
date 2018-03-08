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

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.DateLiteral;
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
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.util.ExpressionUtils;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A visitor that can determine possible target types of a scalar expressions.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ScalarTargetResolvingExpressionVisitor extends VisitorAdapter {

    private final ManagedType<?> managedType;
    private final EntityMetamodel metamodel;
    private final Map<String, JpqlFunction> functions;
    private boolean parametersAllowed;
    private PathPosition currentPosition;
    private List<PathPosition> pathPositions;

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class PathPosition {

        private Class<?> currentClass;
        private Class<?> keyClass;
        private Class<?> valueClass;
        private Method method;
        private boolean hasCollectionJoin;

        PathPosition(ManagedType<?> managedType, Method method) {
            this.currentClass = managedType.getJavaType();
            this.method = method;
        }
        
        private PathPosition(Class<?> currentClass, Class<?> keyClass, Class<?> valueClass, Method method, boolean hasCollectionJoin) {
            this.currentClass = currentClass;
            this.keyClass = keyClass;
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
            this.keyClass = null;
            this.valueClass = null;
            this.hasCollectionJoin = false;
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

        void setKeyClass(Class<?> keyClass) {
            this.keyClass = keyClass;
        }

        Class<?> getKeyClass() {
            return keyClass;
        }

        void setValueClass(Class<?> valueClass) {
            this.valueClass = valueClass;
            
            if (valueClass != null && valueClass != currentClass) {
                hasCollectionJoin = true;
            }
        }

        Class<?> getValueClass() {
            return valueClass;
        }

        PathPosition copy() {
            return new PathPosition(currentClass, keyClass, valueClass, method, hasCollectionJoin);
        }
    }

    public ScalarTargetResolvingExpressionVisitor(Class<?> managedTypeClass, EntityMetamodel metamodel, Map<String, JpqlFunction> functions) {
        this(metamodel.getManagedType(managedTypeClass), metamodel, functions);
    }

    public ScalarTargetResolvingExpressionVisitor(ManagedType<?> managedType, EntityMetamodel metamodel, Map<String, JpqlFunction> functions) {
        this.managedType = managedType;
        this.metamodel = metamodel;
        this.functions = functions;
        this.parametersAllowed = false;
        this.pathPositions = new ArrayList<PathPosition>();
        this.pathPositions.add(currentPosition = new PathPosition(managedType, null));
    }

    public void clear() {
        this.pathPositions.clear();
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

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    public static interface TargetType {
        
        public boolean hasCollectionJoin();
        
        public Method getLeafMethod();
        
        public Class<?> getLeafBaseClass();

        public Class<?> getLeafBaseKeyClass();
        
        public Class<?> getLeafBaseValueClass();
        
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    public static class TargetTypeImpl implements TargetType {
        
        private final boolean hasCollectionJoin;
        private final Method leafMethod;
        private final Class<?> leafBaseClass;
        private final Class<?> leafBaseKeyClass;
        private final Class<?> leafBaseValueClass;

        public TargetTypeImpl(boolean hasCollectionJoin, Method leafMethod, Class<?> leafBaseClass, Class<?> leafBaseKeyClass, Class<?> leafBaseValueClass) {
            this.hasCollectionJoin = hasCollectionJoin;
            this.leafMethod = leafMethod;
            this.leafBaseClass = leafBaseClass;
            this.leafBaseKeyClass = leafBaseKeyClass;
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

        public Class<?> getLeafBaseKeyClass() {
            return leafBaseKeyClass;
        }

        @Override
        public Class<?> getLeafBaseValueClass() {
            return leafBaseValueClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TargetTypeImpl that = (TargetTypeImpl) o;

            if (leafBaseClass != null ? !leafBaseClass.equals(that.leafBaseClass) : that.leafBaseClass != null) {
                return false;
            }
            return leafBaseValueClass != null ? leafBaseValueClass.equals(that.leafBaseValueClass) : that.leafBaseValueClass == null;
        }

        @Override
        public int hashCode() {
            int result = leafBaseClass != null ? leafBaseClass.hashCode() : 0;
            result = 31 * result + (leafBaseValueClass != null ? leafBaseValueClass.hashCode() : 0);
            return result;
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
            possibleTargets.add(new TargetTypeImpl(position.hasCollectionJoin(), position.getMethod(), position.getRealCurrentClass(), position.getCurrentClass(), position.getCurrentClass()));
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
            Class<?> keyType = null;
            Class<?> valueType = null;
            
            if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getCurrentClass(), currentPosition.getMethod());
                valueType = typeArguments[typeArguments.length - 1];
                if (typeArguments.length > 1) {
                    keyType = typeArguments[0];
                }
            } else {
                valueType = type;
            }
            
            currentPosition.setCurrentClass(type);
            currentPosition.setKeyClass(keyType);
            currentPosition.setValueClass(valueType);
        }
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        List<PathPosition> currentPositions = pathPositions;
        List<PathPosition> newPositions = new ArrayList<>();
        
        int positionsSize = currentPositions.size();
        for (int j = 0; j < positionsSize; j++) {
            List<WhenClauseExpression> expressions = expression.getWhenClauses();
            int size = expressions.size();
            EXPRESSION_LOOP: for (int i = 0; i < size; i++) {
                PathPosition position = currentPositions.get(j).copy();
                pathPositions = new ArrayList<>();
                pathPositions.add(currentPosition = position);
                expressions.get(i).accept(this);

                // We just use the type of the first path position that we find
                for (PathPosition newPosition : pathPositions) {
                    if (newPosition.getCurrentClass() != null) {
                        newPositions.add(newPosition);
                        break EXPRESSION_LOOP;
                    }
                }
            }

            if (newPositions.isEmpty() && expression.getDefaultExpr() != null) {
                PathPosition position = currentPositions.get(j).copy();
                pathPositions = new ArrayList<>();
                pathPositions.add(currentPosition = position);
                expression.getDefaultExpr().accept(this);

                // We just use the type of the first path position that we find
                for (PathPosition newPosition : pathPositions) {
                    if (newPosition.getCurrentClass() != null) {
                        newPositions.add(newPosition);
                    }
                }
            }
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
        currentPosition.setCurrentClass(currentPosition.getValueClass());
    }

    @Override
    public void visit(TreatExpression expression) {
        EntityType<?> type = metamodel.getEntity(expression.getType());
        currentPosition.setMethod(null);
        currentPosition.setCurrentClass(type.getJavaType());
        currentPosition.setValueClass(type.getJavaType());
    }

    @Override
    public void visit(ParameterExpression expression) {
        // We can't infer a type here
        if (parametersAllowed) {
            // NOTE: We use null as marker for ANY TYPE
            currentPosition.setCurrentClass(null);
        } else {
            // If there are other branches (path positions) i.e. of a case when that have a type, we can allow parameters too
            for (PathPosition position : pathPositions) {
                if (position != currentPosition) {
                    if (position.getCurrentClass() != null) {
                        currentPosition.setCurrentClass(null);
                        return;
                    }
                }
            }
            // NOTE: plain parameters are only supported in the select clause when having an insert!
            invalid(expression, "Parameters are not allowed as results in mapping. Please use @MappingParameter for this instead!");
        }
    }

    private void resolveFirst(List<Expression> expressions, boolean allowParams) {
        List<PathPosition> currentPositions = pathPositions;
        List<PathPosition> newPositions = new ArrayList<>();
        
        int positionsSize = currentPositions.size();
        for (int j = 0; j < positionsSize; j++) {
            int size = expressions.size();
            EXPRESSION_LOOP: for (int i = 0; i < size; i++) {
                PathPosition position = currentPositions.get(j).copy();
                pathPositions = new ArrayList<>();
                pathPositions.add(currentPosition = position);
                if (allowParams) {
                    parametersAllowed = true;
                }
                expressions.get(i).accept(this);
                if (allowParams) {
                    parametersAllowed = false;
                }

                // We just use the type of the first path position that we find
                for (PathPosition newPosition : pathPositions) {
                    if (newPosition.getCurrentClass() != null) {
                        newPositions.add(newPosition);
                        break EXPRESSION_LOOP;
                    }
                }
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

    @Override
    public void visit(SubqueryExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(ListIndexExpression expression) {
        PropertyExpression property = resolveBase(expression.getPath());
        currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), property.getProperty()));
        Class<?> type = ReflectionUtils.getResolvedMethodReturnType(currentPosition.getCurrentClass(), currentPosition.getMethod());

        if (!List.class.isAssignableFrom(type)) {
            invalid(expression, "Does not resolve to java.util.List!");
        } else {
            currentPosition.setCurrentClass(type);
            currentPosition.setValueClass(Integer.class);
        }
    }

    @Override
    public void visit(MapEntryExpression expression) {
        PropertyExpression property = resolveBase(expression.getPath());
        currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), property.getProperty()));
        Class<?> type = ReflectionUtils.getResolvedMethodReturnType(currentPosition.getCurrentClass(), currentPosition.getMethod());

        if (!Map.class.isAssignableFrom(type)) {
            invalid(expression, "Does not resolve to java.util.Map!");
        } else {
            currentPosition.setCurrentClass(Map.Entry.class);
        }
    }

    @Override
    public void visit(MapKeyExpression expression) {
        PropertyExpression property = resolveBase(expression.getPath());
        currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), property.getProperty()));
        Class<?> type = ReflectionUtils.getResolvedMethodReturnType(currentPosition.getCurrentClass(), currentPosition.getMethod());
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getCurrentClass(), currentPosition.getMethod());

        if (!Map.class.isAssignableFrom(type)) {
            invalid(expression, "Does not resolve to java.util.Map!");
        } else {
            currentPosition.setCurrentClass(type);
            currentPosition.setValueClass(typeArguments[0]);
        }
    }

    @Override
    public void visit(MapValueExpression expression) {
        PropertyExpression property = resolveBase(expression.getPath());
        currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), property.getProperty()));
        Class<?> type = ReflectionUtils.getResolvedMethodReturnType(currentPosition.getCurrentClass(), currentPosition.getMethod());
        Class<?>[] typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getCurrentClass(), currentPosition.getMethod());

        if (!Map.class.isAssignableFrom(type)) {
            invalid(expression, "Does not resolve to java.util.Map!");
        } else {
            currentPosition.setCurrentClass(type);
            currentPosition.setValueClass(typeArguments[1]);
        }
    }

    @Override
    public void visit(FunctionExpression expression) {
        String name = expression.getFunctionName();
        if ("FUNCTION".equalsIgnoreCase(name)) {
            // Skip the function name
            resolveFirst(expression.getExpressions().subList(1, expression.getExpressions().size()), true);
            resolveToFunctionReturnType(((StringLiteral) expression.getExpressions().get(0)).getValue());
        } else if (ExpressionUtils.isSizeFunction(expression)) {
            // According to our grammar, we can only get a path here
            PropertyExpression property = resolveBase((PathExpression) expression.getExpressions().get(0));
            currentPosition.setMethod(resolve(currentPosition.getCurrentClass(), property.getProperty()));
            currentPosition.setCurrentClass(Long.class);
        } else {
            resolveFirst(expression.getExpressions(), true);
            resolveToFunctionReturnType(name);
        }
    }

    private void resolveToFunctionReturnType(String functionName) {
        JpqlFunction function = functions.get(functionName.toLowerCase());
        if (function == null) {
            return;
        }

        List<PathPosition> currentPositions = pathPositions;
        int positionsSize = currentPositions.size();

        for (int i = 0; i < positionsSize; i++) {
            PathPosition position = currentPositions.get(i);
            Class<?> returnType = function.getReturnType(position.getCurrentClass());
            position.setCurrentClass(returnType);
        }
    }

    @Override
    public void visit(TrimExpression expression) {
        currentPosition.setCurrentClass(String.class);
    }

    private PropertyExpression resolveBase(PathExpression path) {
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
