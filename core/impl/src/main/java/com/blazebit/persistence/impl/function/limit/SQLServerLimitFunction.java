/*
 * Copyright 2015 Blazebit.
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
package com.blazebit.persistence.impl.function.limit;

import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class SQLServerLimitFunction extends LimitFunction {

    public SQLServerLimitFunction() {
    }

    @Override
    protected void renderLimitOnly(FunctionRenderContext functionRenderContext) {
        String query = getQuery(functionRenderContext);
        addTop(functionRenderContext, query);
    }

    @Override
    protected void renderLimitOffset(FunctionRenderContext functionRenderContext) {
        // String query = getQuery(functionRenderContext);

    }

    private String getQuery(FunctionRenderContext functionRenderContext) {
        String query = functionRenderContext.getArgument(0);

        if (query.charAt(query.length() - 1) == ';') {
            return query.substring(0, query.length() - 1);
        }

        return query;
    }

    private static void addTop(FunctionRenderContext functionRenderContext, final String query) {
        final String upperQuery = query.toUpperCase();
        final int fromIndex = indexOfIgnoreCase(upperQuery, "FROM");
        final int distinctStartPos = indexOfUntil(query, "DISTINCT", fromIndex);

        if (distinctStartPos > 0) {
            // Place TOP after DISTINCT.
            functionRenderContext.addChunk(query.substring(0, distinctStartPos + "DISTINCT".length()));
            functionRenderContext.addChunk(" TOP(");
            functionRenderContext.addArgument(1);
            functionRenderContext.addChunk(")");
            functionRenderContext.addChunk(query.substring(distinctStartPos + "DISTINCT".length()));
        } else {
            final int selectStartPos = indexOfUntil(query, "SELECT", fromIndex);
            // Place TOP after SELECT.
            functionRenderContext.addChunk(query.substring(0, selectStartPos + "SELECT".length()));
            functionRenderContext.addChunk(" TOP(");
            functionRenderContext.addArgument(1);
            functionRenderContext.addChunk(")");
            functionRenderContext.addChunk(query.substring(selectStartPos + "SELECT".length()));
        }
    }

    private static int indexOfIgnoreCase(String string, String search) {
        return string.indexOf(search);
    }

    private static int indexOfUntil(String string, String search, int endIndex) {
        String subString = string.substring(0, endIndex);
        return subString.indexOf(search);
    }
}
