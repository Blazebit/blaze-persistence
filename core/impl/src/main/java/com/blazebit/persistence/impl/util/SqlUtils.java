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

package com.blazebit.persistence.impl.util;

import com.blazebit.persistence.parser.SQLParser;
import com.blazebit.persistence.parser.SQLParserBaseVisitor;
import com.blazebit.persistence.parser.SqlParserUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to extract information from SQL queries.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SqlUtils {

    public static final String SELECT = "select ";
    public static final String UPDATE = "update ";
    public static final String SET = " set ";
    public static final String FROM = " from ";
    public static final String JOIN = " join ";
    public static final String ON = " on ";
    public static final String WITH = "with ";
    public static final String WHERE = " where ";
    public static final String GROUP_BY = " group by ";
    public static final String HAVING = " having ";
    public static final String ORDER_BY = " order by ";
    public static final String LIMIT = " limit ";
    public static final String FETCH_FIRST = " fetch first ";
    public static final String AS = " as ";
    public static final String FROM_FINAL_TABLE = " from final table (";
    public static final String NEXT_VALUE_FOR = "next value for ";
    public static final String INTO = "into ";
    public static final PatternFinder SELECT_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(SELECT));
    public static final PatternFinder UPDATE_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(UPDATE));
    public static final PatternFinder SET_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(SET));
    public static final PatternFinder FROM_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM));
    public static final PatternFinder JOIN_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(JOIN));
    public static final PatternFinder ON_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(ON));
    public static final PatternFinder WITH_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(WITH));
    public static final PatternFinder WHERE_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(WHERE));
    public static final PatternFinder GROUP_BY_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(GROUP_BY));
    public static final PatternFinder HAVING_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(HAVING));
    public static final PatternFinder ORDER_BY_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(ORDER_BY));
    public static final PatternFinder LIMIT_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(LIMIT));
    public static final PatternFinder FETCH_FIRST_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FETCH_FIRST));
    public static final PatternFinder AS_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiLastPatternFinder(AS));
    public static final PatternFinder FROM_FINAL_TABLE_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM_FINAL_TABLE));
    public static final PatternFinder NEXT_VALUE_FOR_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(NEXT_VALUE_FOR));
    public static final PatternFinder INTO_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(INTO));

    /**
     * @author Christian Beikov
     * @since 1.2.0
     */
    public static interface SelectItemExtractor {
        public String extract(StringBuilder sb, int index, int currentPosition);
    }

    private static final SelectItemExtractor ALIAS_EXTRACTOR = new SelectItemExtractor() {
        @Override
        public String extract(StringBuilder sb, int index, int currentPosition) {
            return extractAlias(sb);
        }
    };

    private static final SelectItemExtractor EXPRESSION_EXTRACTOR = new SelectItemExtractor() {
        @Override
        public String extract(StringBuilder sb, int index, int currentPosition) {
            return extractExpression(sb);
        }
    };

    private static final SelectItemExtractor COLUMN_EXTRACTOR = new SelectItemExtractor() {
        @Override
        public String extract(StringBuilder sb, int index, int currentPosition) {
            return extractColumn(sb);
        }
    };

    private SqlUtils() {
    }

    public static void applyTableNameRemapping(StringBuilder sb, String sqlAlias, String newCteName, String aliasExtension, String newSqlAlias, boolean useApply) {
        final String searchAs = " as";
        final String searchAlias = " " + sqlAlias;
        int searchIndex = 0;
        while ((searchIndex = sb.indexOf(searchAlias, searchIndex)) > -1) {
            int idx = searchIndex + searchAlias.length();
            if (idx < sb.length() && sb.charAt(idx) == '.') {
                // This is a dereference of the alias, skip this
            } else if (isInMainQuery(sb, searchIndex)) {
                int[] tableNameIndexRange;
                if (searchAs.equalsIgnoreCase(sb.substring(searchIndex - searchAs.length(), searchIndex))) {
                    // Uses aliasing with the AS keyword
                    tableNameIndexRange = rtrimBackwardsToFirstWhitespace(sb, searchIndex - searchAs.length());
                } else {
                    // Uses aliasing without the AS keyword
                    tableNameIndexRange = rtrimBackwardsToFirstWhitespace(sb, searchIndex);
                }

                // If the table name is a subquery, we have to respect that and scan back to the start parenthesis
                if (sb.charAt(tableNameIndexRange[0]) == ')') {
                    int parenthesis = 1;
                    QuoteMode mode = QuoteMode.NONE;
                    for (int i = tableNameIndexRange[0] - 1; i >= 0; i--) {
                        char c = sb.charAt(i);
                        mode = mode.onCharBackwards(c);

                        if (mode == QuoteMode.NONE) {
                            if (c == '(') {
                                parenthesis--;
                                if (parenthesis == 0) {
                                    tableNameIndexRange[0] = i;
                                    break;
                                }
                            } else if (c == ')') {
                                parenthesis++;
                            }
                        }
                    }
                }

                if (newSqlAlias != null) {
                    sb.replace(tableNameIndexRange[1] + 1, tableNameIndexRange[1] + 1 + sqlAlias.length(), newSqlAlias);
                    searchIndex += newSqlAlias.length() - sqlAlias.length();
                    sqlAlias = newSqlAlias;
                }

                int oldTableNameLength = tableNameIndexRange[1] - tableNameIndexRange[0];
                // Replace table name with cte name
                if (useApply) {
                    int whereIndex = SqlUtils.indexOfWhere(sb, tableNameIndexRange[1]);
                    if (whereIndex == -1) {
                        whereIndex = sb.length();
                    }
                    int[] indexRange = SqlUtils.indexOfFullJoin(sb, sqlAlias, tableNameIndexRange[1] + 1, whereIndex);
                    // Since we are moving the sql alias due to the use of apply, we have to adapt the search index
                    oldTableNameLength += tableNameIndexRange[0] - indexRange[0];
                    // NOTE: This will remove the ON clause as the APPLY clause doesn't support conditions. The query builder must ensure predicates are put into the query
                    sb.replace(indexRange[0], indexRange[1], newCteName + sb.substring(tableNameIndexRange[1], tableNameIndexRange[1] + searchAlias.length()));
                } else {
                    sb.replace(tableNameIndexRange[0], tableNameIndexRange[1], newCteName);
                }

                if (aliasExtension != null) {
                    sb.insert(searchIndex + searchAlias.length() + (newCteName.length() - oldTableNameLength), aliasExtension);
                    searchIndex += aliasExtension.length();
                }

                // Adjust index after replacing
                searchIndex += newCteName.length() - oldTableNameLength;
            }

            searchIndex = searchIndex + 1;
        }
    }

    public static void remapColumnExpressions(StringBuilder sqlSb, Map<String, String> columnExpressionRemappings) {
        remapColumnExpressions(sqlSb, columnExpressionRemappings, 0, sqlSb.length());
    }

    public static int remapColumnExpressions(StringBuilder sqlSb, Map<String, String> columnExpressionRemappings, int startIndex, int endIndex) {
        // Replace usages of the owner entities id columns by the corresponding join table id columns
        for (Map.Entry<String, String> entry : columnExpressionRemappings.entrySet()) {
            String sourceExpression = entry.getKey();
            String targetExpression = entry.getValue();
            if (sourceExpression.equals(targetExpression)) {
                continue;
            }
            int index = startIndex;
            while ((index = sqlSb.indexOf(sourceExpression, index)) != -1 && index < endIndex) {
                if (index != 0 && Character.isJavaIdentifierPart(sqlSb.charAt(index - 1))) {
                    index++;
                    continue;
                }
                int sourceEndIndex = index + sourceExpression.length();
                if (sourceEndIndex < endIndex && Character.isJavaIdentifierPart(sqlSb.charAt(sourceEndIndex))) {
                    index++;
                    continue;
                }
                sqlSb.replace(index, sourceEndIndex, targetExpression);
                int delta = targetExpression.length() - sourceExpression.length();
                index += delta;
                endIndex += delta;
            }
        }
        return endIndex;
    }

    private static boolean isInMainQuery(StringBuilder sb, int tableNameIndex) {
        int parenthesis = 0;
        QuoteMode mode = QuoteMode.NONE;
        for (int i = 0; i < tableNameIndex; i++) {
            final char c = sb.charAt(i);
            mode = mode.onChar(c);

            if (mode == QuoteMode.NONE) {
                if (c == '(') {
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                }
            }
        }
        return parenthesis == 0;
    }

    public static int[] rtrimBackwardsToFirstWhitespace(CharSequence sb, int startIndex) {
        int tableNameStartIndex;
        int tableNameEndIndex = startIndex;
        boolean text = false;
        for (tableNameStartIndex = tableNameEndIndex; tableNameStartIndex >= 0; tableNameStartIndex--) {
            if (text) {
                final char c = sb.charAt(tableNameStartIndex);
                if (Character.isWhitespace(c) || c == ',') {
                    tableNameStartIndex++;
                    break;
                }
            } else {
                if (Character.isWhitespace(sb.charAt(tableNameStartIndex))) {
                    tableNameEndIndex--;
                } else {
                    text = true;
                    tableNameEndIndex++;
                }
            }
        }

        return new int[]{ tableNameStartIndex, tableNameEndIndex };
    }

    public static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    public static boolean isIdentifier(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /**
     * Extracts the select item aliases of an arbitrary SELECT query.
     *
     * @param sql The SQL query
     * @param start The start index from which to look for select items
     * @return The select item aliases
     */
    public static String[] getSelectItemAliases(CharSequence sql, int start) {
        return getSelectItems(sql, start, ALIAS_EXTRACTOR);
    }

    public static String[] getSelectItemExpressions(CharSequence sql, int start) {
        return getSelectItems(sql, start, EXPRESSION_EXTRACTOR);
    }

    public static String[] getSelectItemColumns(CharSequence sql, int start) {
        return getSelectItems(sql, start, COLUMN_EXTRACTOR);
    }

    public static String[] getSelectItems(CharSequence sql, int start, SelectItemExtractor extractor) {
        int selectIndex = SELECT_FINDER.indexIn(sql, start);
        int fromIndex = indexOfFrom(sql, selectIndex);
        // from-less query
        if (fromIndex == -1) {
            fromIndex = sql.length();
        }

        List<String> selectItems = getExpressionItems(sql, selectIndex + SELECT.length(), fromIndex, extractor);
        return selectItems.toArray(new String[selectItems.size()]);
    }

    public static List<String> getExpressionItems(CharSequence sql) {
        return getExpressionItems(sql, 0, sql.length(), EXPRESSION_EXTRACTOR);
    }

    public static List<String> getExpressionItems(CharSequence sql, int i, int end) {
        return getExpressionItems(sql, i, end, EXPRESSION_EXTRACTOR);
    }

    public static List<String> getExpressionItems(CharSequence sql, int i, int end, SelectItemExtractor extractor) {
        List<String> selectItems = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int parenthesis = 0;
        QuoteMode mode = QuoteMode.NONE;

        while (i < end) {
            final char c = sql.charAt(i);
            mode = mode.onChar(c);

            if (mode == QuoteMode.NONE) {
                if (parenthesis == 0 && c == ',') {
                    selectItems.add(extractor.extract(sb, selectItems.size(), i));
                    sb.setLength(0);
                    i++;
                    continue;
                } else if (c == '(') {
                    // While we are in a subcontext, consider the whole query
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                }
                if (sb.length() != 0 || !Character.isWhitespace(c)) {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }

            i++;
        }

        String lastAlias = extractor.extract(sb, selectItems.size(), i);
        if (!lastAlias.isEmpty()) {
            selectItems.add(lastAlias);
        }

        return selectItems;
    }

    public static void buildAliasMappingForTopLevelSelects(CharSequence sql, String alias, Map<String, String> aliasMapping) {
        final Set<String> definedTableAliases = new HashSet<>();
        final Map<String, Set<String>> usedColumns = new HashMap<>();
        try {
            SqlParserUtils.visitSelectStatement(sql, new SQLParserBaseVisitor<Void>() {

                Boolean inTopLevelSelect;
                boolean inSubquery;

                @Override
                public Void visitSelect_list(SQLParser.Select_listContext ctx) {
                    Boolean select = inTopLevelSelect;
                    if (inTopLevelSelect == null) {
                        inTopLevelSelect = true;
                    } else if (inTopLevelSelect == Boolean.FALSE) {
                        // We only care about the first select item list, we ignore others
                        return null;
                    }
                    try {
                        return super.visitSelect_list(ctx);
                    } finally {
                        if (select == null) {
                            inTopLevelSelect = Boolean.FALSE;
                        }
                    }
                }

                @Override
                public Void visitFull_column_name(SQLParser.Full_column_nameContext ctx) {
                    SQLParser.Table_nameContext tableNameContext = ctx.table_name();
                    SQLParser.IdContext idContext = ctx.id();
                    columnUsage(tableNameContext, idContext);
                    return super.visitFull_column_name(ctx);
                }

                @Override
                public Void visitColumn_elem(SQLParser.Column_elemContext ctx) {
                    SQLParser.Table_nameContext tableNameContext = ctx.table_name();
                    SQLParser.IdContext idContext = ctx.id();
                    columnUsage(tableNameContext, idContext);
                    return super.visitColumn_elem(ctx);
                }

                private void columnUsage(SQLParser.Table_nameContext tableNameContext, SQLParser.IdContext idContext) {
                    if (tableNameContext == null || idContext == null || !inTopLevelSelect) {
                        return;
                    }
                    String tableName = tableNameContext.getText();
                    Set<String> columns = usedColumns.get(tableName);
                    if (columns == null) {
                        usedColumns.put(tableName, columns = new HashSet<>());
                    }
                    columns.add(idContext.getText());
                }

                @Override
                public Void visitSubquery(SQLParser.SubqueryContext ctx) {
                    boolean subquery = !inSubquery;
                    inSubquery = true;
                    try {
                        return super.visitSubquery(ctx);
                    } finally {
                        if (subquery) {
                            inSubquery = false;
                        }
                    }
                }

                @Override
                public Void visitTable_source_item(SQLParser.Table_source_itemContext ctx) {
                    if (!inSubquery) {
                        SQLParser.As_table_aliasContext aliasContext = ctx.as_table_alias();
                        if (aliasContext != null) {
                            definedTableAliases.add(aliasContext.table_alias().getText());
                        }
                    }
                    return super.visitTable_source_item(ctx);
                }
            });
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Couldn't parse SQL fragment: " + sql, ex);
        }
        for (Map.Entry<String, Set<String>> entry : usedColumns.entrySet()) {
            if (definedTableAliases.contains(entry.getKey())) {
                for (String column : entry.getValue()) {
                    if (!aliasMapping.containsKey(entry.getKey() + "." + column)) {
                        aliasMapping.put(entry.getKey() + "." + column, alias + ".c" + aliasMapping.size());
                    }
                }
            }
        }
    }

    /**
     * Finds the toplevel SELECT keyword in an arbitrary SELECT query.
     *
     * @param sql The SQL query
     * @return The index of the SELECT keyword if found, or -1
     */
    public static int indexOfSelect(CharSequence sql) {
        int selectIndex = SELECT_FINDER.indexIn(sql);
        int withIndex = WITH_FINDER.indexIn(sql, 0, selectIndex);
        if (withIndex == -1 && selectIndex == 0) {
            return selectIndex;
        }

        return indexOf(SELECT_FINDER, sql, 0, Math.max(withIndex, 0));
    }

    public static int indexOfSet(CharSequence sql) {
        return indexOf(SET_FINDER, sql, 0, 0);
    }

    public static int indexOfFrom(CharSequence sql) {
        return indexOf(FROM_FINDER, sql, 0, 0);
    }

    public static int indexOfFrom(CharSequence sql, int start) {
        return indexOf(FROM_FINDER, sql, start, start);
    }

    /**
     * Finds the toplevel WHERE keyword in an arbitrary query.
     *
     * @param sql The SQL query
     * @return The index of the SELECT keyword if found, or -1
     */
    public static int indexOfWhere(CharSequence sql) {
        return indexOfWhere(sql, 0);
    }

    public static int indexOfWhere(CharSequence sql, int start) {
        return indexOf(WHERE_FINDER, sql, start, 0);
    }

    public static int indexOfGroupBy(CharSequence sql, int start) {
        return indexOf(GROUP_BY_FINDER, sql, start, 0);
    }

    public static int indexOfHaving(CharSequence sql, int start) {
        return indexOf(HAVING_FINDER, sql, start, 0);
    }

    /**
     * Finds the toplevel ORDER BY keyword in an arbitrary query.
     *
     * @param sql The SQL query
     * @return The index of the ORDER BY keyword if found, or -1
     */
    public static int indexOfOrderBy(CharSequence sql) {
        return indexOf(ORDER_BY_FINDER, sql, 0, 0);
    }

    public static int indexOfOrderBy(CharSequence sql, int start) {
        return indexOf(ORDER_BY_FINDER, sql, start, start);
    }

    /**
     * Finds the toplevel LIMIT keyword in an arbitrary query.
     *
     * @param sql The SQL query
     * @return The index of the LIMIT keyword if found, or -1
     */
    public static int indexOfLimit(CharSequence sql) {
        return indexOf(LIMIT_FINDER, sql, 0, 0);
    }

    public static int indexOfLimit(CharSequence sql, int start) {
        return indexOf(LIMIT_FINDER, sql, start, start);
    }

    public static int indexOfFetchFirst(CharSequence sql, int start) {
        return indexOf(FETCH_FIRST_FINDER, sql, start, start);
    }

    public static int indexOfOn(CharSequence sql, int start) {
        return indexOf(ON_FINDER, sql, start, start);
    }

    private static int indexOf(PatternFinder patternFinder, CharSequence sql, int start, int checkStart) {
        int patternIndex = patternFinder.indexIn(sql, start);
        int brackets = 0;
        QuoteMode mode = QuoteMode.NONE;
        int i = checkStart;
        int end = patternIndex;
        while (i < end) {
            final char c = sql.charAt(i);
            mode = mode.onChar(c);

            if (mode == QuoteMode.NONE) {
                if (c == '(') {
                    // While we are in a subcontext, consider the whole query
                    end = sql.length();

                    brackets++;
                } else if (c == ')') {
                    brackets--;

                    if (brackets == 0) {
                        // When we leave the context, reset the end to the select index
                        if (i < patternIndex) {
                            end = patternIndex;
                        } else {
                            // If the found select was in the subcontext, find the next select
                            end = patternIndex = patternFinder.indexIn(sql, i);
                        }
                    }
                }
            }

            i++;
        }

        return patternIndex;
    }

    /**
     * Finds the final table clause in an arbitrary SELECT query.
     *
     * @param sql The SQL query
     * @param selectIndex The start index or the index of the toplevel SELECT keyword in the query
     * @return The start and end index of the final table subquery if found, or 0 and the length of the query
     */
    public static int[] indexOfFinalTableSubquery(CharSequence sql, int selectIndex) {
        int fromFinalTableIndex = FROM_FINAL_TABLE_FINDER.indexIn(sql, selectIndex);
        if (fromFinalTableIndex == -1) {
            return new int[] { 0, sql.length() };
        }
        int brackets = 1;
        QuoteMode mode = QuoteMode.NONE;
        int i = fromFinalTableIndex + FROM_FINAL_TABLE.length();
        int end = sql.length();
        while (i < end) {
            final char c = sql.charAt(i);
            mode = mode.onChar(c);

            if (mode == QuoteMode.NONE) {
                if (c == '(') {
                    brackets++;
                } else if (c == ')') {
                    brackets--;

                    if (brackets == 0) {
                        return new int[] { fromFinalTableIndex + FROM_FINAL_TABLE.length(), i };
                    }
                }
            }

            i++;
        }

        return new int[] { 0, sql.length() };
    }

    /**
     * Finds the table name within a FROM clause of an arbitrary SELECT query.
     *
     * @param sql The SQL query
     * @param tableName The table name to look for
     * @return The index of the table name or -1 if it couldn't be found
     */
    public static int indexOfTableName(CharSequence sql, String tableName) {
        int startIndex = FROM_FINDER.indexIn(sql, 0);
        if (startIndex == -1) {
            return -1;
        }
        startIndex += FROM.length();
        int whereIndex = indexOfWhere(sql);
        if (whereIndex == -1) {
            whereIndex = sql.length();
        }

        PatternFinder finder = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(" " + tableName + " "));
        int index = finder.indexIn(sql, startIndex, whereIndex);
        if (index == -1) {
            return -1;
        }

        return index + 1;
    }

    public static int indexOfJoinTableAlias(CharSequence sql, String tableName) {
        int startIndex = FROM_FINDER.indexIn(sql, 0);
        if (startIndex == -1) {
            return -1;
        }
        startIndex += FROM.length();
        int whereIndex = indexOfWhere(sql);
        if (whereIndex == -1) {
            whereIndex = sql.length();
        }

        PatternFinder finder = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(" " + tableName + " on "));
        int index = finder.indexIn(sql, startIndex, whereIndex);
        if (index == -1) {
            return -1;
        }

        return index + 1;
    }

    public static int[] indexOfFullJoin(CharSequence sql, String tableAlias) {
        int whereIndex = SqlUtils.indexOfWhere(sql);
        return indexOfFullJoin(sql, tableAlias, whereIndex);
    }

    public static int[] indexOfFullJoin(CharSequence sql, String tableAlias, int whereIndex) {
        // For every table alias we found in the select items that we removed due to the cutoff, we delete the joins
        int aliasIndex = indexOfJoinTableAlias(sql, tableAlias);
        return indexOfFullJoin(sql, tableAlias, aliasIndex, whereIndex);
    }

    public static int[] indexOfFullJoin(CharSequence sql, String tableAlias, int aliasIndex, int whereIndex) {
        String aliasOnPart = " " + tableAlias + " on ";
        if (aliasIndex > -1 && aliasIndex < whereIndex) {
            // indexOfJoinTableAlias moves the index to the first char of the alias, so move back
            aliasIndex--;
            // First, let's find the end of the on clause
            int onClauseStart = aliasIndex + aliasOnPart.length();
            int onClauseEnd = SqlUtils.findEndOfOnClause(sql, onClauseStart, whereIndex);
            int joinStartIndex = SqlUtils.findJoinStartIndex(sql, aliasIndex);
            return new int[] { joinStartIndex, onClauseEnd };
        }

        return null;
    }

    public static int findJoinStartIndex(CharSequence sqlSb, int aliasIndex) {
        // Then we step back token-wise until we have found the tokens "(left|inner|cross)? outer? join"
        int[] tokenRange = SqlUtils.rtrimBackwardsToFirstWhitespace(sqlSb, aliasIndex);
        return findJoinStartIndex(sqlSb, tokenRange[0] - 1, EnumSet.of(JoinToken.JOIN));
    }

    public static int findJoinStartIndex(CharSequence sqlSb, int tokenEnd, Set<JoinToken> allowedTokens) {
        int[] tokenRange;
        do {
            tokenRange = SqlUtils.rtrimBackwardsToFirstWhitespace(sqlSb, tokenEnd);
            tokenEnd = tokenRange[0] - 1;
            JoinToken token = JoinToken.of(sqlSb.subSequence(tokenRange[0], tokenRange[1]).toString().trim().toUpperCase());
            if (allowedTokens.contains(token)) {
                allowedTokens = token.previous();
            } else {
                return tokenRange[1];
            }
        } while (!allowedTokens.isEmpty());

        return tokenRange[0];
    }

    /**
     * @author Christian Beikov
     * @since 1.3.0
     */
    enum JoinToken {
        COMMA,
        LEFT,
        INNER,
        RIGHT,
        CROSS,
        OUTER {
            @Override
            Set<JoinToken> previous() {
                return EnumSet.of(LEFT, RIGHT);
            }
        },
        JOIN {
            @Override
            Set<JoinToken> previous() {
                return EnumSet.of(LEFT, INNER, RIGHT, OUTER, CROSS);
            }
        };

        Set<JoinToken> previous() {
            return EnumSet.noneOf(JoinToken.class);
        }

        static JoinToken of(String text) {
            switch (text) {
                case ",":
                    return COMMA;
                default:
                    return valueOf(text);
            }
        }
    }

    public static int findEndOfOnClause(CharSequence sqlSb, int predicateStartIndex, int whereIndex) {
        int joinIndex = JOIN_FINDER.indexIn(sqlSb, predicateStartIndex);
        int end;
        if (joinIndex == -1 || joinIndex > whereIndex) {
            end = whereIndex;
        } else {
            end = findJoinStartIndex(sqlSb, joinIndex, JoinToken.JOIN.previous());
        }
        int potentialEndIndex = end;
        int parenthesis = 0;
        QuoteMode mode = QuoteMode.NONE;
        for (int i = predicateStartIndex; i < end; i++) {
            char c = sqlSb.charAt(i);
            mode = mode.onCharBackwards(c);

            if (mode == QuoteMode.NONE) {
                if (c == '(') {
                    // While we are in a subcontext, consider the whole query
                    end = whereIndex;
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                    // When we leave the context, reset the end to the potential end index
                    if (i < potentialEndIndex) {
                        end = potentialEndIndex;
                    } else {
                        // If the found end index was in the subcontext, find the next join index
                        joinIndex = JOIN_FINDER.indexIn(sqlSb, i);
                        if (joinIndex == -1) {
                            // If there is none, we break out
                            return whereIndex;
                        } else {
                            end = potentialEndIndex = findJoinStartIndex(sqlSb, joinIndex, JoinToken.JOIN.previous());
                        }
                    }
                } else if (c == ',' && parenthesis == 0) {
                    // Cross join via comma operator
                    return i;
                }
            }
        }

        return end;
    }

    /**
     * Extracts the alias part of a select item expression.
     *
     * @param sb The string builder containing the select item expression
     * @return The alias of the select item expression
     */
    public static String extractAlias(StringBuilder sb) {
        int aliasEndCharIndex = findLastNonWhitespace(sb);
        QuoteMode mode = QuoteMode.NONE.onCharBackwards(sb.charAt(aliasEndCharIndex));
        int endIndex = aliasEndCharIndex;

        // While we are in quote mode, reduce the end index
        if (mode != QuoteMode.NONE) {
            do {
                endIndex--;
                mode = mode.onCharBackwards(sb.charAt(endIndex));
            } while (mode != QuoteMode.NONE || endIndex > 0 && sb.charAt(endIndex) == sb.charAt(endIndex - 1));
        }

        int aliasBeforeIndex = findLastWhitespace(sb, endIndex);
        int dotIndex = sb.lastIndexOf(".", endIndex);
        aliasBeforeIndex = Math.max(aliasBeforeIndex, dotIndex);
        if (NEXT_VALUE_FOR_FINDER.indexIn(sb) != -1) {
            // Since sequences in subqueries might not be allowed, we pass the whole expression
            return sb.toString();
        }
        return sb.substring(aliasBeforeIndex + 1, aliasEndCharIndex + 1);
    }

    /**
     * Extracts the next alias from the given expression starting the given index.
     *
     * @param sb The char sequence containing the alias
     * @param index The start index
     * @return The next alias of the char sequence
     */
    public static String extractAlias(CharSequence sb, int index) {
        int aliasBeginCharIndex = skipWhitespaces(sb, index);

        if (sb.length() > aliasBeginCharIndex + 3
                && Character.toLowerCase(sb.charAt(aliasBeginCharIndex)) == 'a'
                && Character.toLowerCase(sb.charAt(aliasBeginCharIndex + 1)) == 's'
                && Character.isWhitespace(sb.charAt(aliasBeginCharIndex + 2))
        ) {
            aliasBeginCharIndex = skipWhitespaces(sb, aliasBeginCharIndex + 2);
        }

        QuoteMode mode = QuoteMode.NONE;
        int i = aliasBeginCharIndex;
        int end = sb.length();
        while (i < end) {
            final char c = sb.charAt(i);
            mode = mode.onChar(c);

            // When we encounter the next whitespace, the alias ended
            if (mode == QuoteMode.NONE && Character.isWhitespace(c)) {
                break;
            }

            i++;
        }
        return sb.subSequence(aliasBeginCharIndex, i).toString();
    }

    private static int skipWhitespaces(CharSequence charSequence, int index) {
        while (Character.isWhitespace(charSequence.charAt(index))) {
            int nextIndex = index + 1;
            if (nextIndex == charSequence.length()) {
                return nextIndex;
            } else {
                index = nextIndex;
            }
        }
        return index;
    }

    /**
     * Extracts the expression part of a select item expression.
     *
     * @param sb The string builder containing the select item expression
     * @return The expression part of the select item expression
     */
    private static String extractExpression(StringBuilder sb) {
        int asIndex = AS_FINDER.indexIn(sb);
        if (asIndex == -1) {
            return sb.toString();
        }
        int lastNonWhitespaceIndex = sb.length();
        for (int i = sb.length() - 1; i > 0; i--) {
            if (!Character.isWhitespace(sb.charAt(i))) {
                lastNonWhitespaceIndex = i + 1;
                break;
            }
        }
        // If the supposed alias contains a whitespace, we are in a subquery
        for (int i = asIndex + 4; i < lastNonWhitespaceIndex; i++) {
            final char c = sb.charAt(i);
            if (Character.isWhitespace(c)) {
                return sb.toString();
            }
        }

        return sb.substring(0, asIndex);
    }

    /**
     * Extracts the column name part of a select item expression.
     *
     * @param sb The string builder containing the select item expression
     * @return The column name part of the select item expression
     */
    private static String extractColumn(StringBuilder sb) {
        int asIndex = AS_FINDER.indexIn(sb);
        if (asIndex == -1) {
            return sb.substring(findLastDot(sb, sb.length()) + 1);
        }

        return sb.substring(findLastDot(sb, asIndex) + 1, asIndex);
    }

    private static int findLastDot(StringBuilder sb, int end) {
        // Goes through the chars backwards looking for the first '.' when not being in quote mode
        // While in quote mode, we skip chars
        int i = end - 1;
        QuoteMode mode = QuoteMode.NONE;
        while (i >= 0) {
            final char c = sb.charAt(i);
            mode = mode.onCharBackwards(sb.charAt(i));

            if (mode == QuoteMode.NONE) {
                if (c == '.') {
                    break;
                } else {
                    i--;
                }
            } else {
                i--;
            }
        }

        return i;
    }

    private static int findLastNonWhitespace(StringBuilder sb) {
        return findLastNonWhitespace(sb, sb.length() - 1);
    }

    private static int findLastNonWhitespace(StringBuilder sb, int end) {
        int i = end;
        while (i >= 0) {
            if (!Character.isWhitespace(sb.charAt(i))) {
                break;
            } else {
                i--;
            }
        }

        return i;
    }

    private static int findLastWhitespace(StringBuilder sb, int end) {
        int i = end;
        while (i >= 0) {
            if (Character.isWhitespace(sb.charAt(i))) {
                break;
            } else {
                i--;
            }
        }

        return i;
    }
}
