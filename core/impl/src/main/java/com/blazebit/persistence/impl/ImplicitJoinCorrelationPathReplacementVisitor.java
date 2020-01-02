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

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.InplaceModificationResultVisitorAdapter;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.predicate.EqPredicate;
import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * This visitor gathers information about supposed invalid implicit joins in the ON clause
 * to be able to rewrite the predicate to EXISTS subqueries.
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class ImplicitJoinCorrelationPathReplacementVisitor extends InplaceModificationResultVisitorAdapter {

    private final Map<PathExpression, CorrelationTransformEntry> pathIdentitiesToCorrelate;
    private final Map<String, CorrelationTransformEntry> pathsToCorrelate;
    private final Map<String, RootCorrelationEntry> rootsToCorrelate;

    public ImplicitJoinCorrelationPathReplacementVisitor() {
        this.pathIdentitiesToCorrelate = new IdentityHashMap<>();
        this.pathsToCorrelate = new HashMap<>();
        this.rootsToCorrelate = new HashMap<>();
    }

    public void addPathExpression(PathExpression pathExpression, ImplicitJoinNotAllowedException ex, boolean isInConjunction) {
        if (!pathIdentitiesToCorrelate.containsKey(pathExpression)) {
            StringBuilder sb = new StringBuilder();
            if (ex.getTreatType() != null) {
                sb.append("TREAT(");
            }

            if (isInConjunction) {
                ex.getBaseNode().appendAlias(sb, true, false);
            } else {
                String syntheticAlias = "_synthetic_" + ex.getBaseNode().getAlias();
                List<PathElementExpression> list1 = new ArrayList<>(1);
                list1.add(new PropertyExpression(syntheticAlias));
                List<PathElementExpression> list2 = new ArrayList<>(1);
                list2.add(new PropertyExpression(ex.getBaseNode().getAlias()));
                Predicate predicate = new EqPredicate(new PathExpression(list1), new PathExpression(list2));
                rootsToCorrelate.put(syntheticAlias, new RootCorrelationEntry(syntheticAlias, ex.getBaseNode().getNodeType().getJavaType(), predicate));
                sb.append(syntheticAlias);
            }

            sb.append('.');
            sb.append(ex.getJoinRelationName());

            if (ex.getTreatType() != null) {
                sb.append(" AS ").append(ex.getTreatType()).append(')');
            }
            String correlationExpression = sb.toString();

            String alias;
            CorrelationTransformEntry existingEntry = pathsToCorrelate.get(correlationExpression);
            if (existingEntry != null) {
                alias = existingEntry.alias;
            } else {
                alias = "_synth_subquery_" + pathIdentitiesToCorrelate.size();
            }

            PathExpression transformedExpression = pathExpression.clone(false);
            // For now, we only support 2 levels of treats, but this obviously should be improved
            if (transformedExpression.getExpressions().get(0) instanceof TreatExpression) {
                // Only the first expression in the list can be a treat expression
                TreatExpression treatExpression = (TreatExpression) transformedExpression.getExpressions().get(0);
                if (treatExpression.getType().equals(ex.getTreatType())) {
                    // The not-allowed implicit join is the one within the treat
                    PathExpression pathExpressionToModify = (PathExpression) treatExpression.getExpression();
                    removeMatchingJoinAttributePathElements(ex, pathExpressionToModify);
                    if (pathExpressionToModify.getExpressions().isEmpty()) {
                        transformedExpression.getExpressions().set(0, new PropertyExpression(alias));
                    } else {
                        throw new IllegalArgumentException("Can't transform nested TREAT expression: " + pathExpression);
                    }
                } else {
                    // The not-allowed implicit join happens after dereferencing the treat
                    PathExpression pathExpressionToModify = (PathExpression) treatExpression.getExpression();
                    // Usually, this will always mean the treat is a root treat
                    // If the root treat is an up-cast, JoinNode#getTreatType will be null
                    // If it's a proper treat, it must match the treat expressions treat type
                    // In addition, we also need to make sure the root treat alias matches the not-allowed implicit joins base node alias
                    if ((ex.getBaseNode().getTreatType() == null && ex.getBaseNode().getEntityType().getName().equals(treatExpression.getType())
                            || ex.getBaseNode().getTreatType() != null && ex.getBaseNode().getTreatType().getName().equals(treatExpression.getType()))
                            && pathExpressionToModify.getExpressions().size() == 1 && ex.getBaseNode().getAlias().equals(pathExpressionToModify.getExpressions().get(0).toString())) {
                        // Root treat
                        removeMatchingJoinAttributePathElements(ex, transformedExpression);
                        transformedExpression.getExpressions().add(0, new PropertyExpression(alias));
                    } else {
                        removeMatchingJoinAttributePathElements(ex, pathExpressionToModify);
                        pathExpressionToModify.getExpressions().add(0, new PropertyExpression(alias));
                    }
                }
            } else {
                removeMatchingJoinAttributePathElements(ex, transformedExpression);
                transformedExpression.getExpressions().add(0, new PropertyExpression(alias));
            }
            CorrelationTransformEntry correlationTransformEntry = new CorrelationTransformEntry(alias, correlationExpression, transformedExpression, isInConjunction);
            pathIdentitiesToCorrelate.put(pathExpression, correlationTransformEntry);
            if (existingEntry == null) {
                pathsToCorrelate.put(correlationExpression, correlationTransformEntry);
            }
        }
    }

    private void removeMatchingJoinAttributePathElements(ImplicitJoinNotAllowedException ex, PathExpression pathExpressionToModify) {
        // We remove the matching prefix
        ListIterator<PathElementExpression> pathElementExpressionListIterator = pathExpressionToModify.getExpressions().listIterator();
        Iterator<String> joinRelationAttributesIterator = ex.getJoinRelationAttributes().iterator();
        String joinRelationAttribute = joinRelationAttributesIterator.next();
        while (pathElementExpressionListIterator.hasNext() && joinRelationAttribute != null) {
            PathElementExpression elementExpression = pathElementExpressionListIterator.next();
            pathElementExpressionListIterator.remove();
            if (joinRelationAttribute.equals(elementExpression.toString())) {
                joinRelationAttribute = joinRelationAttributesIterator.hasNext() ? joinRelationAttributesIterator.next() : null;
            }
        }
    }

    public Collection<CorrelationTransformEntry> getPathsToCorrelate() {
        return pathsToCorrelate.values();
    }

    public Collection<RootCorrelationEntry> getRootsToCorrelate() {
        return rootsToCorrelate.values();
    }

    public Predicate rewritePredicate(Predicate predicate) {
        Predicate newPredicate = (Predicate) predicate.accept(this);
        pathIdentitiesToCorrelate.clear();
        pathsToCorrelate.clear();
        rootsToCorrelate.clear();
        return newPredicate;
    }

    @Override
    public Expression visit(PathExpression expression) {
        CorrelationTransformEntry entry = pathIdentitiesToCorrelate.get(expression);
        if (entry == null) {
            return super.visit(expression);
        } else {
            return entry.transformedExpression;
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.3.0
     */
    public static class CorrelationTransformEntry {

        private final String alias;
        private final String correlationExpression;
        private final PathExpression transformedExpression;
        private final boolean isInConjunction;

        public CorrelationTransformEntry(String alias, String correlationExpression, PathExpression transformedExpression, boolean isInConjunction) {
            this.alias = alias;
            this.correlationExpression = correlationExpression;
            this.transformedExpression = transformedExpression;
            this.isInConjunction = isInConjunction;
        }

        public String getAlias() {
            return alias;
        }

        public String getCorrelationExpression() {
            return correlationExpression;
        }

        public PathExpression getTransformedExpression() {
            return transformedExpression;
        }

        public boolean isInConjunction() {
            return isInConjunction;
        }
    }

    /**
     *
     * @author Christian Beikov
     * @since 1.4.0
     */
    public static class RootCorrelationEntry {

        private final String alias;
        private final Class<?> entityClass;
        private final Predicate additionalPredicate;

        public RootCorrelationEntry(String alias, Class<?> entityClass, Predicate additionalPredicate) {
            this.alias = alias;
            this.entityClass = entityClass;
            this.additionalPredicate = additionalPredicate;
        }

        public String getAlias() {
            return alias;
        }

        public Class<?> getEntityClass() {
            return entityClass;
        }

        public Predicate getAdditionalPredicate() {
            return additionalPredicate;
        }
    }

}
