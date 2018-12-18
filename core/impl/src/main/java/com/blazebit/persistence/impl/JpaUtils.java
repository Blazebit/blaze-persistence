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
import com.blazebit.persistence.parser.PathTargetResolvingExpressionVisitor;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.NullExpression;
import com.blazebit.persistence.parser.expression.ParameterExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.spi.ExtendedAttribute;
import com.blazebit.persistence.spi.ExtendedManagedType;
import com.blazebit.persistence.spi.JoinTable;
import com.blazebit.persistence.spi.JpaMetamodelAccessor;
import com.blazebit.persistence.spi.JpaProvider;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class JpaUtils {

    private JpaUtils() {
    }

    private static boolean isBasicElementType(Attribute<?, ?> attribute) {
        return attribute instanceof PluralAttribute<?, ?, ?> && ((PluralAttribute<?, ?, ?>) attribute).getElementType().getPersistenceType() == Type.PersistenceType.BASIC;
    }

    public static void expandBindings(EntityType<?> bindType, Map<String, Integer> bindingMap, Map<String, String> columnBindingMap, Map<String, ExtendedAttribute<?, ?>> attributeEntries, ClauseType clause, AbstractCommonQueryBuilder<?, ?, ?, ?, ?> queryBuilder) {
        SelectManager<?> selectManager = queryBuilder.selectManager;
        JoinManager joinManager = queryBuilder.joinManager;
        ParameterManager parameterManager = queryBuilder.parameterManager;
        JpaProvider jpaProvider = queryBuilder.mainQuery.jpaProvider;
        EntityMetamodel metamodel = queryBuilder.mainQuery.metamodel;
        JpaMetamodelAccessor jpaMetamodelAccessor = jpaProvider.getJpaMetamodelAccessor();

        boolean needsElementCollectionIdCutoff = jpaProvider.needsElementCollectionIdCutoff();
        final Queue<String> attributeQueue = new ArrayDeque<>(bindingMap.keySet());
        while (!attributeQueue.isEmpty()) {
            final String attributeName = attributeQueue.remove();
            Integer tupleIndex = bindingMap.get(attributeName);

            final ExtendedAttribute<?, ?> attributeEntry = attributeEntries.get(attributeName);
            if (attributeEntry != null) {
                final List<Attribute<?, ?>> attributePath = attributeEntry.getAttributePath();
                final Attribute<?, ?> lastAttribute = attributePath.get(attributePath.size() - 1);
                boolean splitExpression = lastAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED;

                if (!splitExpression && jpaMetamodelAccessor.isJoinable(lastAttribute) && !isBasicElementType(lastAttribute)) {
                    splitExpression = true;
                    if (needsElementCollectionIdCutoff) {
                        for (int i = 0; i < attributePath.size() - 1; i++) {
                            Attribute<?, ?> attribute = attributePath.get(i);
                            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                                splitExpression = false;
                                break;
                            }
                        }
                    }
                }

                if (splitExpression) {
                    // We have to map *-to-one relationships to their id or unique props
                    // NOTE: Since we are talking about *-to-ones, the expression can only be a path to an object
                    // so it is safe to just append the id to the path
                    Expression selectExpression = selectManager.getSelectInfos().get(tupleIndex).getExpression();

                    // TODO: Maybe also allow Treat, Case-When, Array?
                    if (selectExpression instanceof NullExpression) {
                        // When binding null, we don't have to adapt anything
                    } else if (selectExpression instanceof PathExpression) {
                        boolean firstBinding = true;
                        final Collection<String> embeddedPropertyNames = getEmbeddedPropertyPaths(attributeEntries, attributeName, needsElementCollectionIdCutoff, false);

                        PathExpression baseExpression = embeddedPropertyNames.size() > 1 ?
                                ((PathExpression) selectExpression).clone(false) : ((PathExpression) selectExpression);

                        joinManager.implicitJoin(baseExpression, true, true, null, ClauseType.SELECT, new HashSet<String>(), false, false, false, false);

                        if (attributeEntry.getElementClass() != baseExpression.getPathReference().getType().getJavaType()) {
                            throw new IllegalStateException("An association should be bound to its association type and not its identifier type");
                        }

                        if (embeddedPropertyNames.size() > 0) {
                            bindingMap.remove(attributeName);
                            // We are going to insert the expanded attributes as new select items and shift existing ones
                            int delta = embeddedPropertyNames.size() - 1;
                            if (delta > 0) {
                                for (Map.Entry<String, Integer> entry : bindingMap.entrySet()) {
                                    if (entry.getValue() > tupleIndex) {
                                        entry.setValue(entry.getValue() + delta);
                                    }
                                }
                            }

                            int offset = 0;
                            for (String embeddedPropertyName : embeddedPropertyNames) {
                                PathExpression pathExpression = firstBinding ?
                                        ((PathExpression) selectExpression) : baseExpression.clone(false);

                                for (String propertyNamePart : embeddedPropertyName.split("\\.")) {
                                    pathExpression.getExpressions().add(new PropertyExpression(propertyNamePart));
                                }

                                String nestedAttributePath = attributeName + "." + embeddedPropertyName;
                                ExtendedAttribute<?, ?> nestedAttributeEntry = attributeEntries.get(nestedAttributePath);

                                // Process the nested attribute path recursively
                                attributeQueue.add(nestedAttributePath);

                                // Replace this binding in the binding map, additional selects need an updated index
                                bindingMap.put(nestedAttributePath, firstBinding ? tupleIndex : tupleIndex + offset);

                                if (!firstBinding) {
                                    selectManager.select(pathExpression, null, tupleIndex + offset);
                                } else {
                                    firstBinding = false;
                                }

                                for (String column : nestedAttributeEntry.getColumnNames()) {
                                    columnBindingMap.put(column, nestedAttributePath);
                                }
                                offset++;
                            }
                        }
                    } else if (selectExpression instanceof ParameterExpression) {
                        final Collection<String> embeddedPropertyNames = getEmbeddedPropertyPaths(attributeEntries, attributeName, jpaProvider.needsElementCollectionIdCutoff(), false);

                        if (embeddedPropertyNames.size() > 0) {
                            ParameterExpression parameterExpression = (ParameterExpression) selectExpression;
                            String parameterName = parameterExpression.getName();
                            Map<String, List<String>> parameterAccessPaths = new HashMap<>(embeddedPropertyNames.size());
                            ParameterValueTransformer tranformer = parameterManager.getParameter(parameterName).getTranformer();
                            if (tranformer instanceof SplittingParameterTransformer) {
                                for (String name : ((SplittingParameterTransformer) tranformer).getParameterNames()) {
                                    parameterManager.unregisterParameterName(name, clause, queryBuilder);
                                }
                            }

                            selectManager.getSelectInfos().remove(tupleIndex.intValue());
                            bindingMap.remove(attributeName);
                            // We are going to insert the expanded attributes as new select items and shift existing ones
                            int delta = embeddedPropertyNames.size() - 1;
                            if (delta > 0) {
                                for (Map.Entry<String, Integer> entry : bindingMap.entrySet()) {
                                    if (entry.getValue() > tupleIndex) {
                                        entry.setValue(entry.getValue() + delta);
                                    }
                                }
                            }

                            int offset = 0;
                            for (String embeddedPropertyName : embeddedPropertyNames) {
                                String subParamName = "_" + parameterName + "_" + embeddedPropertyName.replace('.', '_');
                                parameterManager.registerParameterName(subParamName, false, clause, queryBuilder);
                                parameterAccessPaths.put(subParamName, Arrays.asList(embeddedPropertyName.split("\\.")));

                                String nestedAttributePath = attributeName + "." + embeddedPropertyName;
                                ExtendedAttribute<?, ?> nestedAttributeEntry = attributeEntries.get(nestedAttributePath);

                                // Process the nested attribute path recursively
                                attributeQueue.add(nestedAttributePath);

                                // Replace this binding in the binding map, additional selects need an updated index
                                bindingMap.put(nestedAttributePath, tupleIndex + offset);
                                selectManager.select(new ParameterExpression(subParamName), null, tupleIndex + offset);

                                for (String column : nestedAttributeEntry.getColumnNames()) {
                                    columnBindingMap.put(column, nestedAttributePath);
                                }
                                offset++;
                            }

                            parameterManager.getParameter(parameterName).setTranformer(new SplittingParameterTransformer(parameterManager, metamodel, attributeEntry.getElementClass(), parameterAccessPaths));
                        }
                    } else {
                        throw new IllegalArgumentException("Illegal expression '" + selectExpression.toString() + "' for binding relation '" + attributeName + "'!");
                    }
                }
            }
        }
    }

    public static Collection<String> getEmbeddedPropertyPaths(Map<String, ExtendedAttribute<?, ?>> attributeEntries, String attributeName, boolean needsElementCollectionIdCutoff, boolean filterCollections) {
        final NavigableSet<String> embeddedPropertyNames = new TreeSet<>();
        String prefix = attributeName == null ? "" : attributeName + ".";
        int dotCount = -1;
        int dotIndex = -1;
        do {
            dotCount++;
            dotIndex = prefix.indexOf('.', dotIndex + 1);
        } while (dotIndex != -1);

        OUTER: for (Map.Entry<String, ExtendedAttribute<?, ?>> entry : attributeEntries.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                if (filterCollections) {
                    List<Attribute<?, ?>> attributePath = entry.getValue().getAttributePath();
                    for (int i = dotCount; i < attributePath.size(); i++) {
                        if (attributePath.get(i).isCollection()) {
                            continue OUTER;
                        }
                    }
                }
                String subAttribute = entry.getKey().substring(prefix.length());
                String lower = embeddedPropertyNames.lower(subAttribute);
                if (lower == null) {
                    String higher = embeddedPropertyNames.higher(subAttribute);
                    if (higher == null || !higher.startsWith(subAttribute + ".")) {
                        embeddedPropertyNames.add(subAttribute);
                    }
                } else {
                    if (subAttribute.startsWith(lower + ".")) {
                        embeddedPropertyNames.remove(lower);
                    }
                    if (!lower.startsWith(subAttribute + ".")) {
                        String higher = embeddedPropertyNames.higher(subAttribute);
                        if (higher == null || !higher.startsWith(subAttribute + ".")) {
                            embeddedPropertyNames.add(subAttribute);
                        }
                    }
                }
            }
        }
        // Remove the embeddable itself since it was split up
        if (attributeName != null) {
            embeddedPropertyNames.remove(attributeName);
            // Hibernate has a bug in the handling of the deep property named "id" when being part of an element collection alias, so we cut it off
            if (needsElementCollectionIdCutoff && attributeEntries.get(attributeName).getAttribute().getPersistentAttributeType() == Attribute.PersistentAttributeType.ELEMENT_COLLECTION) {
                Iterator<String> iterator = embeddedPropertyNames.iterator();
                List<String> addProperties = new ArrayList<>();
                while (iterator.hasNext()) {
                    String property = iterator.next();
                    if (property.endsWith(".id")) {
                        iterator.remove();
                        addProperties.add(property.substring(0, property.length() - ".id".length()));
                    }
                }
                embeddedPropertyNames.addAll(addProperties);
            }
        }
        return embeddedPropertyNames;
    }

    public static Map<String, ExtendedAttribute<?, ?>> getCollectionAttributeEntries(EntityMetamodel metamodel, EntityType<?> entityType, ExtendedAttribute<?, ?> attribute) {
        Map<String, ExtendedAttribute<?, ?>> collectionAttributeEntries = new HashMap<>();
        JoinTable joinTable = attribute.getJoinTable();
        if (joinTable == null) {
            throw new IllegalArgumentException("No support for inserting into inverse collection via DML API yet!");
        }

        ExtendedManagedType<?> extendedManagedType = metamodel.getManagedType(ExtendedManagedType.class, entityType);
        for (String idAttributeName : joinTable.getIdAttributeNames()) {
            collectionAttributeEntries.put(idAttributeName, extendedManagedType.getAttribute(idAttributeName));
        }
        if (((PluralAttribute<?, ?, ?>) attribute.getAttribute()).getElementType() instanceof ManagedType<?>) {
            String prefix = attribute.getAttributePathString() + ".";
            for (Map.Entry<String, ? extends ExtendedAttribute<?, ?>> entry : extendedManagedType.getAttributes().entrySet()) {
                if (entry.getKey().startsWith(prefix)) {
                    collectionAttributeEntries.put(entry.getKey(), entry.getValue());
                }
            }
        }

        collectionAttributeEntries.put(attribute.getAttributePathString(), attribute);
        return collectionAttributeEntries;
    }

    public static AttributeHolder getAttributeForJoining(EntityMetamodel metamodel, PathExpression expression) {
        JoinNode expressionBaseNode = ((JoinNode) expression.getPathReference().getBaseNode());
        String firstElementString = expression.getExpressions().get(0).toString();
        String baseNodeAlias;
        JoinNode baseNode = expressionBaseNode;

        do {
            baseNodeAlias = baseNode.getAlias();
        } while (!firstElementString.equals(baseNodeAlias) && (baseNode = baseNode.getParent()) != null);

        if (baseNode == null) {
            baseNodeAlias = null;
            if (expressionBaseNode.getParent() == null) {
                baseNode = expressionBaseNode;
            } else {
                baseNode = expressionBaseNode.getParent();
            }
        }

        return getAttributeForJoining(metamodel, baseNode.getNodeType(), expression, baseNodeAlias);
    }

    public static AttributeHolder getAttributeForJoining(EntityMetamodel metamodel, Expression resolvedExpression) {
        return getAttributeForJoining(metamodel, null, resolvedExpression, null);
    }

    public static AttributeHolder getAttributeForJoining(EntityMetamodel metamodel, Type<?> baseNodeType, Expression joinExpression, String baseNodeAlias) {
        PathTargetResolvingExpressionVisitor visitor = new PathTargetResolvingExpressionVisitor(metamodel, baseNodeType, baseNodeAlias);
        joinExpression.accept(visitor);
        Map<Attribute<?, ?>, Type<?>> possibleTargets = visitor.getPossibleTargets();
        if (possibleTargets.size() > 1) {
            throw new IllegalArgumentException("Multiple possible target types for expression: " + joinExpression);
        }

        Map.Entry<Attribute<?, ?>, Type<?>> entry = possibleTargets.entrySet().iterator().next();
        return new AttributeHolder(entry.getKey(), entry.getValue());
    }
}
