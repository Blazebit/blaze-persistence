/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.integration.datanucleus.function;

import com.blazebit.persistence.spi.FunctionRenderContext;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DataNucleusFunctionRenderContext implements FunctionRenderContext {
    
    private final StringBuilder sb = new StringBuilder();
    private final List<?> arguments;

    public DataNucleusFunctionRenderContext(List<?> arguments) {
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
        return "HibernateFunctionRenderContext{" + "arguments=" + arguments + '}';
    }
    
}
