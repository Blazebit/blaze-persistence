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

package com.blazebit.persistence.integration.openjpa;

import com.blazebit.persistence.ObjectBuilder;
import org.apache.openjpa.kernel.StoreContext;
import org.apache.openjpa.kernel.exps.AggregateListener;

import java.util.Collection;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class ObjectBuilderAggregateListenerAdapter implements AggregateListener {

    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    private final ObjectBuilder<?> builder;

    public ObjectBuilderAggregateListenerAdapter(ObjectBuilder<?> builder) {
        this.builder = builder;
    }

    @Override
    public String getTag() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean expectsArguments() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Object evaluate(Collection clctn, Class[] types, Collection clctn1, StoreContext sc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class getType(Class[] types) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
