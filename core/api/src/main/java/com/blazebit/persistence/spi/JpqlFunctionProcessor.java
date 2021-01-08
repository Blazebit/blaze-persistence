/*
 * Copyright 2014 - 2021 Blazebit.
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

import java.util.List;

/**
 * Interface for implementing processing of values produced by a JPQL function in the SELECT clause.
 *
 * @param <T> The type this processor handles
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface JpqlFunctionProcessor<T> {

    /**
     * Processes the result set object.
     * 
     * @param result The result set object
     * @param arguments The JPQL function arguments
     * @return Returns the processed result set object
     */
    public Object process(T result, List<Object> arguments);

}
