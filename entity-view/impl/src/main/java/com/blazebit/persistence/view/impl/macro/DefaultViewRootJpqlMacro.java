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
