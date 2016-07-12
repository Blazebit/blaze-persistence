package com.blazebit.persistence.impl.function.cast;

import com.blazebit.persistence.spi.DbmsDialect;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.spi.TemplateRenderer;

public class CastFunction implements JpqlFunction {

    private final String functionName;
    private final Class<?> castType;
    private final TemplateRenderer renderer;

    public CastFunction(Class<?> castType, DbmsDialect dbmsDialect) {
        this.functionName = "CAST_" + castType.getSimpleName().toUpperCase();
        this.castType = castType;
        this.renderer = new TemplateRenderer("cast(?1 as " + dbmsDialect.getSqlType(castType) + ")");
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
        return castType;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 1) {
            throw new RuntimeException("The " + functionName + " function needs exactly one argument <expression>! args=" + context);
        }
        renderer.start(context).addArgument(0).build();
    }

}
