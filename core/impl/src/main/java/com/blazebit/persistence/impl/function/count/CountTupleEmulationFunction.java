package com.blazebit.persistence.impl.function.count;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CountTupleEmulationFunction extends AbstractCountFunction {

    private static String DISTINCT = "distinct ";
    private final boolean ANSI_SQL = true;

    @Override
    public void render(FunctionRenderContext context) {
        Count count = getCount(context);

        context.addChunk("count(");

        if (count.isDistinct()) {
            context.addChunk(DISTINCT);
        }

        int argumentStartIndex = count.getArgumentStartIndex();

        if (count.getCountArgumentSize() > 1) {
            // see https://hibernate.atlassian.net/browse/HHH-11042 for the workaround description
            if (ANSI_SQL) {
                if (count.isDistinct()) {
                    // NULL -> \0
                    // '' -> \0 + argumentNumber
                    //count(distinct coalesce(nullif(coalesce(col1 || '', '\0'), ''), '\01') || '\0' || coalesce(nullif(coalesce(col2 || '', '\0'), ''), '\02'))
                    context.addChunk("coalesce(nullif(coalesce(");
                    context.addArgument(argumentStartIndex);
                    int argumentNumber = 1;
                    for (int i = argumentStartIndex + 1; i < context.getArgumentsSize(); i++, argumentNumber++) {
                        // Concat with empty string to get implicit conversion
                        context.addChunk(" || ''");
                        context.addChunk(", '\\0'), ''), '\\0" + argumentNumber + "') || '\\0' || coalesce(nullif(coalesce(");
                        context.addArgument(i);
                    }
                    // Concat with empty string to get implicit conversion
                    context.addChunk(" || ''");
                    context.addChunk(", '\\0'), ''), '\\0" + argumentNumber + "')");
                } else {
                    context.addChunk("case when ");
                    context.addArgument(argumentStartIndex);
                    context.addChunk(" is null");
                    for (int i = argumentStartIndex + 1; i < context.getArgumentsSize(); i++) {
                        context.addChunk(" or ");
                        context.addArgument(i);
                        context.addChunk(" is null");
                    }
                    context.addChunk(" then null else 1 end");
                }
            } else {
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
                context.addChunk(" end");

                // Count nulls
                context.addChunk(") + count(case when ");
                context.addArgument(argumentStartIndex);
                context.addChunk(" is null");
                for (int i = argumentStartIndex + 1; i < context.getArgumentsSize(); i++) {
                    context.addChunk(" or ");
                    context.addArgument(i);
                    context.addChunk(" is null");
                }
                context.addChunk(" then 1 end");
            }
        } else {
            context.addArgument(argumentStartIndex);
        }

        context.addChunk(")");
    }

}
