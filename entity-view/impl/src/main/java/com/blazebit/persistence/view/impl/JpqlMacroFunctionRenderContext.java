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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class JpqlMacroFunctionRenderContext implements FunctionRenderContext {

    private final StringBuilder sb = new StringBuilder();
    private final List<?> arguments;

    public JpqlMacroFunctionRenderContext(List<?> arguments) {
        this.arguments = arguments;
    }

    @Override
    public int getArgumentsSize() {
        return arguments.size();
    }

    @Override
    public String getArgument(int index) {
        return arguments.get(index).toString();
    }

    @Override
    public void addArgument(int index) {
        sb.append(arguments.get(index));
    }

    @Override
    public void addChunk(String chunk) {
        sb.append(chunk);
    }

    public String renderToString() {
        return sb.toString();
    }

    @Override
    public String toString() {
        return "JpqlMacroFunctionRenderContext{" + "arguments=" + arguments + '}';
    }
}
