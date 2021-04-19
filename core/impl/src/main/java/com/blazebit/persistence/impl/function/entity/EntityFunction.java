/*
 * Copyright 2014 - 2021 Blazebit.
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
    private static final String NULL_IS_NULL = "(null is null)";
    private static final String AND_TOKEN = " and ";
    private static final String WHERE_TOKEN = " where ";
    private static final String GROUP_BY_TOKEN = " group by ";
    private static final String HAVING_TOKEN = " having ";
    private static final String ORDER_BY_TOKEN = " order by ";

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

        // com.blazebit.persistence.impl.JoinManager.buildClause adds synthetic where clause conjuncts and starts with MARKER_PREDICATE so we can determine it properly
        // We remove the synthetic predicate here and later extract the values clause alias of it so we can insert the proper SQL values clause at the right place
        String subquery = functionRenderContext.getArgument(0);
        StringBuilder sb = new StringBuilder();
        int subqueryEndIndex = subquery.lastIndexOf(AND_MARKER);
        if (subquery.regionMatches(subqueryEndIndex - NULL_IS_NULL.length(), NULL_IS_NULL, 0, NULL_IS_NULL.length())) {
            subqueryEndIndex -= NULL_IS_NULL.length();
        }
        int aliasEndIndex = subquery.indexOf('.', subqueryEndIndex) ;
        int aliasStartIndex = aliasEndIndex - 1;
        while (aliasStartIndex > subqueryEndIndex) {
            if (!SqlUtils.isIdentifier(subquery.charAt(aliasStartIndex))) {
                aliasStartIndex++;
                break;
            }
            aliasStartIndex--;
        }

        String entityName = JpqlFunctionUtil.unquoteSingleQuotes(functionRenderContext.getArgument(1));
        String valuesClause = JpqlFunctionUtil.unquoteSingleQuotes(functionRenderContext.getArgument(2));
        String valuesAliases = JpqlFunctionUtil.unquoteSingleQuotes(functionRenderContext.getArgument(3));
        String syntheticPredicate = JpqlFunctionUtil.unquoteSingleQuotes(functionRenderContext.getArgument(4));
        String valuesTableSqlAlias = subquery.substring(aliasStartIndex, aliasEndIndex);
        appendSubqueryPart(sb, subquery, 1, subqueryEndIndex, subquery.length() - 1);

        if (!syntheticPredicate.isEmpty()) {
            String exampleQuerySqlAlias = syntheticPredicate.substring(0, syntheticPredicate.indexOf('.'));
            syntheticPredicate = syntheticPredicate.replace(exampleQuerySqlAlias, valuesTableSqlAlias);

            removeSyntheticPredicate(sb, entityName, syntheticPredicate, valuesTableSqlAlias);
        }
        SqlUtils.applyTableNameRemapping(sb, valuesTableSqlAlias, valuesClause, valuesAliases, null, false);

        if (regionMatches(sb, sb.length() - WHERE_TOKEN.length(), WHERE_TOKEN, 0, WHERE_TOKEN.length())) {
            sb.setLength(sb.length() - WHERE_TOKEN.length());
        }
        functionRenderContext.addChunk("(");
        functionRenderContext.addChunk(sb.toString());
        functionRenderContext.addChunk(")");
    }

    public static void removeSyntheticPredicate(StringBuilder sb, String entityName, String syntheticPredicate, String valuesTableSqlAlias) {
        // TODO: this is a hibernate specific integration detail
        // Replace the subview subselect that is generated for this subselect
        final String subselect = "( select * from " + entityName + " )";
        final String subselectTableExpr = subselect + " " + valuesTableSqlAlias;
        int subselectIndex = sb.indexOf(subselectTableExpr, 0);
        if (subselectIndex == -1) {
            if (syntheticPredicate != null) {
                // this is probably a VALUES clause for an entity type
                int syntheticPredicateStart = sb.indexOf(syntheticPredicate, sb.indexOf(" " + valuesTableSqlAlias + " "));
                int end = syntheticPredicateStart + syntheticPredicate.length();
                if ('(' == sb.charAt(syntheticPredicateStart - 1) && sb.charAt(end) == ')') {
                    syntheticPredicateStart--;
                    end++;
                }
                if (regionMatches(sb, syntheticPredicateStart - AND_TOKEN.length(), AND_TOKEN, 0, AND_TOKEN.length())) {
                    syntheticPredicateStart -= AND_TOKEN.length();
                } else if (regionMatches(sb, end, AND_TOKEN, 0, AND_TOKEN.length())) {
                    end += AND_TOKEN.length();
                } else if (
                        regionMatches(sb, syntheticPredicateStart - WHERE_TOKEN.length(), WHERE_TOKEN, 0, WHERE_TOKEN.length())
                                && (regionMatches(sb, end, GROUP_BY_TOKEN, 0, GROUP_BY_TOKEN.length())
                                || regionMatches(sb, end, HAVING_TOKEN, 0, HAVING_TOKEN.length())
                                || regionMatches(sb, end, ORDER_BY_TOKEN, 0, ORDER_BY_TOKEN.length())
                        )
                ) {
                    syntheticPredicateStart -= WHERE_TOKEN.length();
                }
                sb.replace(syntheticPredicateStart, end, "");
            }
        } else {
            while ((subselectIndex = sb.indexOf(subselectTableExpr, subselectIndex)) > -1) {
                int endIndex = subselectIndex + subselect.length();
                if (syntheticPredicate != null) {
                    int syntheticPredicateStart = sb.indexOf(syntheticPredicate, endIndex);
                    int end = syntheticPredicateStart + syntheticPredicate.length();
                    if ('(' == sb.charAt(syntheticPredicateStart - 1) && sb.charAt(end) == ')') {
                        syntheticPredicateStart--;
                        end++;
                    }
                    if (regionMatches(sb, syntheticPredicateStart - AND_TOKEN.length(), AND_TOKEN, 0, AND_TOKEN.length())) {
                        syntheticPredicateStart -= AND_TOKEN.length();
                    } else if (regionMatches(sb, end, AND_TOKEN, 0, AND_TOKEN.length())) {
                        end += AND_TOKEN.length();
                    } else if (
                            regionMatches(sb, syntheticPredicateStart - WHERE_TOKEN.length(), WHERE_TOKEN, 0, WHERE_TOKEN.length())
                                    && (regionMatches(sb, end, GROUP_BY_TOKEN, 0, GROUP_BY_TOKEN.length())
                                    || regionMatches(sb, end, HAVING_TOKEN, 0, HAVING_TOKEN.length())
                                    || regionMatches(sb, end, ORDER_BY_TOKEN, 0, ORDER_BY_TOKEN.length())
                            )
                    ) {
                        syntheticPredicateStart -= WHERE_TOKEN.length();
                    }
                    sb.replace(syntheticPredicateStart, end, "");
                }
                sb.replace(subselectIndex, endIndex, entityName);
            }
        }
    }

    private static boolean regionMatches(CharSequence source, int sourceOffset, CharSequence target, int targetOffset, int length) {
        int sourceEnd = sourceOffset + length;
        if (source.length() < sourceEnd) {
            return false;
        }
        for (; sourceOffset < sourceEnd; sourceOffset++, targetOffset++) {
            if (source.charAt(sourceOffset) != target.charAt(targetOffset)) {
                return false;
            }
        }
        return true;
    }

    public static void appendSubqueryPart(StringBuilder sb, String sqlQuery) {
        int markerIndex = sqlQuery.lastIndexOf(AND_MARKER);
        if (markerIndex == -1) {
            sb.append(sqlQuery);
        } else {
            int subqueryEndIndex = sqlQuery.lastIndexOf("(null is null)", markerIndex);
            if (subqueryEndIndex == -1) {
                subqueryEndIndex = markerIndex;
            }
            sb.append(sqlQuery, 0, subqueryEndIndex);
            int[] range = removeSyntheticPredicate(sqlQuery, markerIndex, sqlQuery.length());
            if (
                    sqlQuery.regionMatches(subqueryEndIndex - WHERE_TOKEN.length(), WHERE_TOKEN, 0, WHERE_TOKEN.length())
                            && (sqlQuery.regionMatches(range[0], GROUP_BY_TOKEN, 0, GROUP_BY_TOKEN.length())
                            || sqlQuery.regionMatches(range[0], HAVING_TOKEN, 0, HAVING_TOKEN.length())
                            || sqlQuery.regionMatches(range[0], ORDER_BY_TOKEN, 0, ORDER_BY_TOKEN.length())
                    )
            ) {
                sb.setLength(sb.length() - WHERE_TOKEN.length());
            }
            sb.append(sqlQuery, range[0], range[1]);
        }
    }

    private static void appendSubqueryPart(StringBuilder sb, String sqlQuery, int start, int subqueryEndIndex, int end) {
        sb.append(sqlQuery, start, subqueryEndIndex);
        int[] range = removeSyntheticPredicate(sqlQuery, subqueryEndIndex, end);
        sb.append(sqlQuery, range[0], range[1]);
    }

    public static void removeSyntheticPredicate(StringBuilder sb, int end) {
        int markerIndex = sb.lastIndexOf(AND_MARKER);
        int subqueryEndIndex = sb.lastIndexOf("(null is null)", markerIndex);
        if (subqueryEndIndex == -1) {
            subqueryEndIndex = markerIndex;
        }
        int[] range = removeSyntheticPredicate(sb, subqueryEndIndex, end);
        sb.replace(subqueryEndIndex, range[0], "");
    }

    private static int[] removeSyntheticPredicate(StringBuilder sqlQuery, int markerIndex, int end) {
        // When inlining e.g. a VALUES clause we need to remove the synthetic predicate
        int idPredicateIndex = sqlQuery.indexOf(" and ", markerIndex + AND_MARKER.length());
        if (idPredicateIndex == -1) {
            return new int[]{ markerIndex + AND_MARKER.length(), end };
        } else {
            int isNullPredicateIndex = sqlQuery.indexOf(" is null", idPredicateIndex + 1);
            int newSubqueryPartStart = isNullPredicateIndex + " is null".length();
            for (int i = idPredicateIndex + 1; i < isNullPredicateIndex; i++) {
                if (sqlQuery.charAt(i) == '(') {
                    newSubqueryPartStart++;
                }
            }
            if (regionMatches(sqlQuery, newSubqueryPartStart, " and ", 0, " and ".length())) {
                return new int[]{ newSubqueryPartStart + " and ".length(), end };
            }
            return new int[]{ newSubqueryPartStart, end };
        }
    }

    private static int[] removeSyntheticPredicate(String sqlQuery, int markerIndex, int end) {
        // When inlining e.g. a VALUES clause we need to remove the synthetic predicate
        int idPredicateIndex = sqlQuery.indexOf(" and ", markerIndex + AND_MARKER.length());
        if (idPredicateIndex == -1) {
            return new int[]{ markerIndex + AND_MARKER.length(), end };
        } else {
            int isNullPredicateIndex = sqlQuery.indexOf(" is null", idPredicateIndex + 1);
            int newSubqueryPartStart = isNullPredicateIndex + " is null".length();
            for (int i = idPredicateIndex + 1; i < isNullPredicateIndex; i++) {
                if (sqlQuery.charAt(i) == '(') {
                    newSubqueryPartStart++;
                }
            }
            if (regionMatches(sqlQuery, newSubqueryPartStart, " and ", 0, " and ".length())) {
                return new int[]{ newSubqueryPartStart + " and ".length(), end };
            }
            return new int[]{ newSubqueryPartStart, end };
        }
    }

}
