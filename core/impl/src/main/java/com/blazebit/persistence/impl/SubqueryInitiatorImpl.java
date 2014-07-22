/*
 * Copyright 2014 Blazebit.
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

package com.blazebit.persistence.impl;

import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import javax.persistence.EntityManager;

/**
 *
 * @author ccbem
 */
public class SubqueryInitiatorImpl<X> implements SubqueryInitiator<X> {
    private final EntityManager em;
    private final X result;
    private final ParameterManager parameterManager;

    public SubqueryInitiatorImpl(EntityManager em, X result, ParameterManager parameterManager) {
        this.em = em;
        this.result = result;
        this.parameterManager = parameterManager;
    }
    
    @Override
    public SubqueryBuilder<X> from(Class<?> clazz) {
        return new SubqueryBuilderImpl<X>(em, clazz.getSimpleName().toLowerCase(), result, parameterManager);
    }

    @Override
    public SubqueryBuilder<X> from(Class<?> clazz, String alias) {
        return new SubqueryBuilderImpl<X>(em, alias, result, parameterManager);
    }
    
}
