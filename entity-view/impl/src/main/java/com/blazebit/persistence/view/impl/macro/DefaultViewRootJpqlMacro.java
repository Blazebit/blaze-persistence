/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.macro;

import com.blazebit.persistence.spi.CacheableJpqlMacro;
import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.view.spi.ViewRootJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class DefaultViewRootJpqlMacro implements ViewRootJpqlMacro, CacheableJpqlMacro {

    private final String viewRoot;

    public DefaultViewRootJpqlMacro(String viewRoot) {
        this.viewRoot = viewRoot;
    }

    @Override
    public String getViewRoot() {
        return viewRoot;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() > 1) {
            throw new IllegalArgumentException("The VIEW_ROOT macro allows maximally one argument: <expression>!");
        }

        if (viewRoot == null) {
            if (context.getArgumentsSize() > 0) {
                context.addChunk(".");
                context.addArgument(0);
            }
        } else {
            context.addChunk(viewRoot);
            if (context.getArgumentsSize() > 0) {
                context.addChunk(".");
                context.addArgument(0);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DefaultViewRootJpqlMacro)) {
            return false;
        }

        DefaultViewRootJpqlMacro that = (DefaultViewRootJpqlMacro) o;

        return viewRoot != null ? viewRoot.equals(that.viewRoot) : that.viewRoot == null;
    }

    @Override
    public int hashCode() {
        return viewRoot != null ? viewRoot.hashCode() : 0;
    }
}
