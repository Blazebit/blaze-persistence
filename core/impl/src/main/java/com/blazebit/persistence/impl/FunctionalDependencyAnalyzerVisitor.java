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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.ArithmeticExpression;
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
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PathReference;
import com.blazebit.persistence.parser.expression.SimpleCaseExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TimeLiteral;
import com.blazebit.persistence.parser.expression.TimestampLiteral;
import com.blazebit.persistence.parser.expression.TrimExpression;
import com.blazebit.persistence.parser.expression.TypeFunctionExpression;
import com.blazebit.persistence.parser.expression.WhenClauseExpression;
import com.blazebit.persistence.parser.predicate.BooleanLiteral;
import com.blazebit.persistence.parser.predicate.CompoundPredicate;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
class FunctionalDependencyAnalyzerVisitor extends EmbeddableSplittingVisitor {

    private final ConstantifiedJoinNodeAttributeCollector constantifiedJoinNodeAttributeCollector;
    private final Map<Object, Map<String, Boolean>> uniquenessMissingJoinNodeAttributes;
    private final Map<Object, List<ResolvedExpression>> uniquenessFormingJoinNodeExpressions;
    private final Map<Object, List<ResolvedExpression>> functionalDependencyRootExpressions;
    private Object lastJoinNode;
    private ResolvedExpression currentResolvedExpression;
    private boolean resultUnique;
    private boolean inKey;

    public FunctionalDependencyAnalyzerVisitor(EntityMetamodel metamodel, SplittingVisitor splittingVisitor, JpaProvider jpaProvider, AliasManager aliasManager) {
        super(metamodel, jpaProvider, aliasManager, splittingVisitor);
        this.constantifiedJoinNodeAttributeCollector = new ConstantifiedJoinNodeAttributeCollector(metamodel, aliasManager);
        this.uniquenessMissingJoinNodeAttributes = new HashMap<>();
        this.uniquenessFormingJoinNodeExpressions = new HashMap<>();
        this.functionalDependencyRootExpressions = new LinkedHashMap<>();
    }

    public void reset() {
        constantifiedJoinNodeAttributeCollector.reset();
    }

    public void clear(CompoundPredicate rootPredicate) {
        super.clear();
        uniquenessMissingJoinNodeAttributes.clear();
        uniquenessFormingJoinNodeExpressions.clear();
        functionalDependencyRootExpressions.clear();
        resultUnique = false;
        lastJoinNode = null;
        constantifiedJoinNodeAttributeCollector.collectConstantifiedJoinNodeAttributes(rootPredicate);
    }

    public boolean analyzeFormsUniqueTuple(Expression expression) {
        lastJoinNode = null;
        expressionToSplit = null;
        boolean unique = expression.accept(this);
        JoinNode p;
        if (lastJoinNode instanceof Map.Entry<?, ?>) {
            p = ((Map.Entry<JoinNode, ?>) lastJoinNode).getKey();
        } else {
            p = (JoinNode) lastJoinNode;
        }
        if (p != null) {
            // Traverse join nodes up and if we see that this expression is part of a collection, we revert result uniqueness
            while (p.getParent() != null) {
                if (p.getParentTreeNode() == null || p.getParentTreeNode().isCollection()) {
                    unique = false;
                    break;
                }
                p = p.getParent();
            }
        }
        resultUnique = resultUnique || unique;

        collectSplittedOffExpressions(expression);

        return unique;
    }

    public ResolvedExpression[] getFunctionalDependencyRootExpressions(CompoundPredicate rootPredicate, ResolvedExpression[] expressions) {
        clear(rootPredicate);
        for (int i = 0; i < expressions.length; i++) {
            currentResolvedExpression = expressions[i];
            if (analyzeFormsUniqueTuple(expressions[i].getExpression())) {
                List<ResolvedExpression> resolvedExpressions = uniquenessFormingJoinNodeExpressions.get(lastJoinNode);
                if (resolvedExpressions != null) {
                    functionalDependencyRootExpressions.put(lastJoinNode, resolvedExpressions);
                    lastJoinNode = null;
                }
            }
        }

        currentResolvedExpression = null;
        lastJoinNode = null;
        if (functionalDependencyRootExpressions.isEmpty()) {
            return null;
        }
        List<ResolvedExpression> resolvedExpressions;
        if (functionalDependencyRootExpressions.size() == 1) {
            resolvedExpressions = functionalDependencyRootExpressions.values().iterator().next();
        } else {
            resolvedExpressions = new ArrayList<>();
            OUTER: for (Map.Entry<Object, List<ResolvedExpression>> entry : functionalDependencyRootExpressions.entrySet()) {
                JoinNode node;
                if (entry.getKey() instanceof Map.Entry<?, ?>) {
                    node = ((Map.Entry<JoinNode, ?>) entry.getKey()).getKey();
                } else {
                    node = (JoinNode) entry.getKey();
                }
                // We always need collections in here
                if (node.getParentTreeNode() == null || node.getParentTreeNode().isCollection()) {
                    // Skip *ToOne join nodes that are functionally dependent on an existing node
                    if (entry.getKey() instanceof Map.Entry<?, ?> && functionalDependencyRootExpressions.containsKey(node)) {
                        continue OUTER;
                    }
                    resolvedExpressions.addAll(entry.getValue());
                } else {
                    // Skip *ToOne join nodes that are functionally dependent on an existing node
                    while (node.getParent() != null) {
                        if (functionalDependencyRootExpressions.containsKey(node.getParent())) {
                            continue OUTER;
                        }
                        node = node.getParent();
                    }
                    resolvedExpressions.addAll(entry.getValue());
                }
            }
        }
        return resolvedExpressions.toArray(new ResolvedExpression[resolvedExpressions.size()]);
    }

    public boolean isResultUnique() {
        return resultUnique;
    }

    @Override
    public Boolean visit(PathExpression expr) {
        PathReference pathReference = expr.getPathReference();
        if (pathReference == null) {
            Expression aliasedExpression = ((SelectInfo) aliasManager.getAliasInfo(expr.toString())).getExpression();
            return aliasedExpression.accept(this);
        }

        JoinNode baseNode = (JoinNode) pathReference.getBaseNode();
        String field = pathReference.getField();

        if (field == null) {
            lastJoinNode = baseNode;
            functionalDependencyRootExpressions.put(baseNode, Collections.singletonList(currentResolvedExpression));
            // This is a basic element collection. The element is it's unique key
            if (baseNode.getType().getPersistenceType() == Type.PersistenceType.BASIC) {
                return true;
            }
            // The key of a collection is it's unique key
            if (inKey) {
                return true;
            }
            field = baseNode.getParentTreeNode().getRelationName();
            baseNode = baseNode.getParent();
        }

        // First we check if the target attribute is unique, if it isn't, we don't need to check the join structure
        ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getManagedType());
        Attribute attr = managedType.getAttribute(field).getAttribute();

        if (attr instanceof PluralAttribute<?, ?, ?>) {
            lastJoinNode = baseNode;
            if (inKey) {
                registerFunctionalDependencyRootExpression(baseNode);
                return true;
            }
            throw new IllegalArgumentException("Ordering by plural attribute '" + expr + "' does not make sense! Please order by it's id instead!");
        }

        // Right now we only support ids, but we actually should check for unique constraints
        boolean isEmbeddedIdPart = false;
        SingularAttribute<?, ?> singularAttr = (SingularAttribute<?, ?>) attr;
        if (!singularAttr.isId() && !(isEmbeddedIdPart = isEmbeddedIdPart(baseNode, field, singularAttr))) {
            registerFunctionalDependencyRootExpression(baseNode);
            return false;
        }

        Object baseNodeKey;
        // Check if we have a single valued id access
        int dotIndex = expr.getField().lastIndexOf('.');
        if (dotIndex == -1) {
            baseNodeKey = baseNode;
            if (singularAttr.getType() instanceof EmbeddableType<?>) {
                expressionToSplit = expr;
            }
        } else if (isEmbeddedIdPart) {
            baseNodeKey = baseNode;
        } else {
            // We have to correct the base node for single valued id paths
            String associationName = expr.getField().substring(0, dotIndex);
            ExtendedManagedType<?> extendedManagedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getManagedType());
            ExtendedAttribute<?, ?> extendedAttribute = extendedManagedType.getAttribute(associationName);
            Attribute<?, ?> attribute = extendedAttribute.getAttribute();
            baseNodeKey = new AbstractMap.SimpleEntry<>(baseNode, associationName);
            if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE) {
                boolean nonConstantParent = true;
                Set<String> constantifiedAttributes = constantifiedJoinNodeAttributeCollector.getConstantifiedJoinNodeAttributes().get(baseNode);
                if (constantifiedAttributes != null) {
                    Map<String, Boolean> orderedAttributes = new HashMap<>();
                    addAttributes(baseNode.getEntityType(), null, "", "", (SingularAttribute<?, ?>) attribute, orderedAttributes);
                    initConstantifiedAttributes(orderedAttributes, constantifiedAttributes);
                    orderedAttributes.remove(expr.getField());
                    String singleNonConstantifiedAttribute = getSingleNonConstantifiedAttribute(orderedAttributes);
                    // If the identifiers are constantified, we don't care if this is a one-to-one
                    if (singleNonConstantifiedAttribute != null && (singleNonConstantifiedAttribute.isEmpty() || equalsAny(singleNonConstantifiedAttribute, extendedManagedType.getAttribute(expr.getField()).getColumnEquivalentAttributes()))) {
                        nonConstantParent = false;
                        orderedAttributes.clear();
                        managedType = metamodel.getManagedType(ExtendedManagedType.class, JpaMetamodelUtils.resolveFieldClass(baseNode.getJavaType(), attribute));
                    }
                }
                if (nonConstantParent) {
                    registerFunctionalDependencyRootExpression(baseNodeKey);
                    return false;
                }
            } else {
                managedType = metamodel.getManagedType(ExtendedManagedType.class, JpaMetamodelUtils.resolveFieldClass(baseNode.getJavaType(), attribute));
            }
        }

        registerFunctionalDependencyRootExpression(baseNodeKey);

        // First we initialize the names of the id attributes as set for the join node
        Map<String, Boolean> orderedAttributes = getUniquenessMissingAttributes(baseNodeKey, managedType);

        // We remove for every id attribute from the initialized set of id attribute names
        String prefix;
        if (dotIndex == -1 && baseNode.getParentTreeNode() != null && !baseNode.getParentTreeNode().isCollection()) {
            prefix = baseNode.getParentTreeNode().getRelationName() + ".";
            JoinNode node = baseNode.getParent();

            while (node.getParentTreeNode() != null) {
                prefix = node.getParentTreeNode().getRelationName() + "." + prefix;
                node = node.getParent();
            }
        } else {
            prefix = isEmbeddedIdPart ? field.substring(0, dotIndex + 1) : "";
        }
        if (removeAttribute(prefix, singularAttr, orderedAttributes) && currentResolvedExpression != null) {
            List<ResolvedExpression> resolvedExpressions = uniquenessFormingJoinNodeExpressions.get(baseNodeKey);
            if (resolvedExpressions == null) {
                resolvedExpressions = new ArrayList<>(orderedAttributes.size() + 1);
                uniquenessFormingJoinNodeExpressions.put(baseNodeKey, resolvedExpressions);
            }
            resolvedExpressions.add(currentResolvedExpression);
        }
        lastJoinNode = baseNodeKey;

        // While there still are some attribute names left, we simply report that it isn't unique, yet
        if (hasNonConstantifiedAttribute(orderedAttributes)) {
            return false;
        }

        // But even now that we order by all id attribute parts, we still have to make sure this join node is uniqueness preserving
        String subPath = field;
        while (baseNode.getParent() != null) {
            if (baseNode.getParentTreeNode() == null) {
                // Don't assume uniqueness when encountering a cross or entity join
                // To support this, we need to find top-level equality predicates between unique keys of the joined relations in the query
                return false;
            } else {
                subPath = baseNode.getParentTreeNode().getRelationName() + "." + subPath;
                attr = baseNode.getParentTreeNode().getAttribute();
                // Only one-to-one relation joins i.e. joins having a unique key with unique key equality predicate are uniqueness preserving
                baseNode = baseNode.getParent();

                if (attr.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE) {
                    Set<String> constantifiedAttributes = constantifiedJoinNodeAttributeCollector.getConstantifiedJoinNodeAttributes().get(baseNode);
                    if (constantifiedAttributes != null) {
                        // If there are constantified attributes for the node, we check if they cover the identifier
                        // This is relevant for queries like `select e.manyToOne.id, e.manyToOne.name from Entity e order by e.manyToOne.id`
                        // Normally, the expression `e.manyToOne.id` wouldn't be considered unique, unless there is a unique key with constant equality predicate
                        // i.e. a predicate like `e.id = 1` that essentially "constantifies" the parent join node
                        ExtendedManagedType<?> extendedManagedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getManagedType());
                        orderedAttributes = new HashMap<>();
                        addAttributes(baseNode.getEntityType(), null, "", "", (SingularAttribute<?, ?>) attr, orderedAttributes);
                        initConstantifiedAttributes(orderedAttributes, constantifiedAttributes);
                        orderedAttributes.remove(subPath);
                        String singleNonConstantifiedAttribute = getSingleNonConstantifiedAttribute(orderedAttributes);
                        // If the identifiers are constantified, we don't care if this is a one-to-one
                        if (singleNonConstantifiedAttribute != null && (singleNonConstantifiedAttribute.isEmpty() || extendedManagedType.getAttributes().containsKey(subPath) && equalsAny(singleNonConstantifiedAttribute, extendedManagedType.getAttribute(subPath).getColumnEquivalentAttributes()))) {
                            continue;
                        }
                    }

                    return false;
                }
            }
        }

        return true;
    }

    private static boolean hasNonConstantifiedAttribute(Map<String, Boolean> orderedAttributes) {
        for (Map.Entry<String, Boolean> entry : orderedAttributes.entrySet()) {
            if (entry.getValue() == Boolean.FALSE) {
                return true;
            }
        }
        return false;
    }

    private static void initConstantifiedAttributes(Map<String, Boolean> orderedAttributes, Set<String> constantifiedAttributes) {
        for (String constantifiedAttribute : constantifiedAttributes) {
            if (orderedAttributes.containsKey(constantifiedAttribute)) {
                orderedAttributes.put(constantifiedAttribute, Boolean.TRUE);
            }
        }
    }

    private String getSingleNonConstantifiedAttribute(Map<String, Boolean> orderedAttributes) {
        // Null means there are multiple or no constantified attributes
        if (orderedAttributes.isEmpty()) {
            // The empty string signals no attributes
            return "";
        }

        String attribute = null;
        for (Map.Entry<String, Boolean> entry : orderedAttributes.entrySet()) {
            if (entry.getValue() == Boolean.FALSE) {
                if (attribute == null) {
                    attribute = entry.getKey();
                } else {
                    return null;
                }
            }
        }

        // Is the case of having only constantified attributes even reasonable?
//        if (attribute == null) {
//            return "";
//        }
        return attribute;
    }

    private boolean equalsAny(String attribute, Set<? extends ExtendedAttribute<?, ?>> columnEquivalentAttributes) {
        StringBuilder sb = null;
        for (ExtendedAttribute<?, ?> columnEquivalentAttribute : columnEquivalentAttributes) {
            List<Attribute<?, ?>> attributePath = columnEquivalentAttribute.getAttributePath();
            String attributeName;
            if (attributePath.size() == 1) {
                attributeName = attributePath.get(0).getName();
            } else {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.setLength(0);
                }
                sb.append(attributePath.get(0).getName());
                for (int i = 1; i < attributePath.size(); i++) {
                    sb.append('.');
                    sb.append(attributePath.get(i).getName());
                }
                attributeName = sb.toString();
            }
            if (attribute.equals(attributeName)) {
                return true;
            }
        }

        return false;
    }

    private Map<String, Boolean> getUniquenessMissingAttributes(Object baseNodeKey, ExtendedManagedType<?> managedType) {
        Map<String, Boolean> orderedAttributes = uniquenessMissingJoinNodeAttributes.get(baseNodeKey);
        if (orderedAttributes == null) {
            orderedAttributes = new HashMap<>();
            JoinNode baseNode;
            EntityType<?> entityType;
            String prefix;
            if (baseNodeKey instanceof Map.Entry<?, ?>) {
                baseNode = (JoinNode) ((Map.Entry) baseNodeKey).getKey();
                String associationName = (String) ((Map.Entry) baseNodeKey).getValue();
                entityType = baseNode.getEntityType();
                prefix = associationName + ".";
            } else {
                baseNode = (JoinNode) baseNodeKey;
                entityType = baseNode.getEntityType();
                prefix = "";
            }

            if (baseNode.getParentTreeNode() == null || baseNode.getParentTreeNode().getAttribute().isCollection()) {
                for (SingularAttribute<?, ?> idAttribute : managedType.getIdAttributes()) {
                    addAttributes(entityType, null, "", "", idAttribute, orderedAttributes);
                }
            } else {
                String fieldPrefix = baseNode.getParentTreeNode().getRelationName();
                fieldPrefix = fieldPrefix.substring(0, fieldPrefix.length() - baseNode.getParentTreeNode().getAttribute().getName().length());
                addAttributes(baseNode.getParent().getEntityType(), null, fieldPrefix, prefix, (SingularAttribute<?, ?>) baseNode.getParentTreeNode().getAttribute(), orderedAttributes);
            }
            Set<String> constantifiedAttributes = constantifiedJoinNodeAttributeCollector.getConstantifiedJoinNodeAttributes().get(baseNodeKey);
            if (constantifiedAttributes != null) {
                initConstantifiedAttributes(orderedAttributes, constantifiedAttributes);
            }
            uniquenessMissingJoinNodeAttributes.put(baseNodeKey, orderedAttributes);
        }
        return orderedAttributes;
    }

    private boolean removeAttribute(String prefix, SingularAttribute<?, ?> singularAttribute, Map<String, Boolean> orderedAttributes) {
        String attributeName;
        if (prefix.isEmpty()) {
            attributeName = singularAttribute.getName();
        } else {
            attributeName = prefix + singularAttribute.getName();
        }

        if (singularAttribute.getType() instanceof EmbeddableType<?>) {
            String newPrefix = attributeName + ".";
            boolean removed = false;
            for (SingularAttribute<? super Object, ?> attribute : ((EmbeddableType<Object>) singularAttribute.getType()).getSingularAttributes()) {
                if (removeAttribute(newPrefix, attribute, orderedAttributes)) {
                    removed = true;
                }
            }
            return removed;
        } else {
            return orderedAttributes.remove(attributeName) != null;
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

    private void registerFunctionalDependencyRootExpression(Object baseNode) {
        if (currentResolvedExpression != null && !uniquenessFormingJoinNodeExpressions.containsKey(baseNode)) {
            List<ResolvedExpression> resolvedExpressions = functionalDependencyRootExpressions.get(baseNode);
            if (resolvedExpressions == null) {
                resolvedExpressions = new ArrayList<>();
                functionalDependencyRootExpressions.put(baseNode, resolvedExpressions);
            }
            resolvedExpressions.add(currentResolvedExpression);
        }
    }

    @Override
    public Boolean visit(ListIndexExpression expression) {
        boolean oldInKey = inKey;
        inKey = true;
        try {
            return expression.getPath().accept(this);
        } finally {
            inKey = oldInKey;
        }
    }

    @Override
    public Boolean visit(MapKeyExpression expression) {
        boolean oldInKey = inKey;
        inKey = true;
        try {
            return expression.getPath().accept(this);
        } finally {
            inKey = oldInKey;
        }
    }

    @Override
    public Boolean visit(NullExpression expression) {
        // The actual semantics of NULL are, that NULL != NULL
        return true;
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        switch (expression.getFunctionName().toUpperCase()) {
            case "COALESCE":
                // Can only be sure to produce unique value if the first element is unique
                return expression.getExpressions().get(0).accept(this);
            case "NULLIF":
                // See visit(NullExpression) for reasoning
                return expression.getExpressions().get(0).accept(this);
            case "ROW_NUMBER":
                // ROW_NUMBER is unique when using no partitions
                return expression.getWindowDefinition() == null || expression.getWindowDefinition().getFilterPredicate() == null && expression.getWindowDefinition().getPartitionExpressions().isEmpty() && expression.getWindowDefinition().getWindowName() == null;
            case "MIN":
            case "MAX": {
                // MIN and MAX are special aggregate functions that preserve uniqueness
                Expression expr = expression.getExpressions().get(0);
                return expr instanceof PathExpression && visit((PathExpression) expr);
            }
            default:
                // The existing JPA functions don't return unique results regardless of their arguments
                return false;
        }
    }

    /* Other expressions can never be detected to be unique */

    @Override
    public Boolean visit(ArrayExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(GeneralCaseExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(SimpleCaseExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(WhenClauseExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(ParameterExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(MapEntryExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(MapValueExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(SubqueryExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(TypeFunctionExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(TrimExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(ArithmeticExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(NumericLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(BooleanLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(StringLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(DateLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(TimeLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(TimestampLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(EnumLiteral expression) {
        return false;
    }

    @Override
    public Boolean visit(EntityLiteral expression) {
        return false;
    }

}