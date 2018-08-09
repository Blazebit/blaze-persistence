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
import com.blazebit.persistence.parser.expression.AbortableVisitorAdapter;
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
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedManagedType;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
class FunctionalDependencyAnalyzerVisitor extends AbortableVisitorAdapter {

    private final EntityMetamodel metamodel;
    private final Map<Object, Set<String>> uniquenessMissingJoinNodeAttributes;
    private final Map<Object, List<ResolvedExpression>> uniquenessFormingJoinNodeExpressions;
    private final Map<Object, List<ResolvedExpression>> functionalDependencyRootExpressions;
    private Object lastJoinNode;
    private ResolvedExpression currentResolvedExpression;
    private boolean resultUnique;
    private boolean inKey;

    public FunctionalDependencyAnalyzerVisitor(EntityMetamodel metamodel) {
        this.metamodel = metamodel;
        this.uniquenessMissingJoinNodeAttributes = new HashMap<>();
        this.uniquenessFormingJoinNodeExpressions = new HashMap<>();
        this.functionalDependencyRootExpressions = new LinkedHashMap<>();
    }

    public void clear() {
        uniquenessMissingJoinNodeAttributes.clear();
        uniquenessFormingJoinNodeExpressions.clear();
        functionalDependencyRootExpressions.clear();
        resultUnique = false;
        lastJoinNode = null;
    }

    public boolean analyzeFormsUniqueTuple(Expression expression) {
        lastJoinNode = null;
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
        return unique;
    }

    public ResolvedExpression[] getFunctionalDependencyRootExpressions(ResolvedExpression[] expressions) {
        clear();
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
        JoinNode baseNode = (JoinNode) pathReference.getBaseNode();

        if (pathReference.getField() == null) {
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
            throw new IllegalArgumentException("Ordering by association '" + expr + "' does not make sense! Please order by it's id instead!");
        }

        // First we check if the target attribute is unique, if it isn't, we don't need to check the join structure
        ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getJavaType());
        Attribute attr = managedType.getAttribute(pathReference.getField()).getAttribute();

        if (attr instanceof PluralAttribute<?, ?, ?>) {
            lastJoinNode = baseNode;
            if (inKey) {
                registerFunctionalDependencyRootExpression(baseNode);
                return true;
            }
            throw new IllegalArgumentException("Ordering by plural attribute '" + expr + "' does not make sense! Please order by it's id instead!");
        }

        // Right now we only support ids, but we actually should check for unique constraints
        if (!((SingularAttribute<?, ?>) attr).isId()) {
            registerFunctionalDependencyRootExpression(baseNode);
            return false;
        }

        Object baseNodeKey;
        // Check if we have a single valued id access
        int dotIndex = expr.getField().lastIndexOf('.');
        if (dotIndex == -1) {
            baseNodeKey = baseNode;
        } else {
            // We have to correct the base node for single valued id paths
            String associationName = expr.getField().substring(0, dotIndex);
            Attribute<?, ?> attribute = baseNode.getManagedType().getAttribute(associationName);
            baseNodeKey = new AbstractMap.SimpleEntry<>(baseNode, associationName);
            if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE) {
                registerFunctionalDependencyRootExpression(baseNodeKey);
                return false;
            }
            managedType = metamodel.getManagedType(ExtendedManagedType.class, JpaMetamodelUtils.resolveFieldClass(baseNode.getJavaType(), attribute));
        }

        registerFunctionalDependencyRootExpression(baseNodeKey);

        // First we initialize the names of the id attributes as set for the join node
        Set<String> orderedAttributes = uniquenessMissingJoinNodeAttributes.get(baseNodeKey);
        if (orderedAttributes == null) {
            orderedAttributes = new HashSet<>();
            for (SingularAttribute<?, ?> singularAttribute : managedType.getIdAttributes()) {
                orderedAttributes.add(singularAttribute.getName());
            }
            uniquenessMissingJoinNodeAttributes.put(baseNodeKey, orderedAttributes);
        }

        // We remove for every id attribute from the initialized set of id attribute names
        if (orderedAttributes.remove(attr.getName()) && currentResolvedExpression != null) {
            List<ResolvedExpression> resolvedExpressions = uniquenessFormingJoinNodeExpressions.get(baseNodeKey);
            if (resolvedExpressions == null) {
                resolvedExpressions = new ArrayList<>(orderedAttributes.size() + 1);
                uniquenessFormingJoinNodeExpressions.put(baseNodeKey, resolvedExpressions);
            }
            resolvedExpressions.add(currentResolvedExpression);
        }
        lastJoinNode = baseNodeKey;

        // While there still are some attribute names left, we simply report that it isn't unique, yet
        if (!orderedAttributes.isEmpty()) {
            return false;
        }

        // But even now that we order by all id attribute parts, we still have to make sure this join node is uniqueness preserving

        while (baseNode.getParent() != null) {
            if (baseNode.getParentTreeNode() == null) {
                // Don't assume uniqueness when encountering a cross or entity join
                // To support this, we need to find top-level equality predicates between unique keys of the joined relations in the query
                return false;
            } else {
                attr = baseNode.getParentTreeNode().getAttribute();
                // Only one-to-one relation joins i.e. joins having a unique key with unique key equality predicate are uniqueness preserving

                // TODO: With #610 we could actually also support ManyToOne relations here if there is a unique key with constant equality predicate for the parent node which is a root node
                // This is relevant for queries like `select e.manyToOne.id, e.manyToOne.name from Entity e order by e.manyToOne.id`
                // Normally, the expression `e.manyToOne.id` wouldn't be considered unique, unless there is a unique key with constant equality predicate
                // i.e. a predicate like `e.id = 1` that essentially "constantifies" the parent join node
                if (attr.getPersistentAttributeType() != Attribute.PersistentAttributeType.ONE_TO_ONE) {
                    return false;
                }
                baseNode = baseNode.getParent();
            }
        }

        return true;
    }

    private void registerFunctionalDependencyRootExpression(Object baseNode) {
        if (!uniquenessFormingJoinNodeExpressions.containsKey(baseNode)) {
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
            // MIN and MAX are special aggregate functions that preserve uniqueness
            case "MIN":
            case "MAX": {
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