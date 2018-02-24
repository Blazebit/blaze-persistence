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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.view.FetchStrategy;
import com.blazebit.persistence.view.Mapping;

import java.lang.annotation.Annotation;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MappingLiteral implements Mapping {

    private static final String[] EMPTY = new String[0];

    private final String value;
    private final String[] fetches;
    private final FetchStrategy fetch;

    public MappingLiteral(String value) {
        this.value = value;
        this.fetches = EMPTY;
        this.fetch = FetchStrategy.JOIN;
    }

    public MappingLiteral(String value, Mapping original) {
        this.value = value;
        this.fetches = original.fetches();
        this.fetch = original.fetch();
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String[] fetches() {
        return fetches;
    }

    @Override
    public FetchStrategy fetch() {
        return fetch;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Mapping.class;
    }
}
