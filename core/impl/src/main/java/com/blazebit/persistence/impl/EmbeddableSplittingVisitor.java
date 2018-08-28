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
import com.blazebit.persistence.parser.expression.MapEntryExpression;
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
import com.blazebit.persistence.spi.ExtendedManagedType;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.ArrayList;
import java.util.List;
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
    protected final SplittingVisitor splittingVisitor;
    protected final List<Expression> splittedOffExpressions;
    protected PathExpression expressionToSplit;

    public EmbeddableSplittingVisitor(EntityMetamodel metamodel, SplittingVisitor splittingVisitor) {
        this.metamodel = metamodel;
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
        collectSplittedOffExpressions(expression);
        return splittedOffExpressions;
    }

    protected void collectSplittedOffExpressions(Expression expression) {
        splittedOffExpressions.clear();
        if (expressionToSplit != null) {
            PathReference pathReference = expressionToSplit.getPathReference();
            JoinNode baseNode = (JoinNode) pathReference.getBaseNode();
            ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getJavaType());
            Set<String> orderedAttributes = new TreeSet<>();
            for (SingularAttribute<?, ?> singularAttribute : managedType.getIdAttributes()) {
                EmbeddableType<?> embeddableType = (EmbeddableType<?>) singularAttribute.getType();
                Set<SingularAttribute<?, ?>> subAttributes = new TreeSet<>(ATTRIBUTE_NAME_COMPARATOR);
                subAttributes.addAll(embeddableType.getSingularAttributes());
                for (SingularAttribute<?, ?> attribute : subAttributes) {
                    addAttributes("", attribute, orderedAttributes);
                }
            }
            for (String orderedAttribute : orderedAttributes) {
                splittedOffExpressions.add(splittingVisitor.splitOff(expression, expressionToSplit, orderedAttribute));
            }
        }
    }

    @Override
    public Boolean visit(PathExpression expr) {
        PathReference pathReference = expr.getPathReference();
        JoinNode baseNode = (JoinNode) pathReference.getBaseNode();

        if (pathReference.getField() == null) {
            return true;
        }

        ExtendedManagedType<?> managedType = metamodel.getManagedType(ExtendedManagedType.class, baseNode.getJavaType());
        Attribute attr = managedType.getAttribute(pathReference.getField()).getAttribute();

        if (attr instanceof PluralAttribute<?, ?, ?>) {
            return true;
        }

        SingularAttribute<?, ?> singularAttr = (SingularAttribute<?, ?>) attr;

        int dotIndex = expr.getField().lastIndexOf('.');
        if (dotIndex == -1 && singularAttr.getType() instanceof EmbeddableType<?>) {
            expressionToSplit = expr;
        }

        return true;
    }

    protected void addAttributes(String prefix, SingularAttribute<?, ?> singularAttribute, Set<String> orderedAttributes) {
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
                addAttributes(newPrefix, attribute, orderedAttributes);
            }
        } else {
            orderedAttributes.add(attributeName);
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