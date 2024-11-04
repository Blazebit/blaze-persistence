/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.impl.function.jsonget;

import com.blazebit.persistence.impl.function.concat.ConcatFunction;
import com.blazebit.persistence.impl.function.jsonset.AbstractJsonFunction;
import com.blazebit.persistence.spi.FunctionRenderContext;
import java.util.List;

/**
 * This function does not support parameterized JSON path templates because the json_value and json_query
 * functions in Oracle only work with literals.
 *
 * @author Moritz Becker
 * @since 1.5.0
 */
public class OracleJsonGetFunction extends AbstractJsonGetFunction {

    public OracleJsonGetFunction(ConcatFunction concatFunction) {
        super(concatFunction);
    }

    @Override
    protected void render0(FunctionRenderContext context) {
        List<Object> jsonPathElements = AbstractJsonFunction.retrieveJsonPathElements(context, 1);
        String jsonPathTemplate = AbstractJsonFunction.toJsonPathTemplate(jsonPathElements, jsonPathElements.size(), true);

        context.addChunk("coalesce(json_value(");
        context.addArgument(0);
        context.addChunk(" format json,");
        renderJsonPathTemplate(context, jsonPathTemplate, jsonPathElements.size() + 1);
        context.addChunk("),nullif(json_query(");
        context.addArgument(0);
        context.addChunk(" format json,");
        renderJsonPathTemplate(context, jsonPathTemplate, jsonPathElements.size() + 1);
        context.addChunk("),'null'))");
    }
}
