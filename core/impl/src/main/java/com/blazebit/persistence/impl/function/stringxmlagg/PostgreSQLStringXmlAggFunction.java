/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.stringxmlagg;

import com.blazebit.persistence.impl.util.JpqlFunctionUtil;
import com.blazebit.persistence.spi.FunctionRenderContext;

/**
 * @author Christian Beikov
 * @since 1.5.0
 */
public class PostgreSQLStringXmlAggFunction extends AbstractStringXmlAggFunction {

    @Override
    public void render(FunctionRenderContext context) {
        if ((context.getArgumentsSize() & 1) == 1) {
            throw new RuntimeException("The string_xml_agg function needs an even amount of arguments <key1>, <value1>, ..., <keyN>, <valueN>! args=" + context);
        }
        context.addChunk("xmlagg(xmlelement(name e");
        for (int i = 0; i < context.getArgumentsSize(); i++) {
            if ((i & 1) == 1) {
                context.addChunk(", '' || ");
                context.addArgument(i);
                context.addChunk(")");
            } else {
                context.addChunk(", xmlelement(name ");
                context.addChunk(JpqlFunctionUtil.unquoteSingleQuotes(context.getArgument(i)));
            }
        }
        context.addChunk("))");
    }

}