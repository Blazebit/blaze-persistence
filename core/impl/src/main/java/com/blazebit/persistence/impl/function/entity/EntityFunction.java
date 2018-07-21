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

package com.blazebit.persistence.impl.function.entity;

import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class EntityFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "entity_function";

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return true;
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return firstArgumentType;
    }

    @Override
    public void render(FunctionRenderContext functionRenderContext) {
        if (functionRenderContext.getArgumentsSize() == 0) {
            throw new RuntimeException("The ENTITY_FUNCTION function needs at least one argument <sub_query>! args=" + functionRenderContext);
        }

        // com.blazebit.persistence.impl.JoinManager.buildClause adds synthetic where clause conjuncts and starts with 1=1 so we can determine it properly
        // We remove the synthetic predicate here and later extract the values clause alias of it so we can insert the proper SQL values clause at the right place
        String subquery = functionRenderContext.getArgument(0);
        StringBuilder sb = new StringBuilder();
        int subqueryEndIndex = subquery.lastIndexOf(" and 1=1");
        int aliasEndIndex = subquery.indexOf('.', subqueryEndIndex) ;
        int aliasStartIndex = aliasEndIndex - 1;
        while (aliasStartIndex > subqueryEndIndex) {
            if (!SqlUtils.isIdentifier(subquery.charAt(aliasStartIndex))) {
                aliasStartIndex++;
                break;
            }
            aliasStartIndex--;
        }

        sb.append(subquery, 1, subqueryEndIndex);
        String entityName = unquote(functionRenderContext.getArgument(1));
        String valuesClause = unquote(functionRenderContext.getArgument(2));
        String valuesAliases = unquote(functionRenderContext.getArgument(3));
        String syntheticPredicate = unquote(functionRenderContext.getArgument(4));
        String exampleQuerySqlAlias = syntheticPredicate.substring(0, syntheticPredicate.indexOf('.'));
        String valuesTableSqlAlias = subquery.substring(aliasStartIndex, aliasEndIndex);

        syntheticPredicate = syntheticPredicate.replace(exampleQuerySqlAlias, valuesTableSqlAlias);

        // TODO: this is a hibernate specific integration detail
        // Replace the subview subselect that is generated for this subselect
        final String subselect = "( select * from " + entityName + " )";
        int subselectIndex = sb.indexOf(subselect, 0);
        if (subselectIndex == -1) {
            // this is probably a VALUES clause for an entity type
            int syntheticPredicateStart = sb.indexOf(syntheticPredicate, SqlUtils.indexOfWhere(sb));
            sb.replace(syntheticPredicateStart, syntheticPredicateStart + syntheticPredicate.length(), "1=1");
        } else {
            while ((subselectIndex = sb.indexOf(subselect, subselectIndex)) > -1) {
                int endIndex = subselectIndex + subselect.length();
                int syntheticPredicateStart = sb.indexOf(syntheticPredicate, endIndex);
                sb.replace(syntheticPredicateStart, syntheticPredicateStart + syntheticPredicate.length(), "1=1");
                sb.replace(subselectIndex, endIndex, entityName);
            }
        }
        SqlUtils.applyTableNameRemapping(sb, valuesTableSqlAlias, valuesClause, valuesAliases);
        functionRenderContext.addChunk("(");
        functionRenderContext.addChunk(sb.toString());
        functionRenderContext.addChunk(")");
    }

    private static String unquote(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        boolean quote = false;
        for (int i = 1; i < s.length() - 1; i++) {
            final char c = s.charAt(i);
            if (quote) {
                quote = false;
                if (c != '\'') {
                    sb.append('\'');
                }
                sb.append(c);
            } else {
                if (c == '\'') {
                    quote = true;
                } else {
                    sb.append(c);
                }
            }
        }
        if (quote) {
            sb.append('\'');
        }
        return sb.toString();
    }

}
