/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
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
