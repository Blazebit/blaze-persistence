package com.blazebit.persistence.testsuite.macro;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PrefixJpqlMacro implements JpqlMacro {

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 2) {
            throw new IllegalArgumentException("The prefix macro requires exactly two arguments: <prefix> and <expression>!");
        }

        context.addArgument(0);
        context.addChunk(".");
        context.addArgument(1);
    }

}
