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
 * @author Moritz Becker
 * @since 1.5.0
 */
public class MySQLJsonGetFunction extends AbstractJsonGetFunction {

    public MySQLJsonGetFunction(ConcatFunction concatFunction) {
        super(concatFunction);
    }

    @Override
    protected void render0(FunctionRenderContext context) {
        List<Object> jsonPathElements = AbstractJsonFunction.retrieveJsonPathElements(context, 1);
        String jsonPathTemplate = AbstractJsonFunction.toJsonPathTemplate(jsonPathElements, jsonPathElements.size(), true);

        context.addChunk("json_unquote(nullif(json_extract(");
        context.addArgument(0);
        context.addChunk(",");
        renderJsonPathTemplate(context, jsonPathTemplate, jsonPathElements.size() + 1);
        context.addChunk("),cast('null' as json)))");
    }
}
