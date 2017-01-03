/*
 * Copyright 2014 - 2017 Blazebit.
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
    private static final String FROM = " from ";
    private static final String WITH = "with ";
    private static final String WHERE = " where ";
    private static final String AS = " as ";
    private static final String FROM_FINAL_TABLE = " from final table (";
    private static final String NEXT_VALUE_FOR = "next value for ";
    private static final PatternFinder SELECT_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(SELECT));
    private static final PatternFinder FROM_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM));
    private static final PatternFinder WITH_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(WITH));
    private static final PatternFinder WHERE_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(WHERE));
    private static final PatternFinder AS_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiLastPatternFinder(AS));
    private static final PatternFinder FROM_FINAL_TABLE_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM_FINAL_TABLE));
    private static final PatternFinder NEXT_VALUE_FOR_FINDER = new QuotedIdentifierAwarePatternFinder(new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(NEXT_VALUE_FOR));

    public static interface SelectItemExtractor {
        public String extract(StringBuilder sb, int index, int currentPosition);
    }

    private static final SelectItemExtractor ALIAS_EXTRACTOR = new SelectItemExtractor() {
        @Override
        public String extract(StringBuilder sb, int index, int currentPosition) {
            return extractAlias(sb, index);
        }
    };

    private static final SelectItemExtractor EXPRESSION_EXTRACTOR = new SelectItemExtractor() {
        @Override
        public String extract(StringBuilder sb, int index, int currentPosition) {
            return extractExpression(sb, index);
        }
    };

    private SqlUtils() {
    }

    /**
     * Counts select items of a select clause.
     *
     * This method should be invoked with the select clause part of a SQL query.
     * That is e.g. <code>col1, col2</code> of <code>SELECT col1, col2 FROM ...</code>.
     *
     * @param sql The select clause part of a SQL query
     * @return The item count
     */
    public static int countSelectItems(CharSequence sql) {
        int count = 1;
        int parenthesis = 0;
        QuoteMode mode = QuoteMode.NONE;
        for (int i = 0; i < sql.length(); i++) {
            final char c = sql.charAt(i);
            mode = mode.onChar(c);

            if (mode == QuoteMode.NONE) {
                if (c == '(') {
                    parenthesis++;
                } else if (c == ')') {
                    parenthesis--;
                } else if (parenthesis == 0 && c == ',') {
                    count++;
                }
            }
        }

        return count;
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

    public static String[] getSelectItems(CharSequence sql, int start, SelectItemExtractor extractor) {
        int selectIndex = SELECT_FINDER.indexIn(sql, start);
        int fromIndex = FROM_FINDER.indexIn(sql, selectIndex);
        // from-less query
        if (fromIndex == -1) {
            fromIndex = sql.length();
        }

        List<String> selectItems = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int parenthesis = 0;
        QuoteMode mode = QuoteMode.NONE;

        int i = selectIndex + SELECT.length();
        int end = fromIndex;
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

        return selectItems.toArray(new String[selectItems.size()]);
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

    public static String extractAlias(StringBuilder sb, int index) {
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

    private static String extractExpression(StringBuilder sb, int index) {
        int asIndex = AS_FINDER.indexIn(sb);
        if (asIndex == -1) {
            return sb.toString();
        }

        return sb.substring(0, asIndex);
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
