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

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PathReference;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.InPredicate;
import com.blazebit.persistence.parser.predicate.IsEmptyPredicate;
import com.blazebit.persistence.parser.predicate.IsNullPredicate;
import com.blazebit.persistence.parser.predicate.MemberOfPredicate;
import com.blazebit.persistence.parser.util.ExpressionUtils;
import com.blazebit.persistence.spi.ExtendedManagedType;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Type;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @author Moritz Becker
 * @since 1.0.0
 */
public class JoinVisitor extends VisitorAdapter implements SelectInfoVisitor {

    private final AssociationParameterTransformerFactory parameterTransformerFactory;
    private final EntityMetamodelImpl metamodel;
    private final JoinVisitor parentVisitor;
    private final JoinManager joinManager;
    private final ParameterManager parameterManager;
    private final boolean needsSingleValuedAssociationIdRemoval;
    private boolean joinRequired;
    private boolean joinWithObjectLeafAllowed = true;
    private ClauseType fromClause;
    private String selectAlias;

    public JoinVisitor(MainQuery mainQuery, JoinVisitor parentVisitor, JoinManager joinManager, ParameterManager parameterManager, boolean needsSingleValuedAssociationIdRemoval) {
        this.parameterTransformerFactory = mainQuery.parameterTransformerFactory;
        this.metamodel = mainQuery.metamodel;
        this.parentVisitor = parentVisitor;
        this.joinManager = joinManager;
        this.parameterManager = parameterManager;
        this.needsSingleValuedAssociationIdRemoval = needsSingleValuedAssociationIdRemoval;
        // By default we require joins
        this.joinRequired = true;
    }

    public ClauseType getFromClause() {
        return fromClause;
    }

    public void setFromClause(ClauseType fromClause) {
        this.fromClause = fromClause;
    }

    @Override
    public void visit(PathExpression expression) {
        visit(expression, false);
    }

    private void visit(PathExpression expression, boolean idRemovable) {
        Expression aliasedExpression;
        if ((aliasedExpression = joinManager.getJoinableSelectAlias(expression, fromClause == ClauseType.SELECT, false)) != null) {
            aliasedExpression.accept(this);
        } else {
            joinManager.implicitJoin(expression, joinWithObjectLeafAllowed, null, fromClause, selectAlias, false, false, joinRequired, idRemovable);
            if (parentVisitor != null) {
                JoinNode baseNode = (JoinNode) expression.getBaseNode();
                AliasManager aliasOwner = baseNode.getAliasInfo().getAliasOwner();
                if (aliasOwner != joinManager.getAliasManager()) {
                    parentVisitor.addClauseDependencies(baseNode, aliasOwner);
                }
            }
        }
    }

    private void addClauseDependencies(JoinNode node, AliasManager aliasOwner) {
        if (aliasOwner != joinManager.getAliasManager()) {
            if (parentVisitor == null) {
                throw new IllegalStateException("Couldn't update clause dependencies because implicit joined node does not seem to belong to the query: " + node);
            }
            parentVisitor.addClauseDependencies(node, aliasOwner);
        } else {
            node.getClauseDependencies().add(fromClause);
        }
    }

    @Override
    public void visit(TreatExpression expression) {
        throw new IllegalArgumentException("Treat should not be a root of an expression: " + expression.toString());
    }

    public boolean isJoinRequired() {
        return joinRequired;
    }
    
    public void setJoinRequired(boolean joinRequired) {
        this.joinRequired = joinRequired;
    }

    @Override
    public void visit(FunctionExpression expression) {
        if (ExpressionUtils.isOuterFunction(expression)) {
            // let the parent visitor handle the path expression of an outer expression
            expression.getExpressions().get(0).accept(parentVisitor);
        } else {
            super.visit(expression);
        }
    }

    // Added eager initialization of subqueries
    @Override
    public void visit(SubqueryExpression expression) {
        // TODO: this is ugly, think of a better way to do this
        ((AbstractCommonQueryBuilder<?, ?, ?, ?, ?>) expression.getSubquery()).applyImplicitJoins(this);
    }

    public boolean isJoinWithObjectLeafAllowed() {
        return joinWithObjectLeafAllowed;
    }

    public void setJoinWithObjectLeafAllowed(boolean joinWithObjectLeafAllowed) {
        this.joinWithObjectLeafAllowed = joinWithObjectLeafAllowed;
    }

    @Override
    public void visit(EqPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        removeAssociationIdIfPossible(predicate.getLeft(), predicate.getRight());
        joinRequired = original;
    }

    @Override
    public void visit(InPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;

        boolean rewritten = false;
        if (predicate.getRight().size() == 1) {
            Expression right = predicate.getRight().get(0);
            if (right instanceof PathExpression || right instanceof ParameterExpression) {
                removeAssociationIdIfPossible(predicate.getLeft(), right);
                rewritten = true;
            }
        }

        if (!rewritten) {
            predicate.getLeft().accept(this);
            for (Expression right : predicate.getRight()) {
                right.accept(this);
            }
        }

        joinRequired = original;
    }

    private void removeAssociationIdIfPossible(Expression left, Expression right) {
        if (needsSingleValuedAssociationIdRemoval && fromClause == ClauseType.JOIN) {
            if (removeAssociationIdIfPossible(left)) {
                // The left expression was successfully rewritten
                if (!removeAssociationIdIfPossible(right)) {
                    // But the right expression failed
                    Type<?> associationType = getAssociationType(left, right);
                    ParameterValueTransformer tranformer = parameterTransformerFactory.getToEntityTranformer(associationType.getJavaType());
                    if (!rewriteToAssociationParam(tranformer, right)) {
                        // If the other part wasn't a parameter, we have to do a "normal" implicit join
                        left.accept(this);
                        right.accept(this);
                    }
                }
            } else {
                if (removeAssociationIdIfPossible(right)) {
                    // The right expression was successfully rewritten, but not the left
                    Type<?> associationType = getAssociationType(left, right);
                    ParameterValueTransformer tranformer = parameterTransformerFactory.getToEntityTranformer(associationType.getJavaType());
                    if (!rewriteToAssociationParam(tranformer, left)) {
                        // If the other part wasn't a parameter, we have to do a "normal" implicit join
                        left.accept(this);
                        right.accept(this);
                    }
                } else {
                    left.accept(this);
                    right.accept(this);
                }
            }
        } else {
            String naturalIdAttribute;
            if (fromClause == ClauseType.JOIN && left instanceof PathExpression && right instanceof PathExpression
                    && (naturalIdAttribute = getNaturalIdAttribute(left, right)) != null) {
                // We can only fix this if both expressions are path expressions
                ((PathExpression) left).getExpressions().add(new PropertyExpression(naturalIdAttribute));
                ((PathExpression) right).getExpressions().add(new PropertyExpression(naturalIdAttribute));
                // Re-visit to update path reference
                left.accept(this);
                right.accept(this);
            } else {
                left.accept(this);
                right.accept(this);
            }
        }
    }

    private boolean removeAssociationIdIfPossible(Expression expression) {
        if (expression instanceof PathExpression) {
            PathExpression pathExpression = (PathExpression) expression;
            visit(pathExpression, true);
            String lastElement = pathExpression.getExpressions().get(pathExpression.getExpressions().size() - 1).toString();
            PathReference pathReference = pathExpression.getPathReference();
            JoinNode node = (JoinNode) pathReference.getBaseNode();
            String field = pathReference.getField();
            if (field == null) {
                // If there is no field, we can only check if the last element is the join nodes relation name
                // If it is, then this is a plain association use like "alias.association" and we didn't remove anything
                if (node.getParentTreeNode() == null) {
                    // A root node
                    if (!lastElement.equals(node.getAlias())) {
                        return true;
                    }
                } else {
                    // A normal join node that could be used either via association name or alias
                    if (!lastElement.equals(node.getParentTreeNode().getRelationName()) && !lastElement.equals(node.getAlias())) {
                        return true;
                    }
                }
            } else {
                if (field.endsWith(lastElement) && (field.length() == lastElement.length() || field.charAt(field.length() - lastElement.length()) == '.')) {
                    // If there is a field, we only have to check if the last element is in the field
                    return false;
                } else {
                    // If it isn't we have removed the id part
                    return true;
                }
            }
        }

        return false;
    }

    private Type<?> getAssociationType(Expression expression1, Expression expression2) {
        if (expression1 instanceof PathExpression) {
            return ((PathExpression) expression1).getPathReference().getType();
        }

        return ((PathExpression) expression2).getPathReference().getType();
    }

    private String getNaturalIdAttribute(Expression expression1, Expression expression2) {
        String naturalIdAttribute = getNaturalIdAttribute(expression1);
        if (naturalIdAttribute != null) {
            return naturalIdAttribute;
        }
        return getNaturalIdAttribute(expression2);
    }

    private String getNaturalIdAttribute(Expression expression) {
        // When comparing an alias with a natural key joined relation, we have to append the natural id to the paths
        // Hibernate fails to do this and instead compares the primary key with the natural key which might go by unnoticed
        if (expression instanceof PathExpression) {
            PathExpression pathExpression = (PathExpression) expression;
            visit(pathExpression, false);
            PathReference pathReference = (pathExpression).getPathReference();
            // We only attach the natural id to paths referring to entity types
            if (pathReference.getField() != null && pathReference.getType() instanceof EntityType<?>) {
                JoinNode node = (JoinNode) pathReference.getBaseNode();
                // We need a parent tree node to determine the natural id attribute
                List<String> identifierOrUniqueKeyEmbeddedPropertyNames = metamodel.getJpaProvider()
                        .getIdentifierOrUniqueKeyEmbeddedPropertyNames(node.getEntityType(), pathReference.getField());
                if (identifierOrUniqueKeyEmbeddedPropertyNames.size() == 1) {
                    // This "fix" only works if we have a single id attribute
                    String naturalIdAttribute = identifierOrUniqueKeyEmbeddedPropertyNames.get(0);
                    ExtendedManagedType extendedManagedType = metamodel.getManagedType(ExtendedManagedType.class, pathReference.getType().getJavaType());
                    if (!extendedManagedType.getIdAttribute().getName().equals(naturalIdAttribute)) {
                        // Now we finally know the natural id attribute name
                        return naturalIdAttribute;
                    }
                }
            }
        }

        return null;
    }

    private boolean rewriteToAssociationParam(ParameterValueTransformer tranformer, Expression expression) {
        if (!(expression instanceof ParameterExpression)) {
            return false;
        }
        ParameterExpression parameterExpression = (ParameterExpression) expression;
        ParameterManager.ParameterImpl<Object> param = (ParameterManager.ParameterImpl<Object>) parameterManager.getParameter(parameterExpression.getName());
        param.setTranformer(tranformer);
        return true;
    }

    @Override
    public void visit(IsNullPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        if (!removeAssociationIdIfPossible(predicate.getExpression())) {
            predicate.getExpression().accept(this);
        }
        joinRequired = original;
    }

    @Override
    public void visit(IsEmptyPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        predicate.getExpression().accept(this);
        joinRequired = original;
    }

    @Override
    public void visit(MemberOfPredicate predicate) {
        boolean original = joinRequired;
        joinRequired = false;
        predicate.getLeft().accept(this);
        predicate.getRight().accept(this);
        joinRequired = original;
    }

    @Override
    public void visit(SelectInfo selectInfo) {
        try {
            this.selectAlias = selectInfo.getAlias();
            selectInfo.getExpression().accept(this);
        } finally {
            this.selectAlias = null;
        }
    }
}
