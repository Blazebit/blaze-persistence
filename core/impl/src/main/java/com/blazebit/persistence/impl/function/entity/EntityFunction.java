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

package com.blazebit.persistence.impl.function.entity;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
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
    public static final String MARKER_PREDICATE = "999=999";
    private static final String AND_MARKER = " and " + MARKER_PREDICATE;

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
        int subqueryEndIndex = subquery.lastIndexOf(AND_MARKER);
        int aliasEndIndex = subquery.indexOf('.', subqueryEndIndex) ;
        int aliasStartIndex = aliasEndIndex - 1;
        while (aliasStartIndex > subqueryEndIndex) {
            if (!SqlUtils.isIdentifier(subquery.charAt(aliasStartIndex))) {
                aliasStartIndex++;
                break;
            }
            aliasStartIndex--;
        }

        String entityName = JpqlFunctionUtil.unquote(functionRenderContext.getArgument(1));
        String valuesClause = JpqlFunctionUtil.unquote(functionRenderContext.getArgument(2));
        String valuesAliases = JpqlFunctionUtil.unquote(functionRenderContext.getArgument(3));
        String syntheticPredicate = JpqlFunctionUtil.unquote(functionRenderContext.getArgument(4));
        String valuesTableSqlAlias = subquery.substring(aliasStartIndex, aliasEndIndex);
        if (syntheticPredicate.isEmpty()) {
            appendSubqueryPart(sb, subquery, 1, subqueryEndIndex, subquery.length() - 1);
        } else {
            sb.append(subquery, 1, subqueryEndIndex);
        }

        if (!syntheticPredicate.isEmpty()) {
            String exampleQuerySqlAlias = syntheticPredicate.substring(0, syntheticPredicate.indexOf('.'));
            syntheticPredicate = syntheticPredicate.replace(exampleQuerySqlAlias, valuesTableSqlAlias);

            // TODO: this is a hibernate specific integration detail
            // Replace the subview subselect that is generated for this subselect
            final String subselect = "( select * from " + entityName + " )";
            final String subselectTableExpr = subselect + " " + valuesTableSqlAlias;
            int subselectIndex = sb.indexOf(subselectTableExpr, 0);
            final String andSeparator = " and ";
            if (subselectIndex == -1) {
                // this is probably a VALUES clause for an entity type
                int syntheticPredicateStart = sb.indexOf(syntheticPredicate, sb.indexOf(" " + valuesTableSqlAlias + " "));
                int end = syntheticPredicateStart + syntheticPredicate.length();
                if (sb.indexOf(andSeparator, end) == end) {
                    sb.replace(syntheticPredicateStart, end + andSeparator.length(), "");
                } else {
                    sb.replace(syntheticPredicateStart, end, "1=1");
                }
            } else {
                while ((subselectIndex = sb.indexOf(subselectTableExpr, subselectIndex)) > -1) {
                    int endIndex = subselectIndex + subselect.length();
                    int syntheticPredicateStart = sb.indexOf(syntheticPredicate, endIndex);
                    int end = syntheticPredicateStart + syntheticPredicate.length();
                    if (sb.indexOf(andSeparator, end) == end) {
                        sb.replace(syntheticPredicateStart, end + andSeparator.length(), "");
                    } else {
                        sb.replace(syntheticPredicateStart, end, "1=1");
                    }
                    sb.replace(subselectIndex, endIndex, entityName);
                }
            }
        }
        SqlUtils.applyTableNameRemapping(sb, valuesTableSqlAlias, valuesClause, valuesAliases, null, false);
        functionRenderContext.addChunk("(");
        functionRenderContext.addChunk(sb.toString());
        functionRenderContext.addChunk(")");
    }

    public static void appendSubqueryPart(StringBuilder sb, String sqlQuery) {
        int subqueryEndIndex = sqlQuery.lastIndexOf(AND_MARKER);
        if (subqueryEndIndex == -1) {
            sb.append(sqlQuery);
        } else {
            appendSubqueryPart(sb, sqlQuery, 0, subqueryEndIndex, sqlQuery.length());
        }
    }

    private static void appendSubqueryPart(StringBuilder sb, String sqlQuery, int start, int subqueryEndIndex, int end) {
        sb.append(sqlQuery, start, subqueryEndIndex);
        int[] range = removeSyntheticPredicate(sqlQuery, subqueryEndIndex, end);
        sb.append(sqlQuery, range[0], range[1]);
    }

    public static void removeSyntheticPredicate(StringBuilder sb, String sqlQuery, int end) {
        int subqueryEndIndex = sb.lastIndexOf(AND_MARKER);
        int[] range = removeSyntheticPredicate(sqlQuery, subqueryEndIndex, end);
        sb.replace(subqueryEndIndex, range[0], "");
    }

    private static int[] removeSyntheticPredicate(String sqlQuery, int subqueryEndIndex, int end) {
        // When inlining e.g. a VALUES clause we need to remove the synthetic predicate
        int idPredicateIndex = sqlQuery.indexOf(" and ", subqueryEndIndex + AND_MARKER.length());
        if (idPredicateIndex == -1) {
            return new int[]{ subqueryEndIndex + AND_MARKER.length(), end };
        } else {
            int isNullPredicateIndex = sqlQuery.indexOf(" is null", idPredicateIndex + 1);
            int newSubqueryPartStart = isNullPredicateIndex + " is null".length();
            for (int i = idPredicateIndex + 1; i < isNullPredicateIndex; i++) {
                if (sqlQuery.charAt(i) == '(') {
                    newSubqueryPartStart++;
                }
            }
            return new int[]{ newSubqueryPartStart, end };
        }
    }

}
