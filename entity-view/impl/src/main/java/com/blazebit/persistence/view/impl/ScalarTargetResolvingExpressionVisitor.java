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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.ListIndexAttribute;
import com.blazebit.persistence.parser.MapKeyAttribute;
import com.blazebit.persistence.parser.PathTargetResolvingExpressionVisitor;
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
import com.blazebit.persistence.spi.JpqlFunction;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.NClob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A visitor that can determine possible target types of a scalar expressions.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ScalarTargetResolvingExpressionVisitor extends PathTargetResolvingExpressionVisitor {

    private static final Map<Class<?>, TypeKind> TYPE_KINDS;

    static {
        Map<Class<?>, TypeKind> typeKinds = new HashMap<>();
        typeKinds.put(boolean.class, TypeKind.BOOLEAN);
        typeKinds.put(Boolean.class, TypeKind.BOOLEAN);
        typeKinds.put(byte.class, TypeKind.NUMERIC);
        typeKinds.put(Byte.class, TypeKind.NUMERIC);
        typeKinds.put(short.class, TypeKind.NUMERIC);
        typeKinds.put(Short.class, TypeKind.NUMERIC);
        typeKinds.put(int.class, TypeKind.NUMERIC);
        typeKinds.put(Integer.class, TypeKind.NUMERIC);
        typeKinds.put(long.class, TypeKind.NUMERIC);
        typeKinds.put(Long.class, TypeKind.NUMERIC);
        typeKinds.put(float.class, TypeKind.NUMERIC);
        typeKinds.put(Float.class, TypeKind.NUMERIC);
        typeKinds.put(double.class, TypeKind.NUMERIC);
        typeKinds.put(Double.class, TypeKind.NUMERIC);
        typeKinds.put(BigInteger.class, TypeKind.NUMERIC);
        typeKinds.put(BigDecimal.class, TypeKind.NUMERIC);
        typeKinds.put(char.class, TypeKind.STRING);
        typeKinds.put(Character.class, TypeKind.STRING);
        typeKinds.put(char[].class, TypeKind.STRING);
        typeKinds.put(Character[].class, TypeKind.STRING);
        typeKinds.put(Clob.class, TypeKind.STRING);
        typeKinds.put(NClob.class, TypeKind.STRING);
        typeKinds.put(String.class, TypeKind.STRING);
        TYPE_KINDS = typeKinds;
    }

    private final ManagedType<?> managedType;
    private final Map<String, JpqlFunction> functions;
    private boolean parametersAllowed;

    public ScalarTargetResolvingExpressionVisitor(Class<?> managedType, EntityMetamodel metamodel, Map<String, JpqlFunction> functions, Map<String, javax.persistence.metamodel.Type<?>> rootTypes) {
        this(metamodel.managedType(managedType), metamodel, functions, rootTypes);
    }

    public ScalarTargetResolvingExpressionVisitor(ManagedType<?> managedType, EntityMetamodel metamodel, Map<String, JpqlFunction> functions, Map<String, javax.persistence.metamodel.Type<?>> rootTypes) {
        super(metamodel, managedType, null, rootTypes);
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

    public List<TargetType> getPossibleTargetTypes() {
        List<PathPosition> positions = pathPositions;
        int size = positions.size();
        List<TargetType> possibleTargets = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            PathPosition position = positions.get(i);
            if (position.getCurrentClass() != null) {
                possibleTargets.add(new TargetTypeImpl(position.hasCollectionJoin(), position.getAttribute(), position.getRealCurrentClass(), position.getKeyCurrentClass(), position.getCurrentClass()));
            }
        }
        
        return possibleTargets;
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
        visit(expression, metamodel.type(Boolean.class));
    }

    private void visit(GeneralCaseExpression expression, Type<?> conditionType) {
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
                PathPosition pathPosition = resolve(expressions.get(i).getCondition());
                if (conditionType != null && pathPosition != null && pathPosition.getCurrentType() != null && !isCompatible(conditionType, pathPosition.getCurrentType())) {
                    invalid(expression, "The case predicate compares different types: [" + conditionType.getJavaType().getName() + ", " + typeName(pathPosition) + "]");
                }
                expressions.get(i).getResult().accept(this);

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
        if (expression.getBase() instanceof EntityLiteral) {
            currentPosition.setCurrentType(metamodel.entity(((EntityLiteral) expression.getBase()).getValue()));
        } else {
            expression.getBase().accept(this);
            currentPosition.setCurrentType(currentPosition.getCurrentType());
        }

        boolean wasParamsAllowed = parametersAllowed;
        List<PathPosition> currentPositions = pathPositions;
        PathPosition position = currentPosition;

        parametersAllowed = true;
        pathPositions = new ArrayList<>();
        if (expression.getIndex() instanceof Predicate) {
            pathPositions.add(currentPosition = currentPosition.copy());
        } else {
            pathPositions.add(currentPosition = new PathPosition(managedType, null));
        }
        
        // Validate index against metamodel
        expression.getIndex().accept(this);
        
        parametersAllowed = wasParamsAllowed;
        currentPosition = position;
        pathPositions = currentPositions;
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
        currentPosition.setCurrentType(null);
    }

    @Override
    public void visit(ArithmeticExpression expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentType(metamodel.type(expression.getNumericType().getJavaType()));
        } else {
            currentPosition.setCurrentType(null);
        }
    }

    @Override
    public void visit(ArithmeticFactor expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentType(metamodel.type(expression.getNumericType().getJavaType()));
        } else {
            currentPosition.setCurrentType(null);
        }
    }

    @Override
    public void visit(NumericLiteral expression) {
        if (expression.getNumericType() != null) {
            currentPosition.setCurrentType(metamodel.type(expression.getNumericType().getJavaType()));
        } else {
            currentPosition.setCurrentType(null);
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
            MapKeyAttribute<Object, Object> keyAttribute = new MapKeyAttribute<>((MapAttribute<?, Object, ?>) currentPosition.getAttribute());
            currentPosition.setAttribute(keyAttribute);
            currentPosition.setCurrentType(keyAttribute.getType());
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
        String name = expression.getFunctionName().toLowerCase();
        switch (name) {
            case "function":
                String functionName = ((StringLiteral) expression.getExpressions().get(0)).getValue();
                JpqlFunction jpqlFunction = functions.get(functionName.toLowerCase());
                if (jpqlFunction == null) {
                    // Can't reliably resolve the type
                    currentPosition.setAttribute(null);
                    currentPosition.setCurrentType(null);
                } else {
                    // Skip the function name
                    resolveFirst(expression.getExpressions().subList(1, expression.getExpressions().size()), true);
                    resolveToFunctionReturnType(functionName);
                }
                break;
            case "size":
                // According to our grammar, we can only get a path here
                currentPosition.setAttribute(null);
                currentPosition.setCurrentType(metamodel.type(Long.class));
                break;
            case "coalesce":
                resolveAny(expression.getExpressions(), true);
                resolveToFunctionReturnType(name);
                break;
            default:
                resolveFirst(expression.getExpressions(), true);
                resolveToFunctionReturnType(name);
                break;
        }
    }

    private void resolveAny(List<Expression> expressions, boolean allowParams) {
        List<PathPosition> currentPositions = pathPositions;
        List<PathPosition> newPositions = new ArrayList<>();

        int positionsSize = currentPositions.size();
        int expressionsSize = expressions.size();
        POSITION_LOOP: for (int j = 0; j < positionsSize; j++) {
            for (int i = 0; i < expressionsSize; i++) {
                PathPosition position = currentPositions.get(j).copy();
                pathPositions = new ArrayList<>();
                pathPositions.add(currentPosition = position);
                if (allowParams) {
                    parametersAllowed = true;
                }
                if (expressions.isEmpty()) {
                    position.setCurrentType(null);
                    position.setAttribute(null);
                } else {
                    expressions.get(i).accept(this);
                }
                if (allowParams) {
                    parametersAllowed = false;
                }

                // We just use the type of the first path position that we find
                for (PathPosition newPosition : pathPositions) {
                    if (newPosition.getCurrentClass() != null) {
                        newPositions.add(newPosition);
                        continue POSITION_LOOP;
                    }
                }
            }
            newPositions.add(new PathPosition(null, null));
        }

        currentPosition = null;
        pathPositions = newPositions;
    }

    private void resolveFirst(List<Expression> expressions, boolean allowParams) {
        List<PathPosition> currentPositions = pathPositions;
        List<PathPosition> newPositions = new ArrayList<>();

        int positionsSize = currentPositions.size();
        POSITION_LOOP: for (int j = 0; j < positionsSize; j++) {
            PathPosition position = currentPositions.get(j).copy();
            pathPositions = new ArrayList<>();
            pathPositions.add(currentPosition = position);
            if (allowParams) {
                parametersAllowed = true;
            }
            if (expressions.isEmpty()) {
                position.setCurrentType(null);
                position.setAttribute(null);
            } else {
                expressions.get(0).accept(this);
            }
            if (allowParams) {
                parametersAllowed = false;
            }

            // We just use the type of the first path position that we find
            for (PathPosition newPosition : pathPositions) {
                if (newPosition.getCurrentClass() != null) {
                    newPositions.add(newPosition);
                    continue POSITION_LOOP;
                }
            }
            newPositions.add(new PathPosition(null, null));
        }

        currentPosition = null;
        pathPositions = newPositions;
    }

    private void resolveToFunctionReturnType(String functionName) {
        JpqlFunction function = functions.get(functionName.toLowerCase());
        List<PathPosition> currentPositions = pathPositions;
        int positionsSize = currentPositions.size();

        if (function == null) {
            if (positionsSize == 0) {
                currentPositions.add(new PathPosition(null, null));
            } else {
                for (int i = 0; i < positionsSize; i++) {
                    PathPosition position = currentPositions.get(i);
                    position.setAttribute(null);
                    position.setCurrentType(null);
                }
            }
        } else {
            if (positionsSize == 0) {
                Class<?> returnType = function.getReturnType(null);
                currentPositions.add(new PathPosition(returnType == null ? null : metamodel.type(returnType), null));
            } else {
                for (int i = 0; i < positionsSize; i++) {
                    PathPosition position = currentPositions.get(i);
                    Class<?> returnType = function.getReturnType(position.getCurrentClass());
                    position.setAttribute(null);
                    position.setCurrentType(returnType == null ? null : metamodel.type(returnType));
                }
            }
        }
    }

    @Override
    public void visit(TrimExpression expression) {
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(String.class));
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
        PathPosition pathPosition = resolve(expression.getCaseOperand());
        visit(expression, pathPosition == null ? null : pathPosition.getCurrentType());
    }

    @Override
    public void visit(WhenClauseExpression expression) {
        invalid(expression, "Should be handled by case expression");
    }

    @Override
    public void visit(EnumLiteral expression) {
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(expression.getValue().getDeclaringClass()));
    }

    @Override
    public void visit(EntityLiteral expression) {
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(EntityType.class));
    }

    @Override
    public void visit(TypeFunctionExpression expression) {
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(EntityType.class));
    }

    @Override
    public void visit(CompoundPredicate predicate) {
        List<Predicate> children = predicate.getChildren();
        PathPosition position = currentPosition;
        List<PathPosition> currentPositions = pathPositions;
        for (int i = 0; i < children.size(); i++) {
            pathPositions = new ArrayList<>();
            pathPositions.add(currentPosition = position.copy());
            children.get(i).accept(this);
        }
        pathPositions = currentPositions;
        currentPosition = position;
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(Boolean.class));
    }

    @Override
    public void visit(EqPredicate predicate) {
        visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public void visit(GtPredicate predicate) {
        visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public void visit(GePredicate predicate) {
        visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public void visit(LtPredicate predicate) {
        visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public void visit(LePredicate predicate) {
        visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        visit((BinaryExpressionPredicate) predicate);
    }

    @Override
    public void visit(LikePredicate predicate) {
        visit((BinaryExpressionPredicate) predicate);
    }

    public void visit(BinaryExpressionPredicate predicate) {
        PathPosition left = resolve(predicate.getLeft());
        PathPosition right = resolve(predicate.getRight());
        if (left != null && right != null && left.getCurrentType() != null && right.getCurrentType() != null && !isCompatible(left.getCurrentType(), right.getCurrentType())) {
            invalid(predicate, "The binary predicate compares different types: [" + typeName(left) + ", " + typeName(right) + "]");
        }
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(Boolean.class));
    }

    private PathPosition resolve(Expression expression) {
        boolean wasParamsAllowed = parametersAllowed;
        List<PathPosition> currentPositions = pathPositions;
        PathPosition position = currentPosition;

        parametersAllowed = true;
        pathPositions = new ArrayList<>();
        pathPositions.add(currentPosition = currentPosition.copy());

        // Validate index against metamodel
        expression.accept(this);
        PathPosition expressionPathPosition = currentPosition;

        parametersAllowed = wasParamsAllowed;
        currentPosition = position;
        pathPositions = currentPositions;
        return expressionPathPosition;
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        predicate.getExpression().accept(this);
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(Boolean.class));
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        predicate.getExpression().accept(this);
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(Boolean.class));
    }

    @Override
    public void visit(BetweenPredicate predicate) {
        PathPosition left = resolve(predicate.getLeft());
        PathPosition start = resolve(predicate.getStart());
        PathPosition end = resolve(predicate.getEnd());
        if (left != null && start != null && end != null && left.getCurrentType() != null && start.getCurrentType() != null && end.getCurrentType() != null && (!isCompatible(left.getCurrentType(), start.getCurrentType()) || !isCompatible(left.getCurrentType(), end.getCurrentType()))) {
            invalid(predicate, "The between predicate compares different types: [" + typeName(left) + ", " + typeName(start) + ", " + typeName(end) + "]");
        }
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(Boolean.class));
    }

    @Override
    public void visit(InPredicate predicate) {
        PathPosition left = resolve(predicate.getLeft());
        List<Expression> expressions = predicate.getRight();
        List<String> expressionTypes = new ArrayList<>(expressions.size());

        if (left != null && left.getCurrentType() != null) {
            boolean invalid = false;

            for (int i = 0; i < expressions.size(); i++) {
                PathPosition pathPosition = resolve(expressions.get(i));
                if (pathPosition != null) {
                    if (!isCompatible(left.getCurrentType(), pathPosition.getCurrentType())) {
                        invalid = true;
                    }
                    expressionTypes.add(typeName(pathPosition));
                }
            }

            if (invalid) {
                invalid(predicate, "The in predicate compares the left type '" + typeName(left) + "' with different item types: " + expressionTypes);
            }
        }
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(Boolean.class));
    }

    @Override
    public void visit(ExistsPredicate predicate) {
        currentPosition.setAttribute(null);
        currentPosition.setCurrentType(metamodel.type(Boolean.class));
    }

    private static String typeName(PathPosition p) {
        if (p == null || p.getCurrentType() == null) {
            return "null";
        }
        return p.getCurrentType().getJavaType().getName();
    }

    private boolean isCompatible(Type<?> t1, Type<?> t2) {
        if (t1 == null || t2 == null) {
            return true;
        }
        if (t1 == t2) {
            return true;
        }

        Class<?> t1Type = t1.getJavaType();
        Class<?> t2Type = t2.getJavaType();
        TypeKind typeKind1 = TYPE_KINDS.get(t1Type);
        TypeKind typeKind2 = TYPE_KINDS.get(t2Type);
        return typeKind1 == typeKind2 || t1Type == t2Type;
    }

    /**
     * Type kinds.
     *
     * @author Christian Beikov
     * @since 1.5.0
     */
    private static enum TypeKind {
        BOOLEAN,
        NUMERIC,
        STRING;
    }

}
