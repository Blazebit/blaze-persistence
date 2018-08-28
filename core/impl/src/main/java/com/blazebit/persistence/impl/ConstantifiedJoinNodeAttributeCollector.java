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

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
import com.blazebit.persistence.parser.expression.ArithmeticFactor;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.GeneralCaseExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.LiteralExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PathReference;
import com.blazebit.persistence.parser.expression.SimpleCaseExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TrimExpression;
import com.blazebit.persistence.parser.expression.TypeFunctionExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.BetweenPredicate;
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
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collects attribute names of join nodes that have been constantified.
 * Currently only works with simple equality predicates in compound predicates.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
class ConstantifiedJoinNodeAttributeCollector extends VisitorAdapter {

    private final EntityMetamodel metamodel;
    private Map<Object, Set<String>> constantifiedJoinNodeAttributes;
    private CompoundPredicate rootPredicate;
    private boolean negated;
    private boolean inKey;

    public ConstantifiedJoinNodeAttributeCollector(EntityMetamodel metamodel) {
        this.metamodel = metamodel;
        this.constantifiedJoinNodeAttributes = new HashMap<>();
    }

    public void reset() {
        rootPredicate = null;
        negated = false;
        constantifiedJoinNodeAttributes.clear();
    }

    public Map<Object, Set<String>> collectConstantifiedJoinNodeAttributes(CompoundPredicate rootPredicate) {
        if (this.rootPredicate != rootPredicate) {
            reset();
        }
        rootPredicate.accept(this);
        return constantifiedJoinNodeAttributes;
    }

    public Map<Object, Set<String>> getConstantifiedJoinNodeAttributes() {
        return constantifiedJoinNodeAttributes;
    }

    @Override
    public void visit(PathExpression expr) {
        PathReference pathReference = expr.getPathReference();
        JoinNode baseNode = (JoinNode) pathReference.getBaseNode();

        // We constantify collection as a whole to a single element when reaching this point
        if (pathReference.getField() == null) {
            if (inKey) {
                constantifiedJoinNodeAttributes.put(baseNode, Collections.singleton("key"));
            }
            return;
        }

        ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getJavaType());
        Attribute attr = managedType.getAttribute(pathReference.getField()).getAttribute();

        // We constantify collection as a whole to a single element when reaching this point
        if (attr instanceof PluralAttribute<?, ?, ?>) {
            if (inKey) {
                constantifiedJoinNodeAttributes.put(baseNode, Collections.singleton("key"));
            }
            return;
        }

        boolean isEmbeddedIdPart = false;
        int dotIndex = -1;
        SingularAttribute<?, ?> singularAttr = (SingularAttribute<?, ?>) attr;
        Object baseNodeKey;
        String associationName = null;
        if (singularAttr.isId() || (isEmbeddedIdPart = isEmbeddedIdPart(baseNode, pathReference.getField(), singularAttr))) {
            // Check if we have a single valued id access
            dotIndex = expr.getField().lastIndexOf('.');
            if (dotIndex == -1) {
                baseNodeKey = baseNode;
            } else if (isEmbeddedIdPart) {
                baseNodeKey = baseNode;
            } else {
                // We have to correct the base node for single valued id paths
                associationName = expr.getField().substring(0, dotIndex);
                baseNodeKey = new AbstractMap.SimpleEntry<>(baseNode, associationName);
            }
        } else {
            baseNodeKey = baseNode;
        }

        Set<String> attributes = constantifiedJoinNodeAttributes.get(baseNodeKey);
        if (attributes == null) {
            attributes = new HashSet<>();
            constantifiedJoinNodeAttributes.put(baseNodeKey, attributes);
        }
        String prefix = isEmbeddedIdPart ? pathReference.getField().substring(0, dotIndex + 1) : "";
        addAttribute(prefix, singularAttr, attributes);
        StringBuilder attributeNameBuilder = null;
        Set<String> baseNodeAttributes = null;
        String associationNamePrefix = associationName == null ? "" : associationName + '.';
        // Also add all attributes to the set that resolve to the same column names i.e. which are essentially equivalent
        List<String> newAttributes = new ArrayList<>();
        for (String attribute : attributes) {
            for (ExtendedAttribute<?, ?> columnEquivalentAttribute : managedType.getAttribute(associationNamePrefix + attribute).getColumnEquivalentAttributes()) {
                List<Attribute<?, ?>> attributePath = columnEquivalentAttribute.getAttributePath();
                String attributeName;
                if (attributePath.size() == 1) {
                    attributeName = attributePath.get(0).getName();
                } else {
                    if (attributeNameBuilder == null) {
                        attributeNameBuilder = new StringBuilder();
                    } else {
                        attributeNameBuilder.setLength(0);
                    }
                    attributeNameBuilder.append(attributePath.get(0).getName());
                    for (int i = 1; i < attributePath.size(); i++) {
                        attributeNameBuilder.append('.');
                        attributeNameBuilder.append(attributePath.get(i).getName());
                    }
                    attributeName = attributeNameBuilder.toString();
                }

                // Be careful with single valued association ids, they have a different baseNodeKey
                if (!associationNamePrefix.isEmpty() && !attributeName.startsWith(associationNamePrefix)) {
                    if (baseNodeAttributes == null) {
                        baseNodeAttributes = constantifiedJoinNodeAttributes.get(baseNode);
                        if (baseNodeAttributes == null) {
                            baseNodeAttributes = new HashSet<>();
                            constantifiedJoinNodeAttributes.put(baseNode, baseNodeAttributes);
                        }
                    }
                    baseNodeAttributes.add(attributeName);
                } else {
                    newAttributes.add(attributeName);
                }
            }
        }
        attributes.addAll(newAttributes);
    }

    private void addAttribute(String prefix, SingularAttribute<?, ?> singularAttribute, Set<String> orderedAttributes) {
        String attributeName;
        if (prefix.isEmpty()) {
            attributeName = singularAttribute.getName();
        } else {
            attributeName = prefix + singularAttribute.getName();
        }

        if (singularAttribute.getType() instanceof EmbeddableType<?>) {
            String newPrefix = attributeName + ".";
            for (SingularAttribute<? super Object, ?> attribute : ((EmbeddableType<Object>) singularAttribute.getType()).getSingularAttributes()) {
                addAttribute(newPrefix, attribute, orderedAttributes);
            }
        } else {
            orderedAttributes.add(attributeName);
        }
    }

    private boolean isEmbeddedIdPart(JoinNode baseNode, String field, SingularAttribute<?, ?> attr) {
        if (attr.getDeclaringType() instanceof EmbeddableType<?>) {
            ManagedType<?> managedType = baseNode.getManagedType();
            if (managedType instanceof EntityType<?>) {
                int dotIndex = field.indexOf('.');
                EntityType<?> entityType = (EntityType<?>) managedType;
                SingularAttribute<?, ?> potentialIdAttribute = entityType.getSingularAttribute(field.substring(0, dotIndex));
                return potentialIdAttribute.isId();
            }
        }
        return false;
    }

    @Override
    public void visit(CompoundPredicate predicate) {
        boolean originNegated = negated;
        try {
            if (predicate.isNegated()) {
                negated = !negated;
            }
            List<Predicate> children = predicate.getChildren();
            int size = children.size();
            if (negated == (predicate.getOperator() == CompoundPredicate.BooleanOperator.OR)) {
                // Case for simple AND or a NOT(OR)
                for (int i = 0; i < size; i++) {
                    children.get(i).accept(this);
                }
            } else {
                // Case for simple OR or a NOT(AND)
                Map<Object, Set<String>> oldConstantifiedJoinNodeAttributes = constantifiedJoinNodeAttributes;
                try {
                    Map<Object, Set<String>> initialConstantifiedJoinNodeAttributes = constantifiedJoinNodeAttributes = new HashMap<>();
                    children.get(0).accept(this);
                    constantifiedJoinNodeAttributes = new HashMap<>();

                    for (int i = 1; i < size; i++) {
                        // If we have no more constantified node attributes left, we stop looking at the predicate
                        if (initialConstantifiedJoinNodeAttributes.isEmpty()) {
                            return;
                        }
                        children.get(i).accept(this);

                        Iterator<Map.Entry<Object, Set<String>>> entryIterator = initialConstantifiedJoinNodeAttributes.entrySet().iterator();
                        while (entryIterator.hasNext()) {
                            Map.Entry<Object, Set<String>> entry = entryIterator.next();
                            Set<String> nodeAttributes = constantifiedJoinNodeAttributes.get(entry.getKey());
                            if (nodeAttributes != null) {
                                entry.getValue().retainAll(nodeAttributes);
                                if (!entry.getValue().isEmpty()) {
                                    continue;
                                }
                            }
                            entryIterator.remove();
                        }

                        constantifiedJoinNodeAttributes.clear();
                    }

                    constantifiedJoinNodeAttributes = initialConstantifiedJoinNodeAttributes;
                } finally {
                    // Merge constantified attributes into the existing ones
                    for (Map.Entry<Object, Set<String>> entry : constantifiedJoinNodeAttributes.entrySet()) {
                        Set<String> attributes = oldConstantifiedJoinNodeAttributes.get(entry.getKey());
                        if (attributes == null) {
                            attributes = new HashSet<>();
                            oldConstantifiedJoinNodeAttributes.put(entry.getKey(), attributes);
                        }
                        attributes.addAll(entry.getValue());
                    }

                    constantifiedJoinNodeAttributes = oldConstantifiedJoinNodeAttributes;
                }
            }
        } finally {
            negated = originNegated;
        }
    }

    @Override
    public void visit(EqPredicate predicate) {
        boolean originNegated = negated;
        try {
            if (predicate.isNegated()) {
                negated = !negated;
            }
            // There is nothing we can do here when we are in a negated context
            if (negated) {
                return;
            }
            // TODO: at some point we should build an equivalence class to transitively propagate constantification
            if (isConstant(predicate.getLeft())) {
                predicate.getRight().accept(this);
            } else if (isConstant(predicate.getRight())) {
                predicate.getLeft().accept(this);
            }
        } finally {
            negated = originNegated;
        }
    }

    @Override
    public void visit(InPredicate predicate) {
        boolean originNegated = negated;
        try {
            if (predicate.isNegated()) {
                negated = !negated;
            }
            // There is nothing we can do here when we are in a negated context
            if (negated) {
                return;
            }
            // TODO: at some point we should build an equivalence class to transitively propagate constantification
            if (isConstant(predicate.getLeft())) {
                // Only support the simple case here
                if (predicate.getRight().size() == 1) {
                    predicate.getRight().get(0).accept(this);
                }
            } else {
                // All expressions on the right must be constant
                List<Expression> right = predicate.getRight();
                int size = right.size();
                for (int i = 0; i < size; i++) {
                    if (!isConstant(right.get(i))) {
                        return;
                    }
                }
                predicate.getLeft().accept(this);
            }
        } finally {
            negated = originNegated;
        }
    }

    private static boolean isConstant(Expression expression) {
        return expression instanceof ParameterExpression || expression instanceof LiteralExpression<?>;
    }

    @Override
    public void visit(ListIndexExpression expression) {
        boolean oldInKey = inKey;
        inKey = true;
        try {
            expression.getPath().accept(this);
        } finally {
            inKey = oldInKey;
        }
    }

    @Override
    public void visit(MapKeyExpression expression) {
        boolean oldInKey = inKey;
        inKey = true;
        try {
            expression.getPath().accept(this);
        } finally {
            inKey = oldInKey;
        }
    }

    /* There is nothing we can collect for complex expressions or non-equality predicates, we focus on simple predicates for now */

    @Override
    public void visit(IsNullPredicate predicate) {
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
    }

    @Override
    public void visit(LikePredicate predicate) {
    }

    @Override
    public void visit(BetweenPredicate predicate) {
    }

    @Override
    public void visit(GtPredicate predicate) {
    }

    @Override
    public void visit(GePredicate predicate) {
    }

    @Override
    public void visit(LtPredicate predicate) {
    }

    @Override
    public void visit(LePredicate predicate) {
    }

    @Override
    public void visit(ExistsPredicate predicate) {
    }

    @Override
    public void visit(FunctionExpression expression) {
    }

    @Override
    public void visit(ArrayExpression expression) {
    }

    @Override
    public void visit(GeneralCaseExpression expression) {
    }

    @Override
    public void visit(SimpleCaseExpression expression) {
    }

    @Override
    public void visit(WhenClauseExpression expression) {
    }

    @Override
    public void visit(MapEntryExpression expression) {
    }

    @Override
    public void visit(MapValueExpression expression) {
    }

    @Override
    public void visit(SubqueryExpression expression) {
    }

    @Override
    public void visit(TypeFunctionExpression expression) {
    }

    @Override
    public void visit(TrimExpression expression) {
    }

    @Override
    public void visit(ArithmeticFactor expression) {
    }

    @Override
    public void visit(ArithmeticExpression expression) {
    }

}