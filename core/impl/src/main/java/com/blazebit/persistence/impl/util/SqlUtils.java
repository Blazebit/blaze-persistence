package com.blazebit.persistence.impl.util;

import java.util.ArrayList;
import java.util.List;

public class SqlUtils {

    private static final String SELECT = "select ";
    private static final String FROM = " from ";
    private static final String WITH = "with ";
    private static final String FROM_FINAL_TABLE = " from final table (";
    private static final String NEXT_VALUE_FOR = "next value for ";
    private static final PatternFinder SELECT_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(SELECT);
    private static final PatternFinder FROM_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM);
    private static final PatternFinder WITH_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(WITH);
    private static final PatternFinder FROM_FINAL_TABLE_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM_FINAL_TABLE);
    private static final PatternFinder NEXT_VALUE_FOR_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(NEXT_VALUE_FOR);

    private SqlUtils() {
    }

    public static int countSelectItems(CharSequence sql) {
        int count = 1;
        int parenthesis = 0;
        for (int i = 0; i < sql.length(); i++) {
            final char c = sql.charAt(i);

            if (c == '(') {
                parenthesis++;
            } else if (c == ')') {
                parenthesis--;
            } else if (parenthesis == 0 && c == ',') {
                count++;
            }
        }

        return count;
    }

    public static String[] getSelectItemAliases(CharSequence sql, int start) {
        int selectIndex = SELECT_FINDER.indexIn(sql, start);
        int fromIndex = FROM_FINDER.indexIn(sql, selectIndex);
        // from-less query
        if (fromIndex == -1) {
            fromIndex = sql.length();
        }

        List<String> selectAliases = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        int parenthesis = 0;
        boolean text = false;

        int i = selectIndex + SELECT.length();
        int end = fromIndex;
        while (i < end) {
            final char c = sql.charAt(i);

            if (text) {
                if (parenthesis == 0 && c == ',') {
                    selectAliases.add(extractAlias(sb, selectAliases.size()));
                    sb.setLength(0);
                    text = false;
                } else {
                    sb.append(c);
                }
            } else {
                if (Character.isWhitespace(c)) {
                    // skip whitespace
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

                sb.append(c);
                text = true;
            }

            i++;
        }

        if (text) {
            selectAliases.add(extractAlias(sb, selectAliases.size()));
        }

        return selectAliases.toArray(new String[selectAliases.size()]);
    }

    public static int indexOfSelect(CharSequence sql) {
        int selectIndex = SELECT_FINDER.indexIn(sql);
        int withIndex = WITH_FINDER.indexIn(sql, 0, selectIndex);
        if (withIndex == -1) {
            return selectIndex;
        }

        int brackets = 0;
        int i = withIndex + WITH.length() + 1;
        int end = selectIndex;
        while (i < end) {
            final char c = sql.charAt(i);
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
            i++;
        }

        return selectIndex;
    }

    public static int[] indexOfFinalTableSubquery(CharSequence sql, int selectIndex) {
        int fromFinalTableIndex = FROM_FINAL_TABLE_FINDER.indexIn(sql, selectIndex);
        if (fromFinalTableIndex == -1) {
            return new int[] { 0, sql.length() };
        }
        int brackets = 1;
        int i = fromFinalTableIndex + FROM_FINAL_TABLE.length();
        int end = sql.length();
        while (i < end) {
            final char c = sql.charAt(i);
            if (c == '(') {
                brackets++;
            } else if (c == ')') {
                brackets--;

                if (brackets == 0) {
                    return new int[] { fromFinalTableIndex + FROM_FINAL_TABLE.length(), i };
                }
            }
            i++;
        }

        return new int[] { 0, sql.length() };
    }

    private static String extractAlias(StringBuilder sb, int index) {
        int aliasEndCharIndex = findLastNonWhitespace(sb);
        int aliasBeforeIndex = findLastWhitespace(sb, aliasEndCharIndex);
        if (aliasBeforeIndex < 0) {
            aliasBeforeIndex = sb.lastIndexOf(".");
        }
        if (NEXT_VALUE_FOR_FINDER.indexIn(sb) != -1) {
            // Since sequences in subqueries might not be allowed, we pass the whole expression
            return sb.toString();
        }
        return sb.substring(aliasBeforeIndex + 1, aliasEndCharIndex + 1);
    }

    private static int findLastNonWhitespace(StringBuilder sb) {
        int i = sb.length() - 1;
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
