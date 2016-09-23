package com.blazebit.persistence.impl.util;

import java.util.ArrayList;
import java.util.List;

public class SqlUtils {

    private static final String SELECT = "select ";
    private static final String FROM = " from ";
    private static final String WITH = "with ";
    private static final PatternFinder SELECT_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(SELECT);
    private static final PatternFinder FROM_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(FROM);
    private static final PatternFinder WITH_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(WITH);

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
                    selectAliases.add(extractAlias(sb));
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
            selectAliases.add(extractAlias(sb));
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

    private static String extractAlias(StringBuilder sb) {
        int aliasEndCharIndex = findLastNonWhitespace(sb);
        int aliasBeforeIndex = findLastWhitespace(sb, aliasEndCharIndex);
        if (aliasBeforeIndex < 0) {
            aliasBeforeIndex = sb.lastIndexOf(".");
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
