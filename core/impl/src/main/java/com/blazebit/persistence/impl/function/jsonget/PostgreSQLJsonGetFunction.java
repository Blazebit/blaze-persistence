/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.function.jsonget;

import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.jsonset.AbstractJsonFunction;
import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.FunctionRenderContext;
import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class PostgreSQLJsonGetFunction extends AbstractJsonGetFunction {

    public PostgreSQLJsonGetFunction(ConcatFunction concatFunction) {
        super(concatFunction);
    }

    @Override
    protected void render0(FunctionRenderContext context) {
        List<Object> jsonPathElements = AbstractJsonFunction.retrieveJsonPathElements(context, 1);
        Object firstArgument = jsonPathElements.get(0);
        if (firstArgument instanceof String && isJsonPathTemplate((String) firstArgument)) {
            String jsonPathTemplate = AbstractJsonFunction.toJsonPathTemplate(jsonPathElements, jsonPathElements.size(), false);
            context.addChunk("jsonb_path_query(cast(");
            context.addArgument(0);
            context.addChunk(" as jsonb),cast(");
            renderJsonPathTemplate(context, jsonPathTemplate, jsonPathElements.size() + 1);
            context.addChunk(" as jsonpath))");
        } else {
            context.addChunk("cast(");
            context.addArgument(0);
            context.addChunk(" as json)");
            context.addChunk("#>>'{");
            addUnquotedArgument(context, 1);
            for (int i = 2; i < context.getArgumentsSize(); i++) {
                context.addChunk(",");
                addUnquotedArgument(context, i);
            }
            context.addChunk("}'");
        }
    }

    private void addUnquotedArgument(FunctionRenderContext context, int argIndex) {
        context.addChunk(JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(argIndex)));
    }
}
