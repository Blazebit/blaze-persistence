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

package com.blazebit.persistence.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blazebit.persistence.impl.expression.ArithmeticExpression;
import com.blazebit.persistence.impl.expression.ArithmeticFactor;
import com.blazebit.persistence.impl.expression.ArrayExpression;
import com.blazebit.persistence.impl.expression.DateLiteral;
import com.blazebit.persistence.impl.expression.EntityLiteral;
import com.blazebit.persistence.impl.expression.EnumLiteral;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.FunctionExpression;
import com.blazebit.persistence.impl.expression.GeneralCaseExpression;
import com.blazebit.persistence.impl.expression.ListIndexExpression;
import com.blazebit.persistence.impl.expression.MapEntryExpression;
import com.blazebit.persistence.impl.expression.MapKeyExpression;
import com.blazebit.persistence.impl.expression.MapValueExpression;
import com.blazebit.persistence.impl.expression.NullExpression;
import com.blazebit.persistence.impl.expression.NumericLiteral;
import com.blazebit.persistence.impl.expression.ParameterExpression;
import com.blazebit.persistence.impl.expression.PathElementExpression;
import com.blazebit.persistence.impl.expression.PathExpression;
import com.blazebit.persistence.impl.expression.PropertyExpression;
import com.blazebit.persistence.impl.expression.SimpleCaseExpression;
import com.blazebit.persistence.impl.expression.StringLiteral;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.expression.TimeLiteral;
import com.blazebit.persistence.impl.expression.TimestampLiteral;
import com.blazebit.persistence.impl.expression.TreatExpression;
import com.blazebit.persistence.impl.expression.TrimExpression;
import com.blazebit.persistence.impl.expression.TypeFunctionExpression;
import com.blazebit.persistence.impl.expression.WhenClauseExpression;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.BooleanLiteral;
import com.blazebit.persistence.impl.predicate.CompoundPredicate;
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
import com.blazebit.reflection.ReflectionUtils;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;

/**
 * A visitor that can determine possible target types and JPA attributes of a path expression.
 *
 * @author Christian Beikov
 * @since 1.0
 */
public class PathTargetResolvingExpressionVisitor implements Expression.Visitor {

    private final EntityMetamodel metamodel;
    private final String skipBaseNodeAlias;
    private PathPosition currentPosition;
    private List<PathPosition> pathPositions;

    private static class PathPosition {

        private Class<?> currentClass;
        private Class<?> valueClass;
        private Class<?> keyClass;
        private Attribute<?, ?> attribute;

        PathPosition(Class<?> currentClass, Attribute<?, ?> attribute) {
            this.currentClass = currentClass;
            this.attribute = attribute;
        }
        
        private PathPosition(Class<?> currentClass, Class<?> valueClass, Class<?> keyClass, Attribute<?, ?> attribute) {
            this.currentClass = currentClass;
            this.valueClass = valueClass;
            this.keyClass = keyClass;
            this.attribute = attribute;
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

        public Attribute<?, ?> getAttribute() {
            return attribute;
        }

        public void setAttribute(Attribute<?, ?> attribute) {
            this.attribute = attribute;
        }

        void setValueClass(Class<?> valueClass) {
            this.valueClass = valueClass;
        }

        public void setKeyClass(Class<?> keyClass) {
            this.keyClass = keyClass;
        }

        PathPosition copy() {
            return new PathPosition(currentClass, valueClass, keyClass, attribute);
        }
    }

    public PathTargetResolvingExpressionVisitor(EntityMetamodel metamodel, Class<?> startClass, String skipBaseNodeAlias) {
        this.metamodel = metamodel;
        this.pathPositions = new ArrayList<PathPosition>();
        this.pathPositions.add(currentPosition = new PathPosition(startClass, null));
        this.skipBaseNodeAlias = skipBaseNodeAlias;
    }

    private Attribute<?, ?> resolve(Class<?> currentClass, String property) {
        Attribute<?, ?> attribute = metamodel.getManagedType(currentClass).getAttribute(property);
        // Older Hibernate versions did not throw an exception but returned null instead
        if (attribute == null) {
            throw new IllegalArgumentException("Attribute '" + property + "' not found on type '" + currentClass.getName() + "'");
        }
        return attribute;
    }

    private Class<?> getType(Class<?> baseClass, Attribute<?, ?> attribute) {
        if (attribute.getJavaMember() instanceof Field) {
            return ReflectionUtils.getResolvedFieldType(baseClass, (Field) attribute.getJavaMember());
        } else {
            return ReflectionUtils.getResolvedMethodReturnType(baseClass, (Method) attribute.getJavaMember());
        }
    }

    public Map<Attribute<?, ?>, Class<?>> getPossibleTargets() {
        Map<Attribute<?, ?>, Class<?>> possibleTargets = new HashMap<Attribute<?, ?>, Class<?>>();

        List<PathPosition> positions = pathPositions;
        int size = positions.size();
        for (int i = 0; i < size; i++) {
            PathPosition position = positions.get(i);
            possibleTargets.put(position.getAttribute(), position.getCurrentClass());
        }
        
        return possibleTargets;
    }
    
    @Override
    public void visit(PropertyExpression expression) {
        currentPosition.setAttribute(resolve(currentPosition.getCurrentClass(), expression.getProperty()));
        Attribute<?, ?> attribute = currentPosition.getAttribute();
        Class<?> type = getType(currentPosition.getCurrentClass(), attribute);
        Class<?> valueType = null;
        Class<?> keyType = null;

        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            Class<?>[] typeArguments;
            if (currentPosition.getAttribute().getJavaMember() instanceof Field) {
                typeArguments = ReflectionUtils.getResolvedFieldTypeArguments(currentPosition.getCurrentClass(), (Field) currentPosition.getAttribute().getJavaMember());
            } else {
                typeArguments = ReflectionUtils.getResolvedMethodReturnTypeArguments(currentPosition.getCurrentClass(), (Method) currentPosition.getAttribute().getJavaMember());
            }

            if (typeArguments.length == 0) {
                // Raw types
                if (attribute instanceof MapAttribute<?, ?, ?>) {
                    keyType = ((MapAttribute) attribute).getKeyJavaType();
                }
                valueType = ((PluralAttribute) attribute).getElementType().getJavaType();
            } else {
                valueType = typeArguments[typeArguments.length - 1];
                if (typeArguments.length > 1) {
                    keyType = typeArguments[0];
                }
            }
        } else {
            valueType = type;
        }

        currentPosition.setCurrentClass(type);
        currentPosition.setValueClass(valueType);
        currentPosition.setKeyClass(keyType);
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

            if (expression.getDefaultExpr() != null) {
                PathPosition position = currentPositions.get(j).copy();
                pathPositions = new ArrayList<PathPosition>();
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
            currentPosition.setValueClass(null);
            currentPosition.setKeyClass(Integer.class);
        }
    }

    @Override
    public void visit(MapEntryExpression expression) {
        expression.getPath().accept(this);
        currentPosition.setAttribute(new MapEntryAttribute<>((MapAttribute<?, Object, ?>) currentPosition.getAttribute()));
        currentPosition.setCurrentClass(Map.Entry.class);
    }

    @Override
    public void visit(MapKeyExpression expression) {
        expression.getPath().accept(this);
        currentPosition.setAttribute(new MapKeyAttribute<>((MapAttribute<?, Object, ?>) currentPosition.getAttribute()));
        currentPosition.setValueClass(null);
    }

    @Override
    public void visit(MapValueExpression expression) {
        expression.getPath().accept(this);
        currentPosition.setKeyClass(null);
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
        currentPosition.setCurrentClass(type.getJavaType());
        currentPosition.setValueClass(type.getJavaType());
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
