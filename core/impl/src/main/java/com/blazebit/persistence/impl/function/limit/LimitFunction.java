package com.blazebit.persistence.impl.function.limit;

import com.blazebit.persistence.impl.function.TemplateRenderer;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.1
 */
public class LimitFunction implements JpqlFunction {

    protected final TemplateRenderer limitOnlyRenderer;
    protected final TemplateRenderer limitOffsetRenderer;

    public LimitFunction() {
        // LIMIT(SUBQUERY, LIMIT, OFFSET)
        this("(?1 limit ?2)", "(?1 limit ?2 offset ?3)");
    }

    protected LimitFunction(String limitOnly, String limitOffset) {
        // LIMIT(SUBQUERY, LIMIT, OFFSET)
        this.limitOnlyRenderer = new TemplateRenderer(limitOnly);
        this.limitOffsetRenderer = new TemplateRenderer(limitOffset);
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
        return firstArgumentType;
    }

    @Override
    public void render(FunctionRenderContext functionRenderContext) {
        switch (functionRenderContext.getArgumentsSize()) {
            case 3:
                if (isNotNull(functionRenderContext.getArgument(1)) && isNotNull(functionRenderContext.getArgument(2))) {
                    renderLimitOffset(functionRenderContext);
                    return;
                }

                break;
            case 2:
                if (isNotNull(functionRenderContext.getArgument(1))) {
                    renderLimitOnly(functionRenderContext);
                    return;
                }

                break;
            default:
                break;
        }

        throw new RuntimeException("The limit function needs two or three non null arguments <sub_query>, <limit> and optionally <offset>! args="
            + functionRenderContext);
    }

    protected void renderLimitOffset(FunctionRenderContext functionRenderContext) {
        adapt(functionRenderContext, limitOffsetRenderer).addArgument(1).addArgument(2).build();
    }

    protected void renderLimitOnly(FunctionRenderContext functionRenderContext) {
        adapt(functionRenderContext, limitOnlyRenderer).addArgument(1).build();
    }

    private static boolean isNotNull(String argument) {
        return argument != null && !"NULL".equalsIgnoreCase(argument);
    }

    protected static TemplateRenderer.Context adapt(FunctionRenderContext functionRenderContext, TemplateRenderer renderer) {
        TemplateRenderer.Context context = renderer.start(functionRenderContext);
        String subquery = functionRenderContext.getArgument(0);
        if (startsWithIgnoreCase(subquery, "(select")) {
            int endIndex = subquery.length() - (subquery.charAt(subquery.length() - 1) == ')' ? 1 : 0);
            context.addParameter(subquery.substring(1, endIndex));
        } else {
            context.addArgument(0);
        }

        return context;
    }

    private static boolean startsWithIgnoreCase(String s1, String s2) {
        return s1.regionMatches(true, 0, s2, 0, s2.length());
    }

}
