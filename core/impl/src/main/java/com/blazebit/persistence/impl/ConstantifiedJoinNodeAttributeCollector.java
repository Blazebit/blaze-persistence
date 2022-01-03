/*
 * Copyright 2014 - 2022 Blazebit.
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
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
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

    private static final String KEY_FUNCTION = "key()";

    private final EntityMetamodel metamodel;
    private final AliasManager aliasManager;
    private Map<Object, Map<String, Boolean>> constantifiedJoinNodeAttributes;
    private Set<CompoundPredicate> analyzedPredicates;
    private JoinNode firstRootNode;
    private boolean innerJoin;
    private boolean negated;
    private boolean inKey;

    public ConstantifiedJoinNodeAttributeCollector(EntityMetamodel metamodel, AliasManager aliasManager) {
        this.metamodel = metamodel;
        this.aliasManager = aliasManager;
        this.constantifiedJoinNodeAttributes = new HashMap<>();
        this.analyzedPredicates = Collections.newSetFromMap(new IdentityHashMap<CompoundPredicate, Boolean>());
    }

    public void reset() {
        analyzedPredicates.clear();
        firstRootNode = null;
        innerJoin = false;
        negated = false;
        constantifiedJoinNodeAttributes.clear();
    }

    public void collectConstantifiedJoinNodeAttributes(CompoundPredicate rootPredicate, JoinNode firstRootNode, boolean innerJoin) {
        if (!analyzedPredicates.add(rootPredicate)) {
            return;
        }
        this.firstRootNode = firstRootNode;
        this.innerJoin = innerJoin;
        rootPredicate.accept(this);
    }

    public Map<Object, Map<String, Boolean>> getConstantifiedJoinNodeAttributes() {
        return constantifiedJoinNodeAttributes;
    }

    public boolean isConstantified(JoinNode node) {
        if (node.isTreatedJoinNode()) {
            node = ((TreatedJoinAliasInfo) node.getAliasInfo()).getTreatedJoinNode();
        }
        // The first root node is not considered to be a collection, all others are
        if (node == firstRootNode) {
            return true;
        }
        Map<String, Boolean> constantifiedAttributes = constantifiedJoinNodeAttributes.get(node);
        if (constantifiedAttributes == null) {
            return false;
        }
        if (constantifiedAttributes.containsKey(KEY_FUNCTION)) {
            return true;
        }
        ExtendedManagedType<?> extendedManagedType = metamodel.getManagedType(ExtendedManagedType.class, node.getManagedType());
        if (extendedManagedType.getIdAttributes().isEmpty()) {
            for (ExtendedAttribute<?, ?> attribute : extendedManagedType.getAttributes().values()) {
                if (attribute.getAttribute() instanceof SingularAttribute<?, ?> && !constantifiedAttributes.containsKey(attribute.getAttributePathString())) {
                    return false;
                }
            }
        } else {
            for (SingularAttribute<?, ?> idAttribute : extendedManagedType.getIdAttributes()) {
                if (!constantifiedAttributes.containsKey(idAttribute.getName())) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isConstantifiedNonOptional(JoinNode node, String attributeName) {
        Map<String, Boolean> constantifiedAttributes = constantifiedJoinNodeAttributes.get(node);
        if (constantifiedAttributes == null) {
            return false;
        }
        return Boolean.TRUE.equals(constantifiedAttributes.containsKey(attributeName));
    }

    @Override
    public void visit(PathExpression expr) {
        PathReference pathReference = expr.getPathReference();
        if (pathReference == null) {
            ((SelectInfo) aliasManager.getAliasInfo(expr.toString())).getExpression().accept(this);
            return;
        }

        JoinNode baseNode = (JoinNode) pathReference.getBaseNode();

        if (pathReference.getField() == null) {
            if (inKey) {
                // We constantify collection as a whole to a single element when reaching this point
                Map<String, Boolean> attributes = new HashMap<>(1);
                attributes.put(KEY_FUNCTION, innerJoin);
                constantifiedJoinNodeAttributes.put(baseNode, attributes);
            } else if (baseNode.getType() instanceof ManagedType<?>) {
                // Here we have a predicate like `d = d2` which is the same as `d.id = d2.id`
                Map<String, Boolean> attributes = constantifiedJoinNodeAttributes.get(baseNode);
                if (attributes == null) {
                    attributes = new HashMap<>();
                    constantifiedJoinNodeAttributes.put(baseNode, attributes);
                }
                ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getManagedType());
                for (SingularAttribute<?, ?> idAttribute : managedType.getIdAttributes()) {
                    addAttribute("", idAttribute, attributes);
                }
            }
            return;
        }

        ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getManagedType());
        ExtendedAttribute<?, ?> extendedAttribute = managedType.getAttribute(pathReference.getField());
        Attribute attr = extendedAttribute.getAttribute();

        // We constantify collection as a whole to a single element when reaching this point
        if (attr instanceof PluralAttribute<?, ?, ?>) {
            if (inKey) {
                Map<String, Boolean> attributes = new HashMap<>(1);
                attributes.put(KEY_FUNCTION, innerJoin);
                constantifiedJoinNodeAttributes.put(baseNode, attributes);
            }
            return;
        }

        int dotIndex = expr.getField().lastIndexOf('.');
        SingularAttribute<?, ?> singularAttr = (SingularAttribute<?, ?>) attr;
        String associationName = getSingleValuedIdAccessAssociationName(pathReference.getField(), extendedAttribute);
        Object baseNodeKey;
        String prefix;
        if (associationName == null) {
            baseNodeKey = baseNode;
            prefix = attr.getDeclaringType() instanceof EmbeddableType<?> ? pathReference.getField().substring(0, dotIndex + 1) : "";
        } else {
            baseNodeKey = new AbstractMap.SimpleEntry<>(baseNode, associationName);
            if (attr.getDeclaringType() instanceof EmbeddableType<?>) {
                prefix = pathReference.getField().substring(associationName.length() + 1, dotIndex + 1);
            } else {
                prefix = "";
            }
        }

        Map<String, Boolean> attributes = constantifiedJoinNodeAttributes.get(baseNodeKey);
        if (attributes == null) {
            attributes = new HashMap<>();
            constantifiedJoinNodeAttributes.put(baseNodeKey, attributes);
        }
        addAttribute(prefix, singularAttr, attributes);
        StringBuilder attributeNameBuilder = null;
        Map<String, Boolean> baseNodeAttributes = null;
        String associationNamePrefix = associationName == null ? "" : associationName + '.';
        // Also add all attributes to the set that resolve to the same column names i.e. which are essentially equivalent
        Map<String, Boolean> newAttributes = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : attributes.entrySet()) {
            String attribute = entry.getKey();
            if (attribute != KEY_FUNCTION) {
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
                                baseNodeAttributes = new HashMap<>();
                                constantifiedJoinNodeAttributes.put(baseNode, baseNodeAttributes);
                            }
                        }
                        baseNodeAttributes.put(attributeName, entry.getValue());
                    } else {
                        newAttributes.put(attributeName, entry.getValue());
                    }
                }
            }
        }
        attributes.putAll(newAttributes);
    }

    private String getSingleValuedIdAccessAssociationName(String field, ExtendedAttribute<?, ?> attr) {
        List<Attribute<?, ?>> attributePath = attr.getAttributePath();
        if (!attr.getAttribute().isAssociation() && attributePath.size() > 1) {
            int endIndex = -1;
            for (Attribute<?, ?> attribute : attributePath) {
                endIndex += attribute.getName().length() + 1;
                if (attribute.isCollection()) {
                    return null;
                } else if (attribute.isAssociation()) {
                    return field.substring(0, endIndex);
                }
            }
        }
        return null;
    }

    private void addAttribute(String prefix, SingularAttribute<?, ?> singularAttribute, Map<String, Boolean> orderedAttributes) {
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
            orderedAttributes.put(attributeName, innerJoin);
        }
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
                Map<Object, Map<String, Boolean>> oldConstantifiedJoinNodeAttributes = constantifiedJoinNodeAttributes;
                try {
                    Map<Object, Map<String, Boolean>> initialConstantifiedJoinNodeAttributes = constantifiedJoinNodeAttributes = new HashMap<>();
                    children.get(0).accept(this);
                    constantifiedJoinNodeAttributes = new HashMap<>();

                    for (int i = 1; i < size; i++) {
                        // If we have no more constantified node attributes left, we stop looking at the predicate
                        if (initialConstantifiedJoinNodeAttributes.isEmpty()) {
                            return;
                        }
                        children.get(i).accept(this);

                        Iterator<Map.Entry<Object, Map<String, Boolean>>> entryIterator = initialConstantifiedJoinNodeAttributes.entrySet().iterator();
                        while (entryIterator.hasNext()) {
                            Map.Entry<Object, Map<String, Boolean>> entry = entryIterator.next();
                            Map<String, Boolean> nodeAttributes = constantifiedJoinNodeAttributes.get(entry.getKey());
                            if (nodeAttributes != null) {
                                Iterator<String> iterator = entry.getValue().keySet().iterator();
                                while (iterator.hasNext()) {
                                    if (!nodeAttributes.containsKey(iterator.next())) {
                                        iterator.remove();
                                    }
                                }
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
                    for (Map.Entry<Object, Map<String, Boolean>> entry : constantifiedJoinNodeAttributes.entrySet()) {
                        Map<String, Boolean> attributes = oldConstantifiedJoinNodeAttributes.get(entry.getKey());
                        if (attributes == null) {
                            attributes = new HashMap<>();
                            oldConstantifiedJoinNodeAttributes.put(entry.getKey(), attributes);
                        }
                        attributes.putAll(entry.getValue());
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
            // TODO: also, it would be nice if we could detect constantifications in disjuncts and work with that
            if (isConstant(predicate.getLeft())) {
                if (isParameterOrLiteral(predicate.getRight())) {
                    predicate.getLeft().accept(this);
                } else {
                    predicate.getRight().accept(this);
                }
            } else if (isConstant(predicate.getRight())) {
                if (isParameterOrLiteral(predicate.getLeft())) {
                    predicate.getRight().accept(this);
                } else {
                    predicate.getLeft().accept(this);
                }
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
            if (isParameterOrLiteral(predicate.getLeft())) {
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

    private static boolean isParameterOrLiteral(Expression expression) {
        return expression instanceof ParameterExpression || expression instanceof LiteralExpression<?>;
    }

    private boolean isConstant(Expression expression) {
        if (isParameterOrLiteral(expression)) {
            return true;
        }

        if (expression instanceof PathExpression) {
            PathReference pathReference = ((PathExpression) expression).getPathReference();
            if (pathReference == null) {
                AliasInfo aliasInfo = aliasManager.getAliasInfo(expression.toString());
                return aliasInfo instanceof SelectInfo && isConstant(((SelectInfo) aliasInfo).getExpression());
            }
            JoinNode baseNode = (JoinNode) pathReference.getBaseNode();
            do {
                if (baseNode.getParentTreeNode() == null) {
                    return isConstantified(baseNode);
                } else {
                    if (baseNode.getParentTreeNode().getAttribute().isCollection()) {
                        return false;
                    }
                }
                baseNode = baseNode.getParent();
            } while (baseNode != null);
        }

        return false;
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