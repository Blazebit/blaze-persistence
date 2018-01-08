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

package com.blazebit.persistence.impl.function.limit;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.DbmsStatementType;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class LimitFunction implements JpqlFunction {

    protected final DbmsDialect dbmsDialect;
    protected final boolean limitIncludesOffset;

    public LimitFunction(DbmsDialect dbmsDialect) {
        // LIMIT(SUBQUERY, LIMIT, OFFSET)
        this.dbmsDialect = dbmsDialect;
        this.limitIncludesOffset = dbmsDialect.createLimitHandler().limitIncludesOffset();
    }

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
        switch (functionRenderContext.getArgumentsSize()) {
            case 3:
                if (isNotNull(functionRenderContext.getArgument(1)) && isNotNull(functionRenderContext.getArgument(2))) {
                    renderLimitOffset(functionRenderContext);
                    return;
                }

                break;
            case 2:
                if (isNotNull(functionRenderContext.getArgument(1))) {
                    renderLimitOnly(functionRenderContext);
                    return;
                }

                break;
            default:
                break;
        }

        throw new RuntimeException("The limit function needs two or three non null arguments <sub_query>, <limit> and optionally <offset>! args="
            + functionRenderContext);
    }

    protected void renderLimitOffset(FunctionRenderContext functionRenderContext) {
        StringBuilder sqlSb = getSql(functionRenderContext);
        if (limitIncludesOffset) {
            // Careful, parameters are not supported in this case as that would require parameter rewriting or something like that
            String limit = functionRenderContext.getArgument(1);
            String offset = functionRenderContext.getArgument(2);
            if (limit.contains("?") || offset.contains("?")) {
                throw new IllegalArgumentException("Limit and offset in subquery can not be a parameter!");
            }
            Integer limitValue = Integer.parseInt(limit);
            Integer offsetValue = Integer.parseInt(offset);
            dbmsDialect.appendExtendedSql(sqlSb, DbmsStatementType.SELECT, true, false, null, Integer.toString(limitValue + offsetValue), offset, null, null);
        } else {
            dbmsDialect.appendExtendedSql(sqlSb, DbmsStatementType.SELECT, true, false, null, functionRenderContext.getArgument(1), functionRenderContext.getArgument(2), null, null);
        }
        functionRenderContext.addChunk(sqlSb.toString());
    }

    protected void renderLimitOnly(FunctionRenderContext functionRenderContext) {
        StringBuilder sqlSb = getSql(functionRenderContext);
        dbmsDialect.appendExtendedSql(sqlSb, DbmsStatementType.SELECT, true, false, null, functionRenderContext.getArgument(1), null, null, null);
        functionRenderContext.addChunk(sqlSb.toString());
    }

    private static boolean isNotNull(String argument) {
        return argument != null && !"NULL".equalsIgnoreCase(argument);
    }
    
    private static StringBuilder getSql(FunctionRenderContext functionRenderContext) {
        String subquery = functionRenderContext.getArgument(0);
        if (startsWithIgnoreCase(subquery, "(select")) {
            int endIndex = subquery.length() - (subquery.charAt(subquery.length() - 1) == ')' ? 1 : 0);
            return new StringBuilder(subquery.length() - 2).append(subquery, 1, endIndex);
        }
        
        return new StringBuilder(subquery);
    }

    private static boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.regionMatches(true, 0, s2, 0, s2.length());
    }

}
