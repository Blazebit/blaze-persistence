/*
 * Copyright 2014 - 2020 Blazebit.
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
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.ListIndexExpression;
import com.blazebit.persistence.parser.expression.MapEntryExpression;
import com.blazebit.persistence.parser.expression.MapKeyExpression;
import com.blazebit.persistence.parser.expression.MapValueExpression;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.VisitorAdapter;
import com.blazebit.persistence.parser.util.JpaMetamodelUtils;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.FetchType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final JpaProvider jpaProvider;
    private final Set<PathExpression> pathExpressions;

    private JoinNode rootNode;

    public EntitySelectResolveVisitor(EntityMetamodel m, JpaProvider jpaProvider, Set<PathExpression> pathExpressions) {
        this.m = m;
        this.jpaProvider = jpaProvider;
        this.pathExpressions = pathExpressions;
    }

    public JoinNode getRootNode() {
        return rootNode;
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
            rootNode = ((JoinNode) expression.getBaseNode());
            if (rootNode == null) {
                // This is an alias expression to a complex expression
                return;
            }
            EntityType<?> entityType = m.getEntity(rootNode.getJavaType());
            if (entityType == null) {
                // ignore if the expression is not an entity
                return;
            }

            // TODO: a polymorphic query will fail because we don't collect subtype properties
            ExtendedManagedType<?> extendedManagedType = m.getManagedType(ExtendedManagedType.class, entityType);
            Set<String> attributePaths;
            if (rootNode.getValuesIdNames() != null && !rootNode.getValuesIdNames().isEmpty()) {
                attributePaths = rootNode.getValuesIdNames();
            } else {
                attributePaths = Collections.emptySet();
            }
            Map<String, ExtendedAttribute<?, ?>> ownedSingularAttributes = (Map<String, ExtendedAttribute<?, ?>>) (Map) extendedManagedType.getOwnedSingularAttributes();
            Collection<String> propertyPaths = JpaUtils.getEmbeddedPropertyPaths(ownedSingularAttributes, null, jpaProvider.needsElementCollectionIdCutoff(), true);
            for (String propertyPath : propertyPaths) {
                // Skip if the attribute is not in the desired attribute paths
                if (!attributePaths.isEmpty() && !attributePaths.contains(propertyPath)) {
                    continue;
                }
                ExtendedAttribute<?, ?> extendedAttribute = ownedSingularAttributes.get(propertyPath);
                Attribute<?, ?> attr = extendedAttribute.getAttribute();
                boolean resolve = false;
                if (JpaMetamodelUtils.isAssociation(attr) && !attr.isCollection()) {
                    resolve = true;
                } else if (ExpressionUtils.getFetchType(attr) == FetchType.EAGER) {
                    if (attr.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                        throw new UnsupportedOperationException("Eager element collections are not supported");
                    }
                    resolve = true;
                }

                if (resolve) {
                    List<PathElementExpression> paths = new ArrayList<>(expression.getExpressions().size() + 1);
                    paths.addAll(expression.getExpressions());
                    for (Attribute<?, ?> attribute : extendedAttribute.getAttributePath()) {
                        paths.add(new PropertyExpression(attribute.getName()));
                    }
                    PathExpression attrPath = new PathExpression(paths);
                    attrPath.setPathReference(new SimplePathReference(rootNode, propertyPath, m.type(extendedAttribute.getElementClass())));
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