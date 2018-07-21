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

package com.blazebit.persistence.impl.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to extract information from SQL queries.
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class SqlUtils {

    private static final String SELECT = "select ";
    private static final String SET = " set ";
    private static final String FROM = " from ";
    private static final String WITH = "with ";
    private static final String WHERE = " where ";
    private static final String ORDER_BY = " order by ";
    private static final String AS = " as ";
    private static final String FROM_FINAL_TABLE = " from final table (";
    private static final String NEXT_VALUE_FOR = "next value for ";
    private static final PatternFinder SELECT_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(SELECT));
    private static final PatternFinder SET_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(SET));
    private static final PatternFinder FROM_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM));
    private static final PatternFinder WITH_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(WITH));
    private static final PatternFinder WHERE_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(WHERE));
    private static final PatternFinder ORDER_BY_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(ORDER_BY));
    private static final PatternFinder AS_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiLastPatternFinder(AS));
    private static final PatternFinder FROM_FINAL_TABLE_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM_FINAL_TABLE));
    private static final PatternFinder NEXT_VALUE_FOR_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(NEXT_VALUE_FOR));

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

    public static void applyTableNameRemapping(StringBuilder sb, String sqlAlias, String newCteName, String aliasExtension) {
        final String searchAs = " as";
        final String searchAlias = " " + sqlAlias;
        int searchIndex = 0;
        while ((searchIndex = sb.indexOf(searchAlias, searchIndex)) > -1) {
            int idx = searchIndex + searchAlias.length();
            if (idx < sb.length() && sb.charAt(idx) == '.') {
                // This is a dereference of the alias, skip this
            } else {
                int[] indexRange;
                if (searchAs.equalsIgnoreCase(sb.substring(searchIndex - searchAs.length(), searchIndex))) {
                    // Uses aliasing with the AS keyword
                    indexRange = rtrimBackwardsToFirstWhitespace(sb, searchIndex - searchAs.length());
                } else {
                    // Uses aliasing without the AS keyword
                    indexRange = rtrimBackwardsToFirstWhitespace(sb, searchIndex);
                }

                int oldLength = indexRange[1] - indexRange[0];
                // Replace table name with cte name
                sb.replace(indexRange[0], indexRange[1], newCteName);

                if (aliasExtension != null) {
                    sb.insert(searchIndex + searchAlias.length() + (newCteName.length() - oldLength), aliasExtension);
                    searchIndex += aliasExtension.length();
                }

                // Adjust index after replacing
                searchIndex += newCteName.length() - oldLength;
            }

            searchIndex = searchIndex + 1;
        }
    }

    public static int[] rtrimBackwardsToFirstWhitespace(StringBuilder sb, int startIndex) {
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
        int fromIndex = FROM_FINDER.indexIn(sql, selectIndex);
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

    public static List<String> getExpressionItems(CharSequence sql, int i, int end, SelectItemExtractor extractor) {
        List<String> selectItems = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int parenthesis = 0;
        QuoteMode mode = QuoteMode.NONE;
        int fromIndex = end;

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
                    end = sql.length();

                    parenthesis++;
                } else if (c == ')') {
                    // When we leave the context, reset the end to the from index
                    if (i < fromIndex) {
                        end = fromIndex;
                    } else {
                        // If the found from was in the subcontext, find the next from
                        end = fromIndex = FROM_FINDER.indexIn(sql, i);
                        // from-less query
                        if (fromIndex == -1) {
                            end = fromIndex = sql.length();
                        }
                    }

                    parenthesis--;
                }
            }

            sb.append(c);
            i++;
        }

        String lastAlias = extractor.extract(sb, selectItems.size(), i);
        if (!lastAlias.isEmpty()) {
            selectItems.add(lastAlias);
        }

        return selectItems;
    }

    public static CharSequence getSetElementSequence(CharSequence sql) {
        int setIndex = SET_FINDER.indexIn(sql);
        if (setIndex == -1) {
            return null;
        }

        setIndex += SET.length();
        int whereIndex = indexOfWhere(sql);
        if (whereIndex == -1) {
            whereIndex = sql.length();
        }

        return sql.subSequence(setIndex, whereIndex);
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
        if (withIndex == -1) {
            return selectIndex;
        }

        int brackets = 0;
        QuoteMode mode = QuoteMode.NONE;
        int i = withIndex + WITH.length();
        int end = selectIndex;
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
                        if (i < selectIndex) {
                            end = selectIndex;
                        } else {
                            // If the found select was in the subcontext, find the next select
                            end = selectIndex = SELECT_FINDER.indexIn(sql, i);
                        }
                    }
                }
            }

            i++;
        }

        return selectIndex;
    }

    /**
     * Finds the toplevel WHERE keyword in an arbitrary query.
     *
     * @param sql The SQL query
     * @return The index of the SELECT keyword if found, or -1
     */
    public static int indexOfWhere(CharSequence sql) {
        int whereIndex = WHERE_FINDER.indexIn(sql);
        int brackets = 0;
        QuoteMode mode = QuoteMode.NONE;
        int i = 0;
        int end = whereIndex;
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
                        if (i < whereIndex) {
                            end = whereIndex;
                        } else {
                            // If the found select was in the subcontext, find the next select
                            end = whereIndex = WHERE_FINDER.indexIn(sql, i);
                        }
                    }
                }
            }

            i++;
        }

        return whereIndex;
    }

    /**
     * Finds the toplevel ORDER BY keyword in an arbitrary query.
     *
     * @param sql The SQL query
     * @return The index of the SELECT keyword if found, or -1
     */
    public static int indexOfOrderBy(CharSequence sql) {
        int orderByIndex = ORDER_BY_FINDER.indexIn(sql);
        int brackets = 0;
        QuoteMode mode = QuoteMode.NONE;
        int i = 0;
        int end = orderByIndex;
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
                        if (i < orderByIndex) {
                            end = orderByIndex;
                        } else {
                            // If the found select was in the subcontext, find the next select
                            end = orderByIndex = ORDER_BY_FINDER.indexIn(sql, i);
                        }
                    }
                }
            }

            i++;
        }

        return orderByIndex;
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
                        return new int[]{fromFinalTableIndex + FROM_FINAL_TABLE.length(), i};
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

        int asIndex = AS_FINDER.indexIn(sb, aliasBeginCharIndex - 1);
        if (asIndex != -1) {
            aliasBeginCharIndex = skipWhitespaces(sb, asIndex + AS.length());
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
