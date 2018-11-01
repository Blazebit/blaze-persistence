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
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.blazebit.persistence.parser.util.JpaMetamodelUtils.ATTRIBUTE_NAME_COMPARATOR;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
class EmbeddableSplittingVisitor extends AbortableVisitorAdapter {

    protected final EntityMetamodel metamodel;
    protected final JpaProvider jpaProvider;
    protected final AliasManager aliasManager;
    protected final SplittingVisitor splittingVisitor;
    protected final List<Expression> splittedOffExpressions;
    protected Expression expressionToSplit;

    public EmbeddableSplittingVisitor(EntityMetamodel metamodel, JpaProvider jpaProvider, AliasManager aliasManager, SplittingVisitor splittingVisitor) {
        this.metamodel = metamodel;
        this.jpaProvider = jpaProvider;
        this.aliasManager = aliasManager;
        this.splittingVisitor = splittingVisitor;
        this.splittedOffExpressions = new ArrayList<>();
    }

    protected void clear() {
        splittedOffExpressions.clear();
    }

    public List<Expression> getSplittedOffExpressions() {
        return splittedOffExpressions;
    }

    public List<Expression> splitOff(Expression expression) {
        expressionToSplit = null;
        expression.accept(this);
        if (collectSplittedOffExpressions(expression)) {
            return null;
        }
        return splittedOffExpressions;
    }

    protected boolean collectSplittedOffExpressions(Expression expression) {
        splittedOffExpressions.clear();
        if (expressionToSplit != null) {
            JoinNode baseNode;
            String field;
            if (expressionToSplit instanceof PathExpression) {
                PathReference pathReference = ((PathExpression) expressionToSplit).getPathReference();
                baseNode = (JoinNode) pathReference.getBaseNode();
                field = pathReference.getField();
            } else if (expressionToSplit instanceof MapKeyExpression) {
                baseNode = ((JoinNode) ((MapKeyExpression) expressionToSplit).getPath().getBaseNode()).getKeyJoinNode();
                field = null;
            } else {
                // This should never happen
                return false;
            }
            String fieldPrefix = field == null ? "" : field + ".";
            ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getJavaType());
            Set<String> orderedAttributes = new TreeSet<>();
            EntityType<?> ownerType;
            if (baseNode.getParentTreeNode() == null && field == null) {
                ownerType = baseNode.getEntityType();
                for (SingularAttribute<?, ?> idAttribute : managedType.getIdAttributes()) {
                    addAttributes(ownerType, null, fieldPrefix, "", idAttribute, orderedAttributes);
                }
            } else {
                Map<String, ? extends ExtendedAttribute<?, ?>> ownedAttributes;
                String prefix = field;
                if (baseNode.getParentTreeNode() != null && jpaProvider.getJpaMetamodelAccessor().isElementCollection(baseNode.getParentTreeNode().getAttribute())) {
                    String elementCollectionPath = baseNode.getParentTreeNode().getRelationName();
                    ExtendedManagedType entityManagedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getParent().getEntityType());
                    ownedAttributes = entityManagedType.getAttributes();
                    if (prefix == null) {
                        prefix = elementCollectionPath;
                    } else {
                        prefix = elementCollectionPath + "." + prefix;
                    }
                } else {
                    ownedAttributes = managedType.getOwnedSingularAttributes();
                }
                orderedAttributes.addAll(JpaUtils.getEmbeddedPropertyPaths((Map<String, ExtendedAttribute<?, ?>>) ownedAttributes, prefix, false, false));
            }

            // Signal the caller that the expression was eliminated
            if (orderedAttributes.isEmpty()) {
                return true;
            }

            for (String orderedAttribute : orderedAttributes) {
                splittedOffExpressions.add(splittingVisitor.splitOff(expression, expressionToSplit, orderedAttribute));
            }
        }

        return false;
    }

    @Override
    public Boolean visit(PathExpression expr) {
        PathReference pathReference = expr.getPathReference();
        if (pathReference == null) {
            return ((SelectInfo) aliasManager.getAliasInfo(expr.toString())).getExpression().accept(this);
        }

        JoinNode baseNode = (JoinNode) pathReference.getBaseNode();
        Attribute attr;
        if (pathReference.getField() == null) {
            if (baseNode.getParentTreeNode() != null) {
                attr = baseNode.getParentTreeNode().getAttribute();
                if (attr instanceof PluralAttribute<?, ?, ?> && ((PluralAttribute) attr).getElementType() instanceof EmbeddableType<?>) {
                    expressionToSplit = expr;
                }
            }
        } else {
            ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getJavaType());
            attr = managedType.getAttribute(pathReference.getField()).getAttribute();

            // This kind of happens when we do an entity select where the entity is split into it's component paths
            // We don't want to split it here though as it won't end up in a group by clause or anything anyway
            if (attr instanceof PluralAttribute<?, ?, ?>) {
                return true;
            }

            SingularAttribute<?, ?> singularAttr = (SingularAttribute<?, ?>) attr;

            int dotIndex = expr.getField().lastIndexOf('.');
            if (dotIndex == -1 && singularAttr.getType() instanceof EmbeddableType<?>) {
                expressionToSplit = expr;
            }
        }

        return true;
    }

    protected void addAttributes(EntityType<?> ownerType, String elementCollectionPath, String fieldPrefix, String prefix, SingularAttribute<?, ?> singularAttribute, Set<String> orderedAttributes) {
        String attributeName;
        if (prefix.isEmpty()) {
            attributeName = singularAttribute.getName();
        } else {
            attributeName = prefix + singularAttribute.getName();
        }
        if (singularAttribute.getType() instanceof EmbeddableType<?>) {
            String newPrefix = attributeName + ".";
            Set<SingularAttribute<?, ?>> subAttributes = new TreeSet<>(ATTRIBUTE_NAME_COMPARATOR);
            subAttributes.addAll(((EmbeddableType<?>) singularAttribute.getType()).getSingularAttributes());
            for (SingularAttribute<?, ?> attribute : subAttributes) {
                addAttributes(ownerType, elementCollectionPath, fieldPrefix, newPrefix, attribute, orderedAttributes);
            }
        } else if (singularAttribute.getType() instanceof ManagedType<?>) {
            String newPrefix = attributeName + ".";
            List<String> attributeNames;
            if (elementCollectionPath == null) {
                attributeNames = jpaProvider.getIdentifierOrUniqueKeyEmbeddedPropertyNames(ownerType, fieldPrefix + attributeName);
            } else {
                attributeNames = jpaProvider.getIdentifierOrUniqueKeyEmbeddedPropertyNames(ownerType, elementCollectionPath, elementCollectionPath + "." + fieldPrefix + attributeName);
            }

            ExtendedAttribute<?, ?> extendedAttribute = (ExtendedAttribute<?, ?>) metamodel.getManagedType(ExtendedManagedType.class, ownerType).getAttributes().get(fieldPrefix + attributeName);
            if (extendedAttribute != null && extendedAttribute.getMappedBy() == null) {
                ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, (ManagedType<?>) singularAttribute.getType());
                for (String attrName : attributeNames) {
                    addAttributes(ownerType, elementCollectionPath, fieldPrefix, newPrefix, (SingularAttribute<?, ?>) managedType.getAttributes().get(attrName).getAttribute(), orderedAttributes);
                }
            }
        } else {
            orderedAttributes.add(attributeName);
        }
    }

    @Override
    public Boolean visit(ListIndexExpression expression) {
        return false;
    }

    @Override
    public Boolean visit(MapKeyExpression expression) {
        PathExpression path = expression.getPath();
        PathReference pathReference = path.getPathReference();
        while (pathReference == null) {
            Expression aliasedExpression = ((SelectInfo) aliasManager.getAliasInfo(path.toString())).getExpression();
            if (aliasedExpression instanceof PathExpression) {
                path = (PathExpression) aliasedExpression;
                pathReference = path.getPathReference();
            } else {
                // This should never happen
                return false;
            }
        }

        JoinNode baseNode = (JoinNode) pathReference.getBaseNode();
        Attribute attr;
        if (baseNode.getParentTreeNode() != null) {
            attr = baseNode.getParentTreeNode().getAttribute();
            if (attr instanceof MapAttribute<?, ?, ?> && ((MapAttribute<?, ?, ?>) attr).getKeyType() instanceof EmbeddableType<?>) {
                expressionToSplit = expression;
            }
        }
        return false;
    }

    @Override
    public Boolean visit(NullExpression expression) {
        // The actual semantics of NULL are, that NULL != NULL
        return true;
    }

    @Override
    public Boolean visit(FunctionExpression expression) {
        switch (expression.getFunctionName().toUpperCase()) {
            // MIN and MAX work with embeddables
            case "MIN":
            case "MAX": {
                Expression expr = expression.getExpressions().get(0);
                return expr instanceof PathExpression && visit((PathExpression) expr);
            }
            default:
                // The use of other functions with embeddable does not make any sense, so don't inspect these
                return false;
        }
    }

    /* Using embeddables in other expressions doesn't make sense, so don't inspect these */

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