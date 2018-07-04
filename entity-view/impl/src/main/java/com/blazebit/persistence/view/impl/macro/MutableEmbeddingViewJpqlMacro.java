/*
 * Copyright 2014 - 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazebit.persistence.view.impl.macro;

import com.blazebit.persistence.spi.FunctionRenderContext;

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
