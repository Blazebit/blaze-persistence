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

package com.blazebit.persistence.impl.function.rowvalue;

import com.blazebit.persistence.impl.util.BoyerMooreCaseInsensitiveAsciiFirstPatternFinder;
import com.blazebit.persistence.impl.util.BoyerMooreCaseInsensitiveAsciiLastPatternFinder;
import com.blazebit.persistence.impl.util.PatternFinder;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

import java.util.regex.Pattern;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class RowValueComparisonFunction implements JpqlFunction {
    public static final String FUNCTION_NAME = "compare_row_value";
    private static final String AND = " and ";
    private static final String THEN = " then ";
    private static final PatternFinder AND_FINDER = new BoyerMooreCaseInsensitiveAsciiFirstPatternFinder(AND);
    private static final PatternFinder THEN_FINDER = new BoyerMooreCaseInsensitiveAsciiLastPatternFinder(THEN);
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*(and|=)\\s*", Pattern.CASE_INSENSITIVE);
    private static final Pattern NULLIF_PATTERN = Pattern.compile("nullif\\(1,\\s*1\\)", Pattern.CASE_INSENSITIVE);

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
        return boolean.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        String operator = context.getArgument(0);
        // need to unquote operator
        operator = operator.substring(1, operator.length() - 1);
        context.addChunk(getLeftmostChunk());
        String caseWhenExpression = context.getArgument(1);

        // Seek to first character index
        int firstIndex = AND_FINDER.indexIn(caseWhenExpression) + AND.length() - 1;
        for (;; firstIndex++) {
            final char c = caseWhenExpression.charAt(firstIndex);
            if (!Character.isWhitespace(c)) {
                break;
            }
        }

        // Seek to last character index
        int lastIndex = THEN_FINDER.indexIn(caseWhenExpression);
        for (;; lastIndex--) {
            final char c = caseWhenExpression.charAt(lastIndex);
            if (!Character.isWhitespace(c)) {
                lastIndex++;
                break;
            }
        }

        String predicate = caseWhenExpression.substring(firstIndex, lastIndex);
        String[] parts = SPLIT_PATTERN.split(predicate);

        // Let's fix the EclipseLink madness
        for (int i = 0; i < parts.length; i += 2) {
            String p = parts[i];
            String p2 = parts[i + 1];
            if (p.length() > 1 && p2.length() > 2 && p.charAt(0) == '(' && p2.charAt(p2.length() - 1) == ')' && p2.charAt(p2.length() - 2) == ')') {
                parts[i] = p.substring(1);
                parts[i + 1] = p2.substring(0, p2.length() - 2);
            }
        }

        // extract the nullif placeholder expressions and rewire the proper values to the proper indexes
        int lastNullIndex = parts.length - 1;
        for (int i = lastNullIndex; i >= 0; i -= 2) {
            if ("nullif(".regionMatches(true, 0, parts[i], 0, "nullif(".length()) && NULLIF_PATTERN.matcher(parts[i]).matches()) {
                parts[i] = parts[lastNullIndex];
                lastNullIndex -= 2;
            }
        }

        int rowValueArity = (lastNullIndex + 1) / 2;
        int end = rowValueArity * 2;

        context.addChunk(parts[0]);
        for (int i = 2; i < end; i += 2) {
            context.addChunk(", ");
            context.addChunk(parts[i]);
        }

        context.addChunk(") " + operator + " (");

        context.addChunk(parts[1]);
        for (int i = 3; i < end; i += 2) {
            context.addChunk(", ");
            context.addChunk(parts[i]);
        }

        context.addChunk(getRightmostChunk());
    }

    protected String getLeftmostChunk() {
        return "((";
    }

    protected String getRightmostChunk() {
        return "))";
    }

}