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
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.BasicType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A visitor that can determine possible target types and JPA attributes of a path expression.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PathTargetResolvingExpressionVisitor implements Expression.Visitor {

    private static final Class[] EMPTY = new Class[0];

    private final EntityMetamodel metamodel;
    private final String skipBaseNodeAlias;
    private PathPosition currentPosition;
    private List<PathPosition> pathPositions;

    /**
     * @author Christian Beikov
     * @since 1.0.0
     */
    private static class PathPosition {

        private Type<?> currentClass;
        private Type<?> valueClass;
        private Type<?> keyClass;
        private Attribute<?, ?> attribute;

        PathPosition(Type<?> currentClass, Attribute<?, ?> attribute) {
            this.currentClass = currentClass;
            this.attribute = attribute;
        }
        
        private PathPosition(Type<?> currentClass, Type<?> valueClass, Type<?> keyClass, Attribute<?, ?> attribute) {
            this.currentClass = currentClass;
            this.valueClass = valueClass;
            this.keyClass = keyClass;
            this.attribute = attribute;
        }

        Class<?> getRealCurrentClass() {
            return currentClass.getJavaType();
        }

        Type<?> getCurrentType() {
            if (valueClass != null) {
                return valueClass;
            }
            if (keyClass != null) {
                return keyClass;
            }

            return currentClass;
        }

        Class<?> getCurrentClass() {
            return getCurrentType().getJavaType();
        }

        void setCurrentType(Type<?> currentClass) {
            this.currentClass = currentClass;
            this.valueClass = null;
            this.keyClass = null;
        }

        public Attribute<?, ?> getAttribute() {
            return attribute;
        }

        public void setAttribute(Attribute<?, ?> attribute) {
            this.attribute = attribute;
        }

        void setValueType(Type<?> valueClass) {
            this.valueClass = valueClass;
        }

        void setKeyType(Type<?> keyClass) {
            this.keyClass = keyClass;
        }

        PathPosition copy() {
            return new PathPosition(currentClass, valueClass, keyClass, attribute);
        }
    }

    public PathTargetResolvingExpressionVisitor(EntityMetamodel metamodel, Type<?> startClass, String skipBaseNodeAlias) {
        this.metamodel = metamodel;
        this.pathPositions = new ArrayList<>();
        this.pathPositions.add(currentPosition = new PathPosition(startClass, null));
        this.skipBaseNodeAlias = skipBaseNodeAlias;
    }

    private Type<?> getType(Type<?> baseType, Attribute<?, ?> attribute) {
        Class<?> baseClass = baseType.getJavaType();

        if (baseClass != null) {
            Class<?> clazz = JpaMetamodelUtils.resolveFieldClass(baseType.getJavaType(), attribute);
            if (clazz != null) {
                return metamodel.type(clazz);
            }
        }
        if (attribute instanceof PluralAttribute<?, ?, ?>) {
            return ((PluralAttribute<?, ?, ?>) attribute).getElementType();
        }
        return ((SingularAttribute<?, ?>) attribute).getType();
    }

    public Map<Attribute<?, ?>, Type<?>> getPossibleTargets() {
        Map<Attribute<?, ?>, Type<?>> possibleTargets = new HashMap<>();

        List<PathPosition> positions = pathPositions;
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            PathPosition position = positions.get(i);
            possibleTargets.put(position.getAttribute(), position.getCurrentType());
        }
        
        return possibleTargets;
    }
    
    @Override
    public void visit(PropertyExpression expression) {
        String property = expression.getProperty();
        if (currentPosition.getCurrentType() instanceof BasicType<?>) {
            throw new IllegalArgumentException("Can't access property '" + property + "' on basic type '" + JpaMetamodelUtils.getTypeName(currentPosition.getCurrentType()) + "'. Did you forget to add the embeddable type to your persistence.xml?");
        }
        Attribute<?, ?> attribute = JpaMetamodelUtils.getAttribute((ManagedType<?>) currentPosition.getCurrentType(), property);
        // Older Hibernate versions did not throw an exception but returned null instead
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute '" + property + "' not found on type '" + JpaMetamodelUtils.getTypeName(currentPosition.getCurrentType()) + "'");
        }
        currentPosition.setAttribute(attribute);
        Type<?> type = getType(currentPosition.getCurrentType(), attribute);
        Type<?> valueType = null;
        Type<?> keyType = null;

        if (attribute instanceof PluralAttribute<?, ?, ?>) {
            Class<?> javaType = type.getJavaType();
            Class<?>[] typeArguments;
            if (javaType == null) {
                typeArguments = EMPTY;
            } else {
                if (attribute.getJavaMember() instanceof Field) {
                    typeArguments = ReflectionUtils.getResolvedFieldTypeArguments(currentPosition.getCurrentClass(), (Field) attribute.getJavaMember());
                } else if (attribute.getJavaMember() instanceof Method) {
                    typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getCurrentClass(), (Method) attribute.getJavaMember());
                } else {
                    typeArguments = EMPTY;
                }
            }

            if (typeArguments.length == 0) {
                // Raw types
                if (attribute instanceof MapAttribute<?, ?, ?>) {
                    keyType = ((MapAttribute<?, ?, ?>) attribute).getKeyType();
                }
                valueType = ((PluralAttribute<?, ?, ?>) attribute).getElementType();
            } else {
                valueType = metamodel.type(typeArguments[typeArguments.length - 1]);
                if (typeArguments.length > 1) {
                    keyType = metamodel.type(typeArguments[0]);
                }
            }
        } else {
            valueType = type;
        }

        currentPosition.setCurrentType(type);
        currentPosition.setValueType(valueType);
        currentPosition.setKeyType(keyType);
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
                pathPositions = new ArrayList<>();
                pathPositions.add(currentPosition = position);
                expressions.get(i).accept(this);
                newPositions.addAll(pathPositions);
            }

            if (expression.getDefaultExpr() != null) {
                PathPosition position = currentPositions.get(j).copy();
                pathPositions = new ArrayList<>();
                pathPositions.add(currentPosition = position);
                expression.getDefaultExpr().accept(this);
                newPositions.addAll(pathPositions);
            }
        }
        
        currentPosition = null;
        pathPositions = newPositions;
    }

    @Override
    public void visit(PathExpression expression) {
        if (currentPosition.getCurrentType() == null) {
            currentPosition.setCurrentType(expression.getPathReference().getType());
            return;
        }
        List<PathElementExpression> expressions = expression.getExpressions();
        int size = expressions.size();
        int i = 0;
        // Skip the base node in (absolute) path expressions otherwise the resolving will fail
        if (size > 1) {
            PathElementExpression firstExpression = expressions.get(0);
            if (firstExpression instanceof PropertyExpression) {
                if (((PropertyExpression) firstExpression).getProperty().equals(skipBaseNodeAlias)) {
                    i = 1;
                }
            }
        }
        for (; i < size; i++) {
            expressions.get(i).accept(this);
        }
    }

    @Override
    public void visit(ListIndexExpression expression) {
        expression.getPath().accept(this);
        Class<?> type = currentPosition.getRealCurrentClass();

        if (!List.class.isAssignableFrom(type)) {
            invalid(expression, "Does not resolve to java.util.List!");
        } else {
            currentPosition.setAttribute(new ListIndexAttribute<>((ListAttribute<?, ?>) currentPosition.getAttribute()));
            currentPosition.setValueType(null);
            currentPosition.setKeyType(metamodel.type(Integer.class));
        }
    }

    @Override
    public void visit(MapEntryExpression expression) {
        expression.getPath().accept(this);
        currentPosition.setAttribute(new MapEntryAttribute<>((MapAttribute<?, Object, ?>) currentPosition.getAttribute()));
        currentPosition.setCurrentType(metamodel.type(Map.Entry.class));
    }

    @Override
    public void visit(MapKeyExpression expression) {
        expression.getPath().accept(this);
        currentPosition.setAttribute(new MapKeyAttribute<>((MapAttribute<?, Object, ?>) currentPosition.getAttribute()));
        currentPosition.setValueType(null);
    }

    @Override
    public void visit(MapValueExpression expression) {
        expression.getPath().accept(this);
        currentPosition.setKeyType(null);
    }

    @Override
    public void visit(ArrayExpression expression) {
        // Only need the base to navigate down the path
        expression.getBase().accept(this);
    }

    @Override
    public void visit(TreatExpression expression) {
        boolean handled = false;
        if (expression.getExpression() instanceof PathExpression) {
            PathExpression treatPath = (PathExpression) expression.getExpression();
            if (treatPath.getExpressions().size() == 1 && skipBaseNodeAlias.equals(treatPath.getExpressions().get(0).toString())) {
                // When we encounter a naked root treat like "TREAT(alias AS Subtype)" we always skip it
                handled = true;
            }
        }
        if (!handled) {
            expression.getExpression().accept(this);
        }

        EntityType<?> type = metamodel.getEntity(expression.getType());
        // TODO: should we check if the type is actually a sub- or super type?
        currentPosition.setCurrentType(type);
        currentPosition.setValueType(type);
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
    public void visit(FunctionExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(TypeFunctionExpression expression) {
        invalid(expression);
    }

    @Override
    public void visit(TrimExpression expression) {
        expression.getTrimSource().accept(this);
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
