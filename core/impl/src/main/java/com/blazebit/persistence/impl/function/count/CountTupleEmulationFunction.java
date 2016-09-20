package com.blazebit.persistence.impl.function.count;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CountTupleEmulationFunction extends AbstractCountFunction {

    private static String DISTINCT = "distinct ";

    @Override
    public void render(FunctionRenderContext context) {
        Count count = getCount(context);

        context.addChunk("count(");

        if (count.isDistinct()) {
            context.addChunk(DISTINCT);
        }

        int argumentStartIndex = count.getArgumentStartIndex();

        if (context.getArgumentsSize() > 1) {
            // see https://hibernate.atlassian.net/browse/HHH-11042 for the workaround description
            //count(distinct case when col1 is null or col2 is null then null else col1 || '\0' || col2 end) + count(case when col1 is null or col2 is null then 1 end)
            context.addChunk("case when ");
            context.addArgument(argumentStartIndex);
            context.addChunk(" is null");
            for (int i = argumentStartIndex + 1; i < context.getArgumentsSize(); i++) {
                context.addChunk(" or ");
                context.addArgument(i);
                context.addChunk(" is null");
            }
            context.addChunk(" then null else ");

            context.addArgument(argumentStartIndex);
            for (int i = argumentStartIndex + 1; i < context.getArgumentsSize(); i++) {
                context.addChunk(" || '\\0' || ");
                context.addArgument(i);
            }
            context.addChunk(" end) + count(case when ");
            context.addArgument(argumentStartIndex);
            context.addChunk(" is null");
            for (int i = argumentStartIndex + 1; i < context.getArgumentsSize(); i++) {
                context.addChunk(" or ");
                context.addArgument(i);
                context.addChunk(" is null");
            }
            context.addChunk(" then 1 end");
        } else {
            context.addArgument(argumentStartIndex);
        }

        context.addChunk(")");
    }

}
