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

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.view.CTEProvider;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public class SimpleCTEProviderFactory implements CTEProviderFactory {

    private final Class<? extends CTEProvider> clazz;

    public SimpleCTEProviderFactory(Class<? extends CTEProvider> clazz) {
        this.clazz = clazz;
    }

    @Override
    public CTEProvider create() {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not instantiate the CTE provider: " + clazz.getName(), ex);
        }
    }

}
