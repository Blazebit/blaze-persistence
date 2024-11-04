/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.macro;

import com.blazebit.persistence.spi.FunctionRenderContext;
import com.blazebit.persistence.view.spi.EmbeddingViewJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
public class MutableEmbeddingViewJpqlMacro implements EmbeddingViewJpqlMacro {

    private String embeddingViewPath;
    private boolean used;

    public MutableEmbeddingViewJpqlMacro() {
    }

    public MutableEmbeddingViewJpqlMacro(String embeddingViewPath) {
        this.embeddingViewPath = embeddingViewPath;
    }

    @Override
    public boolean usesEmbeddingView() {
        return used;
    }

    @Override
    public String getEmbeddingViewPath() {
        return embeddingViewPath;
    }

    @Override
    public void setEmbeddingViewPath(String embeddingViewPath) {
        this.embeddingViewPath = embeddingViewPath;
    }

    @Override
    public void render(FunctionRenderContext context) {
        if (context.getArgumentsSize() > 1) {
            throw new IllegalArgumentException("The EMBEDDING_VIEW macro allows maximally one argument: <expression>!");
        }

        if (embeddingViewPath == null) {
            throw new IllegalArgumentException("The EMBEDDING_VIEW macro is not supported in this context!");
        } else if (embeddingViewPath.isEmpty()) {
            used = true;
            if (context.getArgumentsSize() > 0) {
                context.addArgument(0);
            }
        } else {
            used = true;
            context.addChunk(embeddingViewPath);
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
        if (!(o instanceof MutableEmbeddingViewJpqlMacro)) {
            return false;
        }

        MutableEmbeddingViewJpqlMacro that = (MutableEmbeddingViewJpqlMacro) o;

        return embeddingViewPath != null ? embeddingViewPath.equals(that.embeddingViewPath) : that.embeddingViewPath == null;
    }

    @Override
    public int hashCode() {
        return embeddingViewPath != null ? embeddingViewPath.hashCode() : 0;
    }
}
