/*
 * Copyright 2014 - 2023 Blazebit.
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

package com.blazebit.persistence.impl.function.tostringjson;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.parser.JsonParser;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public abstract class AbstractToStringJsonFunction implements JpqlFunction {

    public static final String FUNCTION_NAME = "to_string_json";

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
        return String.class;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() < 2) {
            throw new RuntimeException("The to_string_json function needs at least two arguments <subquery>, <key1>, ..., <keyN>! args=" + context);
        }

        String subquery = context.getArgument(0);
        int fromIndex = SqlUtils.indexOfFrom(subquery, 1);
        String[] selectItemExpressions = SqlUtils.getSelectItemExpressions(subquery, SqlUtils.SELECT_FINDER.indexIn(subquery));
        String[] fields = new String[context.getArgumentsSize() - 1];
        if (selectItemExpressions.length < fields.length) {
            throw new RuntimeException("The to_string_json function <subquery> argument must have at least as many select items as keys are given! args=" + context);
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i] = JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(i + 1));
        }
        render(context, fields, selectItemExpressions, subquery, fromIndex);
    }

    public Object process(CharSequence result, String[] fields) {
        if (result == null) {
            return null;
        }
        return JsonParser.parseStringOnly(result, fields);
    }

    public abstract void render(FunctionRenderContext context, String[] fields, String[] selectItemExpressions, String subquery, int fromIndex);
}