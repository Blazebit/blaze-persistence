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

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.FetchType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

import com.blazebit.persistence.parser.EntityMetamodel;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;

/**
 * This visitor resolves entity references to their attributes. This is needed for entity references
 * in the select clause when used in combination with aggregate functions. We have to decompose the
 * entity and add the components to the group by because all components will end up in the select clause.
 * Only until grouping by entities is resolved: https://hibernate.atlassian.net/browse/HHH-1615
 * 
 * @author Christian Beikov
 * @since 1.0.5
 */
public class EntitySelectResolveVisitor extends VisitorAdapter {

    private final EntityMetamodel m;
    private final Set<PathExpression> pathExpressions;

    public EntitySelectResolveVisitor(EntityMetamodel m, Set<PathExpression> pathExpressions) {
        this.m = m;
        this.pathExpressions = pathExpressions;
    }

    @Override
    public void visit(FunctionExpression expression) {
        // Skip all functions in here
    }

    @Override
    public void visit(PathExpression expression) {
        if (expression.getField() == null) {
            /**
             * We need to resolve entity selects because hibernate will
             * select every entity attribute. Since we need every select in
             * the group by (because of DB2) we need to resolve such entity
             * selects here
             */
            JoinNode baseNode = ((JoinNode) expression.getBaseNode());
            EntityType<?> entityType = m.getEntity(baseNode.getJavaType());
            if (entityType == null) {
                // ignore if the expression is not an entity
                return;
            }

            Class<?> entityClass = entityType.getJavaType();
            // we need to ensure a deterministic order for testing
            SortedSet<Attribute<?, ?>> sortedAttributes = new TreeSet<>(JpaMetamodelUtils.ATTRIBUTE_NAME_COMPARATOR);
            // TODO: a polymorphic query will fail because we don't collect subtype properties
            sortedAttributes.addAll(entityType.getAttributes());
            for (Attribute<?, ?> attr : sortedAttributes) {
                boolean resolve = false;
                if (ExpressionUtils.isAssociation(attr) && !attr.isCollection()) {
                    resolve = true;
                } else if (ExpressionUtils.getFetchType(attr) == FetchType.EAGER) {
                    if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                        throw new UnsupportedOperationException("Eager element collections are not supported");
                    }
                    resolve = true;
                }

                if (resolve) {
                    PathExpression attrPath = new PathExpression(new ArrayList<PathElementExpression>(expression.getExpressions()));
                    attrPath.setPathReference(new SimplePathReference(baseNode, attr.getName(), m.type(JpaMetamodelUtils.resolveFieldClass(entityClass, attr))));
                    pathExpressions.add(attrPath);
                }
            }
        }
    }

    /* Skip the expressions that are not really entity expressions */

    @Override
    public void visit(ListIndexExpression expression) {
    }

    @Override
    public void visit(MapEntryExpression expression) {
    }

    @Override
    public void visit(MapKeyExpression expression) {
    }

    @Override
    public void visit(MapValueExpression expression) {
    }

}