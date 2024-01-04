/*
 * Copyright 2014 - 2024 Blazebit.
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

package com.blazebit.persistence.view;

import com.blazebit.persistence.CTEBuilder;

import java.util.Map;

/**
 * Provides CTE bindings to a {@link CTEBuilder}.
 *
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface CTEProvider {

    /**
     * Binds a CTE.
     *
     * @param builder the builder
     * @param optionalParameters The optional parameters of the entity view setting
     */
    void applyCtes(CTEBuilder<?> builder, Map<String, Object> optionalParameters);
}
