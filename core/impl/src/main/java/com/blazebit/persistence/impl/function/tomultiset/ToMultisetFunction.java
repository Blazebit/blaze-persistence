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

package com.blazebit.persistence.impl.function.tomultiset;

import com.blazebit.persistence.impl.SubqueryInternalBuilder;
import com.blazebit.persistence.impl.function.tostringjson.AbstractToStringJsonFunction;
import com.blazebit.persistence.impl.function.tostringxml.AbstractToStringXmlFunction;
import com.blazebit.persistence.impl.util.SqlUtils;
import com.blazebit.persistence.parser.expression.Subquery;
import com.blazebit.persistence.parser.expression.SubqueryExpression;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.JpqlFunctionProcessor;

import java.util.List;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class ToMultisetFunction implements JpqlFunction, JpqlFunctionProcessor<CharSequence> {

    public static final String FUNCTION_NAME = "to_multiset";

    private final AbstractToStringJsonFunction toJsonFunction;
    private final AbstractToStringXmlFunction toXmlFunction;

    public ToMultisetFunction(AbstractToStringJsonFunction toJsonFunction, AbstractToStringXmlFunction toXmlFunction) {
        this.toJsonFunction = toJsonFunction;
        this.toXmlFunction = toXmlFunction;
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
        return String.class;
    }

    @Override
    public Object process(CharSequence result, List<Object> arguments) {
        SubqueryExpression subqueryExpression = (SubqueryExpression) arguments.get(0);
        Subquery subquery = subqueryExpression.getSubquery();
        String[] fields;
        if (subquery instanceof SubqueryInternalBuilder<?>) {
            fields = createFields(((SubqueryInternalBuilder<?>) subquery).getSelectExpressions().size());
        } else {
            throw new IllegalArgumentException("Can't handle: " + subquery);
        }
        if (toJsonFunction == null) {
            return toXmlFunction.process(result, fields);
        } else {
            return toJsonFunction.process(result, fields);
        }
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 1) {
            throw new RuntimeException("The to_multiset function needs exactly one argument <subquery>! args=" + context);
        }

        String subquery = context.getArgument(0);
        int fromIndex = SqlUtils.indexOfFrom(subquery, 1);
        String[] selectItemExpressions;
        if (subquery.startsWith("(select * from (")) {
            // For Oracle we need a select wrapper with rownum calculation for for LIMIT/OFFSET support
            selectItemExpressions = SqlUtils.getSelectItemAliases(subquery, SqlUtils.SELECT_FINDER.indexIn(subquery, "(select * from (".length()));
        } else {
            selectItemExpressions = SqlUtils.getSelectItemExpressions(subquery, SqlUtils.SELECT_FINDER.indexIn(subquery, 1));
        }
        String[] fields = createFields(selectItemExpressions.length);
        if (toJsonFunction == null) {
            toXmlFunction.render(context, fields, selectItemExpressions, subquery, fromIndex);
        } else {
            toJsonFunction.render(context, fields, selectItemExpressions, subquery, fromIndex);
        }
    }

    private static String[] createFields(int length) {
        String[] fields = new String[length];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = "f" + i;
        }
        return fields;
    }
}