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

import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.InplaceModificationResultVisitorAdapter;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.predicate.Predicate;

import java.util.IdentityHashMap;
import java.util.Iterator;
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

    private final Map<PathExpression, CorrelationTransformEntry> pathsToCorrelate;

    public ImplicitJoinCorrelationPathReplacementVisitor() {
        this.pathsToCorrelate = new IdentityHashMap<>();
    }

    public void addPathExpression(PathExpression pathExpression, ImplicitJoinNotAllowedException ex) {
        if (!pathsToCorrelate.containsKey(pathExpression)) {
            StringBuilder sb = new StringBuilder();
            if (ex.getTreatType() != null) {
                sb.append("TREAT(");
            }

            ex.getBaseNode().appendDeReference(sb, ex.getJoinRelationName(), true, false, false);

            if (ex.getTreatType() != null) {
                sb.append(" AS ").append(ex.getTreatType()).append(')');
            }
            String correlationExpression = sb.toString();
            String alias = "_synth_subquery_" + pathsToCorrelate.size();
            PathExpression transformedExpression = pathExpression.clone(false);
            // For now, we only support 2 levels of treats, but this obviously should be improved
            if (transformedExpression.getExpressions().get(0) instanceof TreatExpression) {
                TreatExpression treatExpression = (TreatExpression) transformedExpression.getExpressions().get(0);
                if (treatExpression.getType().equals(ex.getTreatType())) {
                    PathExpression pathExpressionToModify = (PathExpression) treatExpression.getExpression();
                    removeMatchingJoinAttributePathElements(ex, pathExpressionToModify);
                    if (pathExpressionToModify.getExpressions().isEmpty()) {
                        transformedExpression.getExpressions().set(0, new PropertyExpression(alias));
                    } else {
                        throw new IllegalArgumentException("Can't transform nested TREAT expression: " + pathExpression);
                    }
                } else {
                    PathExpression pathExpressionToModify = (PathExpression) treatExpression.getExpression();
                    if (ex.getBaseNode().getTreatType() != null && ex.getBaseNode().getTreatType().getName().equals(treatExpression.getType())
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
            pathsToCorrelate.put(pathExpression, new CorrelationTransformEntry(alias, correlationExpression, transformedExpression));
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

    public Map<PathExpression, CorrelationTransformEntry> getPathsToCorrelate() {
        return pathsToCorrelate;
    }

    public Predicate rewritePredicate(Predicate predicate) {
        Predicate newPredicate = (Predicate) predicate.accept(this);
        pathsToCorrelate.clear();
        return newPredicate;
    }

    @Override
    public Expression visit(PathExpression expression) {
        CorrelationTransformEntry entry = pathsToCorrelate.get(expression);
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

        public CorrelationTransformEntry(String alias, String correlationExpression, PathExpression transformedExpression) {
            this.alias = alias;
            this.correlationExpression = correlationExpression;
            this.transformedExpression = transformedExpression;
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
    }

}
