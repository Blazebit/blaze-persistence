/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.function.jsonget;

import com.blazebit.persistence.impl.function.cast.CastFunction;
import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.jsonset.AbstractJsonFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;
import java.util.List;

/**
 * @author Moritz Becker
 * @since 1.5.0
 */
public class MSSQLJsonGetFunction extends AbstractJsonGetFunction {

    private final CastFunction castToStringFunction;

    public MSSQLJsonGetFunction(ConcatFunction concatFunction, CastFunction castToStringFunction) {
        super(concatFunction);
        this.castToStringFunction = castToStringFunction;
    }

    @Override
    protected void render0(FunctionRenderContext context) {
        List<Object> jsonPathElements = AbstractJsonFunction.retrieveJsonPathElements(context, 1);
        String jsonPathTemplate = AbstractJsonFunction.toJsonPathTemplate(jsonPathElements, jsonPathElements.size(), true);

        // We need to combine json_value and json_query here because json_value is for querying scalar values while
        // json_query is for querying JSON objects and arrays.
        context.addChunk("(select coalesce(json_value(");
        context.addArgument(0);
        context.addChunk(",temp.val),json_query(");
        context.addArgument(0);
        context.addChunk(",temp.val)) from (values(");
        renderJsonPathTemplate(context, jsonPathTemplate, jsonPathElements.size() + 1);
        context.addChunk(")) temp(val))");
    }

    @Override
    protected void renderJsonPathTemplateParameter(FunctionRenderContext context, int parameterIdx) {
        context.addChunk(castToStringFunction.startCastExpression());
        context.addArgument(parameterIdx);
        context.addChunk(castToStringFunction.endCastExpression(null));
    }
}
