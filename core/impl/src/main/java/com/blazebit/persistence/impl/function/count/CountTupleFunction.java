package com.blazebit.persistence.impl.function.count;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class CountTupleFunction extends AbstractCountFunction {

    private static String DISTINCT = "distinct ";

    @Override
    public void render(FunctionRenderContext context) {
        Count count = getCount(context);

        context.addChunk("count(");

        if (count.isDistinct()) {
            context.addChunk(DISTINCT);
        }

        int argumentStartIndex = count.getArgumentStartIndex();

        if (count.getCountArgumentSize() > 1) {
            context.addChunk("(");
            context.addArgument(argumentStartIndex);
            for (int i = argumentStartIndex + 1; i < context.getArgumentsSize(); i++) {
                context.addChunk(", ");
                context.addArgument(i);
            }
            context.addChunk(")");
        } else {
            context.addArgument(argumentStartIndex);
        }

        context.addChunk(")");
    }
}
