package com.blazebit.persistence.view.impl.macro;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.spi.JpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class ViewRootJpqlMacro implements JpqlMacro {

    private final String viewRoot;

    public ViewRootJpqlMacro(String viewRoot) {
        this.viewRoot = viewRoot;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() != 1) {
            throw new IllegalArgumentException("The VIEW_ROOT macro requires exactly one argument: <expression>!");
        }

        if (viewRoot != null && !viewRoot.isEmpty()) {
            context.addChunk(viewRoot);
            context.addChunk(".");
        }

        context.addArgument(0);
    }
}
