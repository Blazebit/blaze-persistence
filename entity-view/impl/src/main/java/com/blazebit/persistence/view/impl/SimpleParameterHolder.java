/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl;

import com.blazebit.persistence.ParameterHolder;

import jakarta.persistence.Parameter;
import jakarta.persistence.TemporalType;
import jakarta.persistence.criteria.ParameterExpression;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * @author Christian Beikov
 * @since 1.6.0
 */
public class SimpleParameterHolder implements ParameterHolder {

    public static final SimpleParameterHolder INSTANCE = new SimpleParameterHolder();

    private SimpleParameterHolder() {
    }

    @Override
    public ParameterHolder setParameter(String name, Object value) {
        return this;
    }

    @Override
    public ParameterHolder setParameter(String name, Calendar value, TemporalType temporalType) {
        return this;
    }

    @Override
    public ParameterHolder setParameter(String name, Date value, TemporalType temporalType) {
        return this;
    }

    @Override
    public boolean containsParameter(String name) {
        return false;
    }

    @Override
    public boolean isParameterSet(String name) {
        return false;
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return null;
    }

    @Override
    public Set<? extends Parameter<?>> getParameters() {
        return Collections.emptySet();
    }

    @Override
    public Object getParameterValue(String name) {
        return null;
    }

    @Override
    public ParameterHolder setParameterType(String name, Class type) {
        return this;
    }

    @Override
    public ParameterHolder registerCriteriaParameter(String name, ParameterExpression parameter) {
        return this;
    }
}
