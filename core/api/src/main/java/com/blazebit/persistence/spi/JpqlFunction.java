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

package com.blazebit.persistence.spi;

/**
 * Interface for implementing a JPA custom function that renders dbms specific code.
 * 
 * An instance of this interface needs to be registered to be able to use the function in queries.
 *
 * @author Christian Beikov
 * @since 1.0.0
 * @see EntityManagerFactoryIntegrator#registerFunctions(javax.persistence.EntityManagerFactory, java.util.Map)
 */
public interface JpqlFunction {

    /**
     * Returns true if the function has arguments, false otherwise.
     * 
     * @return true if the function has arguments, false otherwise
     */
    public boolean hasArguments();

    /**
     * Returns false if parentheses might be skipped if no arguments are given, true otherwise.
     * 
     * @return false if parentheses might be skipped if no arguments are given, true otherwise
     */
    public boolean hasParenthesesIfNoArguments();

    /**
     * Returns the return type of this function.
     * 
     * The return type may be null, but beware that nesting this function into other expressions may then fail.
     * 
     * @param firstArgumentType The type of the first argument
     * @return the return type of this function, or null
     */
    public Class<?> getReturnType(Class<?> firstArgumentType);

    /**
     * Renders the function into the given function render context.
     * 
     * @param context The context into which the function should be rendered
     */
    public void render(FunctionRenderContext context);

}
