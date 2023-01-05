/*
 * Copyright 2014 - 2023 Blazebit.
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

import com.blazebit.persistence.ParameterHolder;

import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.criteria.ParameterExpression;
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
