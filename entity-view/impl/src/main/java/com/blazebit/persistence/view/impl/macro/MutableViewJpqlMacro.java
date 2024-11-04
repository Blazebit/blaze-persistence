/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.macro;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.view.spi.ViewJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class MutableViewJpqlMacro implements ViewJpqlMacro {

    private String viewPath;

    public MutableViewJpqlMacro() {
    }

    public MutableViewJpqlMacro(String viewPath) {
        this.viewPath = viewPath;
    }

    @Override
    public String getViewPath() {
        return viewPath;
    }

    @Override
    public void setViewPath(String viewPath) {
        this.viewPath = viewPath;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() > 1) {
            throw new IllegalArgumentException("The EMBEDDING_VIEW macro allows maximally one argument: <expression>!");
        }

        if (viewPath == null) {
            throw new IllegalArgumentException("The EMBEDDING_VIEW macro is not supported in this context!");
        } else if (viewPath.isEmpty()) {
            if (context.getArgumentsSize() > 0) {
                context.addArgument(0);
            }
        } else {
            context.addChunk(viewPath);
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
        if (!(o instanceof MutableViewJpqlMacro)) {
            return false;
        }

        MutableViewJpqlMacro that = (MutableViewJpqlMacro) o;

        return viewPath != null ? viewPath.equals(that.viewPath) : that.viewPath == null;
    }

    @Override
    public int hashCode() {
        return viewPath != null ? viewPath.hashCode() : 0;
    }
}
