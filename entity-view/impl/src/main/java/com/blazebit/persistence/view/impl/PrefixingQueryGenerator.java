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

package com.blazebit.persistence.view.impl;

import java.util.List;
import java.util.Objects;

import com.blazebit.persistence.parser.SimpleQueryGenerator;
import com.blazebit.persistence.parser.expression.PathElementExpression;
import com.blazebit.persistence.parser.expression.PathExpression;
import com.blazebit.persistence.parser.expression.PropertyExpression;
import com.blazebit.persistence.parser.expression.TreatExpression;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class PrefixingQueryGenerator extends SimpleQueryGenerator {
    
    private final String prefix;
    private final String aliasToSkip;
    private final String aliasToReplaceWithSkipAlias;
    private final String secondAliasToSkip;

    public PrefixingQueryGenerator(List<String> prefixParts) {
        this(prefixParts, null, null, null);
    }

    public PrefixingQueryGenerator(List<String> prefixParts, String aliasToSkip, String aliasToReplaceWithSkipAlias, String secondAliasToSkip) {
        StringBuilder prefixSb = new StringBuilder();
        
        if (prefixParts != null && !prefixParts.isEmpty()) {
            prefixSb.append(prefixParts.get(0));
            for (int i = 1; i < prefixParts.size(); i++) {
                prefixSb.append('.');
                prefixSb.append(prefixParts.get(i));
            }
        }
        
        this.prefix = prefixSb.toString();
        this.aliasToSkip = aliasToSkip;
        this.aliasToReplaceWithSkipAlias = Objects.equals(aliasToSkip, aliasToReplaceWithSkipAlias) ? null : aliasToReplaceWithSkipAlias;
        this.secondAliasToSkip = secondAliasToSkip;
    }

    @Override
    public void visit(PathExpression expression) {
        final List<PathElementExpression> expressions = expression.getExpressions();
        if (expressions.isEmpty()) {
            sb.append(prefix);
        } else {
            final PathElementExpression firstElement = expressions.get(0);
            final int size = expressions.size();
            // The second alias is skipped unconditionally
            if (size == 1 && secondAliasToSkip != null && firstElement instanceof PropertyExpression && secondAliasToSkip.equals(((PropertyExpression) firstElement).getProperty())) {
                super.visit(expression);
                return;
            }
            // This is something special to support the embedding_view macro in subviews where the parent view is correlated
            // Since the correlated alias will not be a prefix of the subview mapping prefix, we have to replace that properly
            if (aliasToReplaceWithSkipAlias != null && firstElement instanceof PropertyExpression && aliasToReplaceWithSkipAlias.equals(((PropertyExpression) firstElement).getProperty())) {
                sb.append(aliasToSkip);
                for (int i = 1; i < size; i++) {
                    sb.append(".");
                    expressions.get(i).accept(this);
                }
                return;
            }
            if (size > 1) {
                if (aliasToSkip != null && firstElement instanceof PropertyExpression && aliasToSkip.equals(((PropertyExpression) firstElement).getProperty())) {
                    super.visit(expression);
                    return;
                }
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
            if (firstElement instanceof PropertyExpression && "this".equalsIgnoreCase(((PropertyExpression) firstElement).getProperty())) {
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
