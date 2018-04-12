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
 * Interface for implementing a macro function that produces JPQL from it's parameters.
 * 
 * An instance of this interface needs to be registered to be able to use the macro in queries.
 * Consider implementing {@link CacheableJpqlMacro} if possible to allow expressions containing the macro to be cached.
 *
 * @author Christian Beikov
 * @since 1.2.0
 * @see CacheableJpqlMacro
 */
public interface JpqlMacro {

    /**
     * Renders the function into the given function render context.
     * 
     * @param context The context into which the function should be rendered
     */
    public void render(FunctionRenderContext context);

}
