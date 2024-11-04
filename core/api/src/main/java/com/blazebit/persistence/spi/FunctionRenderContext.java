/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spi;

/**
 * Interface implemented by the criteria provider.
 * 
 * Abstraction to allow building structured output. This is used for rendering
 * dbms specific code for a {@link JpqlFunction}.
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public interface FunctionRenderContext {

    /**
     * Returns the size of the arguments given to this function.
     * 
     * @return the size of function arguments
     */
    public int getArgumentsSize();

    /**
     * Returns the string representation of the argument at the given index.
     * 
     * @param index The index of the wanted argument
     * @return the string representation of the wanted argument
     */
    public String getArgument(int index);

    /**
     * Adds a binding to the argument of the given index to the render context.
     * 
     * @param index The index of the wanted argument
     */
    public void addArgument(int index);

    /**
     * Adds the given chunk to the render context.
     * 
     * @param chunk The chunk to add
     */
    public void addChunk(String chunk);

}
