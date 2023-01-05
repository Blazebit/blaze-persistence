/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.ArrayExpression;
import com.blazebit.persistence.parser.expression.EntityLiteral;
import com.blazebit.persistence.parser.expression.Expression;
import com.blazebit.persistence.parser.expression.ExpressionFactory;
import com.blazebit.persistence.parser.expression.FunctionExpression;
import com.blazebit.persistence.parser.expression.MacroFunction;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.StringLiteral;
import com.blazebit.persistence.parser.expression.TreatExpression;
import com.blazebit.persistence.parser.predicate.Predicate;
import com.blazebit.persistence.parser.util.ExpressionUtils;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryEmbeddingViewJpqlMacro;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class PrefixingQueryGenerator extends SimpleQueryGenerator {

    private static final Set<String> DEFAULT_QUERY_ALIASES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(CorrelatedSubqueryEmbeddingViewJpqlMacro.CORRELATION_EMBEDDING_VIEW_ALIAS, CorrelatedSubqueryViewRootJpqlMacro.CORRELATION_VIEW_ROOT_ALIAS)));
    private static final Set<String> MACROS = new HashSet<>(Arrays.asList("VIEW", "VIEW_ROOT", "EMBEDDING_VIEW"));

    private final ExpressionFactory expressionFactory;
    private final String prefix;
    private final String substitute;
    private final String aliasToReplace;
    private final Set<String> queryAliases;
    private final boolean expandMacros;
    private final boolean fromEmbeddingViewScope;
    private boolean doPrefix = true;

    public PrefixingQueryGenerator(ExpressionFactory ef, String prefix, String substitute, String aliasToReplace, Set<String> queryAliases, boolean expandMacros, boolean fromEmbeddingViewScope) {
        this.expressionFactory = ef;
        this.prefix = prefix;
        this.substitute = substitute;
        this.expandMacros = expandMacros;
        this.fromEmbeddingViewScope = fromEmbeddingViewScope;
        this.aliasToReplace = aliasToReplace;
        this.queryAliases = new HashSet<>(queryAliases);
        this.queryAliases.addAll(DEFAULT_QUERY_ALIASES);
    }

    public static String prefix(ExpressionFactory ef, Expression expression, String prefix, Set<String> queryAliases, boolean fromEmbeddingViewScope) {
        SimpleQueryGenerator generator = new PrefixingQueryGenerator(ef, prefix, null, null, queryAliases, !fromEmbeddingViewScope, fromEmbeddingViewScope);
        StringBuilder sb = new StringBuilder(20 + prefix.length());
        generator.setQueryBuffer(sb);
        expression.accept(generator);
        return sb.toString();
    }

    @Override
    public void visit(ArrayExpression expression) {
        expression.getBase().accept(this);
        sb.append('[');
        if (expression.getIndex() instanceof Predicate) {
            boolean doPrefix = this.doPrefix;
            try {
                this.doPrefix = false;
                expression.getIndex().accept(this);
            } finally {
                this.doPrefix = doPrefix;
            }
        } else {
            expression.getIndex().accept(this);
        }
        sb.append(']');
    }

    @Override
    public void visit(FunctionExpression expression) {
        String macroName;
        List<Expression> expressions = expression.getExpressions();
        if (ExpressionUtils.isCustomFunctionInvocation(expression) && MACROS.contains((macroName = ((StringLiteral) expressions.get(0)).getValue()).toUpperCase())) {
            if (expandMacros) {
                MacroFunction macroFunction = expressionFactory.getDefaultMacroConfiguration().get(macroName);
                Expression expandedExpression = macroFunction.apply(expressions.subList(1, expressions.size()));
                // It makes no sense to prefix a path that was created through a view macro as that is already absolute
                boolean doPrefix = this.doPrefix;
                try {
                    this.doPrefix = false;
                    expandedExpression.accept(this);
                } finally {
                    this.doPrefix = doPrefix;
                }
            } else {
                // When invoking this in the embedding view scope, we have to redirect VIEW to EMBEDDING_VIEW and unregister EMBEDDING_VIEW
                if (fromEmbeddingViewScope) {
                    if ("EMBEDDING_VIEW".equalsIgnoreCase(macroName)) {
                        throw new IllegalArgumentException("Illegal use of EMBEDDING_VIEW!");
                    } else if ("VIEW".equalsIgnoreCase(macroName)) {
                        sb.append("EMBEDDING_VIEW");
                    } else {
                        sb.append(macroName);
                    }
                } else {
                    sb.append(macroName);
                }
                sb.append('(');
                int size = expressions.size();
                if (size > 1) {
                    expressions.get(1).accept(this);
                    for (int i = 2; i < size; i++) {
                        sb.append(",");
                        expressions.get(i).accept(this);
                    }
                }
                sb.append(')');
            }
        } else {
            super.visit(expression);
        }
    }

    @Override
    public void visit(PathExpression expression) {
        final List<PathElementExpression> expressions = expression.getExpressions();
        if (expressions.isEmpty()) {
            sb.append(prefix);
        } else {
            final PathElementExpression firstElement = expressions.get(0);
            final String firstProperty = firstElement instanceof PropertyExpression ? ((PropertyExpression) firstElement).getProperty() : null;
            final int size = expressions.size();
            // If we encounter a query alias, we don't prefix
            if (!doPrefix || queryAliases.contains(firstProperty)) {
                super.visit(expression);
                return;
            }
            // This is something special to support the embedding_view macro in subviews where the parent view is correlated
            // Since the correlated alias will not be a prefix of the subview mapping prefix, we have to replace that properly
            if (aliasToReplace != null && aliasToReplace.equals(firstProperty)) {
                sb.append(substitute);
                for (int i = 1; i < size; i++) {
                    sb.append(".");
                    expressions.get(i).accept(this);
                }
                return;
            }
            if (firstElement instanceof ArrayExpression && ((ArrayExpression) firstElement).getBase() instanceof EntityLiteral) {
                super.visit(expression);
                return;
            }
            // Prefixing will only be done for the inner most path, but not the treat expression
            if (!(firstElement instanceof TreatExpression)) {
                String expressionString = expression.toString();
                // Find out the common prefix
                int dotIndex = -1;
                int length = Math.min(prefix.length(), expressionString.length());
                for (int i = 0; i < length; i++) {
                    if (prefix.charAt(i) != expressionString.charAt(i)) {
                        for (;i > 0; i--) {
                            if (prefix.charAt(i) == '.' && expressionString.charAt(i) == '.') {
                                dotIndex = i;
                                break;
                            }
                        }
                        break;
                    }
                }

                if (dotIndex != -1 || prefix.isEmpty()
                        || expressionString.length() < prefix.length() && prefix.charAt(expressionString.length()) == '.' && prefix.startsWith(expressionString)
                        || expressionString.length() > prefix.length() && expressionString.charAt(prefix.length()) == '.' && expressionString.startsWith(prefix)
                        || expressionString.equals(prefix)
                    ) {
                    // We only do prefixing if the expressions have a non-common base to avoid prefixing already prefixed expressions
                    // If both point to an alias, dotIndex will be -1 but then the expressions would have the same length
                    // If we have a dotIndex, we know there is a common base, so just skip prefixing
                } else {
                    sb.append(prefix);
                    sb.append('.');
                }
            }
            // Remove the this property
            if ("this".equalsIgnoreCase(firstProperty)) {
                if (size > 1) {
                    for (int i = 1; i < size; i++) {
                        expressions.get(i).accept(this);
                    }
                } else {
                    sb.setLength(sb.length() - 1);
                }
            } else {
                super.visit(expression);
            }
        }
    }

}
