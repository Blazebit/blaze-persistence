/*
 * Copyright 2014 - 2019 Blazebit.
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
import com.blazebit.persistence.parser.ListIndexAttribute;
import com.blazebit.persistence.parser.MapKeyAttribute;
import com.blazebit.persistence.parser.PathTargetResolvingExpressionVisitor;
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
import com.blazebit.persistence.parser.expression.SimpleCaseExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TimeLiteral;
import com.blazebit.persistence.parser.expression.TimestampLiteral;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.TrimExpression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.util.ExpressionUtils;
import com.blazebit.persistence.spi.JpqlFunction;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import java.util.ArrayList;
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
public class ScalarTargetResolvingExpressionVisitor extends PathTargetResolvingExpressionVisitor {

    private final ManagedType<?> managedType;
    private final Map<String, JpqlFunction> functions;
    private boolean parametersAllowed;

    public ScalarTargetResolvingExpressionVisitor(Class<?> managedType, EntityMetamodel metamodel, Map<String, JpqlFunction> functions) {
        this(metamodel.managedType(managedType), metamodel, functions);
    }

    public ScalarTargetResolvingExpressionVisitor(ManagedType<?> managedType, EntityMetamodel metamodel, Map<String, JpqlFunction> functions) {
        super(metamodel, managedType, null);
        this.managedType = managedType;
        this.functions = functions;
        this.parametersAllowed = false;
    }

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    public static interface TargetType {
        
        public boolean hasCollectionJoin();
        
        public Attribute<?, ?> getLeafMethod();
        
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
        private final Attribute<?, ?> leafMethod;
        private final Class<?> leafBaseClass;
        private final Class<?> leafBaseKeyClass;
        private final Class<?> leafBaseValueClass;

        public TargetTypeImpl(boolean hasCollectionJoin, Attribute<?, ?> leafMethod, Class<?> leafBaseClass, Class<?> leafBaseKeyClass, Class<?> leafBaseValueClass) {
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
        public Attribute<?, ?> getLeafMethod() {
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

    public void clear() {
        reset(managedType);
    }

    public List<TargetType> getPossibleTargetTypes() {
        List<PathPosition> positions = pathPositions;
        int size = positions.size();
        
        if (managedType != null && size == 1 && positions.get(0).getAttribute() == null && managedType.getJavaType().equals(positions.get(0).getRealCurrentClass())) {
            // When we didn't resolve any property, the expression is probably static and we can't give types in that case
            return Collections.emptyList();
        }
        
        List<TargetType> possibleTargets = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            PathPosition position = positions.get(i);
            possibleTargets.add(new TargetTypeImpl(position.hasCollectionJoin(), position.getAttribute(), position.getRealCurrentClass(), position.getKeyCurrentClass(), position.getCurrentClass()));
        }
        
        return possibleTargets;
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
    public void visit(ArrayExpression expression) {
        boolean wasParamsAllowed = parametersAllowed;
        List<PathPosition> currentPositions = pathPositions;
        PathPosition position = currentPosition;

        parametersAllowed = true;
        pathPositions = new ArrayList<>();
        pathPositions.add(currentPosition = new PathPosition(managedType, null));
        
        // Validate index against metamodel
        expression.getIndex().accept(this);
        
        parametersAllowed = wasParamsAllowed;
        currentPosition = position;
        pathPositions = currentPositions;
    
        // Only need the base to navigate down the path
        expression.getBase().accept(this);
        currentPosition.setCurrentType(currentPosition.getCurrentType());
    }

    @Override
    public void visit(TreatExpression expression) {
        EntityType<?> type = metamodel.getEntity(expression.getType());
        if (type == null) {
            throw new RuntimeException("No entity found with name \"" + expression.getType() + "\"");
        }
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(type);
    }

    @Override
    public void visit(ParameterExpression expression) {
        // We can't infer a type here
        if (parametersAllowed) {
            // NOTE: We use null as marker for ANY TYPE
            currentPosition.setCurrentType(null);
        } else {
            // If there are other branches (path positions) i.e. of a case when that have a type, we can allow parameters too
            for (PathPosition position : pathPositions) {
                if (position != currentPosition) {
                    if (position.getCurrentClass() != null) {
                        currentPosition.setCurrentType(null);
                        return;
                    }
                }
            }
            // NOTE: plain parameters are only supported in the select clause when having an insert!
            invalid(expression, "Parameters are not allowed as results in mapping. Please use @MappingParameter for this instead!");
        }
    }

    @Override
    public void visit(NullExpression expression) {
        // We can't infer a type here
        // TODO: Not sure what happens when this is the result node of a case when
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentType(metamodel.type(expression.getNumericType().getJavaType()));
        }
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentType(metamodel.type(expression.getNumericType().getJavaType()));
        }
    }

    @Override
    public void visit(NumericLiteral expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentType(metamodel.type(expression.getNumericType().getJavaType()));
        }
    }

    @Override
    public void visit(BooleanLiteral expression) {
        currentPosition.setCurrentType(metamodel.type(Boolean.class));
    }

    @Override
    public void visit(StringLiteral expression) {
        currentPosition.setCurrentType(metamodel.type(String.class));
    }

    @Override
    public void visit(DateLiteral expression) {
        currentPosition.setCurrentType(metamodel.type(Date.class));
    }

    @Override
    public void visit(TimeLiteral expression) {
        currentPosition.setCurrentType(metamodel.type(Date.class));
    }

    @Override
    public void visit(TimestampLiteral expression) {
        currentPosition.setCurrentType(metamodel.type(Date.class));
    }

    @Override
    public void visit(SubqueryExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(ListIndexExpression expression) {
        expression.getPath().accept(this);
        if (!(currentPosition.getAttribute() instanceof ListAttribute<?, ?>)) {
            invalid(expression, "Does not resolve to java.util.List!");
        } else {
            currentPosition.setAttribute(new ListIndexAttribute<>((ListAttribute<?, ?>) currentPosition.getAttribute()));
            currentPosition.setCurrentType(metamodel.type(Integer.class));
        }
    }

    @Override
    public void visit(MapEntryExpression expression) {
        expression.getPath().accept(this);
        if (!(currentPosition.getAttribute() instanceof MapAttribute<?, ?, ?>)) {
            invalid(expression, "Does not resolve to java.util.Map!");
        } else {
            currentPosition.setAttribute(null);
            currentPosition.setCurrentType(metamodel.type(Map.Entry.class));
        }
    }

    @Override
    public void visit(MapKeyExpression expression) {
        expression.getPath().accept(this);
        if (!(currentPosition.getAttribute() instanceof MapAttribute<?, ?, ?>)) {
            invalid(expression, "Does not resolve to java.util.Map!");
        } else {
            currentPosition.setAttribute(new MapKeyAttribute<>((MapAttribute<?, Object, ?>) currentPosition.getAttribute()));
            currentPosition.setCurrentType(((MapAttribute<?, Object, ?>) currentPosition.getAttribute()).getKeyType());
        }
    }

    @Override
    public void visit(MapValueExpression expression) {
        expression.getPath().accept(this);
        if (!(currentPosition.getAttribute() instanceof MapAttribute<?, ?, ?>)) {
            invalid(expression, "Does not resolve to java.util.Map!");
        } else {
            currentPosition.setCurrentType(currentPosition.getCurrentType());
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
            currentPosition.setAttribute(null);
            currentPosition.setCurrentType(metamodel.type(Long.class));
        } else {
            resolveFirst(expression.getExpressions(), true);
            resolveToFunctionReturnType(name);
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
            position.setAttribute(null);
            position.setCurrentType(metamodel.type(returnType));
        }
    }

    @Override
    public void visit(TrimExpression expression) {
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(String.class));
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        visit((GeneralCaseExpression) expression);
    }

    @Override
    public void visit(WhenClauseExpression expression) {
        expression.getResult().accept(this);
    }

}
