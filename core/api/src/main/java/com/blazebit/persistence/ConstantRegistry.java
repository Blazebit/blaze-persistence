/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence;

/**
 * Registry that allows binding of parameter constants to a Criteria Builder
 * prior to their first use in expressions.
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public interface ConstantRegistry {

    /**
     * Bind the constant under the value.
     *
     * @param object the value to bind
     * @return The name of the parameter under which the constant is bound
     */
    String addConstant(Object object);

}
