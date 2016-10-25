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
        if (viewRoot == null || viewRoot.isEmpty()) {
            throw new IllegalArgumentException("An empty view root is not allowed!");
        }
        this.viewRoot = viewRoot;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() > 1) {
            throw new IllegalArgumentException("The VIEW_ROOT macro allows maximally one argument: <expression>!");
        }

        context.addChunk(viewRoot);
        if (context.getArgumentsSize() > 0) {
            context.addChunk(".");
            context.addArgument(0);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ViewRootJpqlMacro)) {
            return false;
        }

        ViewRootJpqlMacro that = (ViewRootJpqlMacro) o;

        return viewRoot != null ? viewRoot.equals(that.viewRoot) : that.viewRoot == null;

    }

    @Override
    public int hashCode() {
        return viewRoot != null ? viewRoot.hashCode() : 0;
    }
}
