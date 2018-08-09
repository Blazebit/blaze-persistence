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

package com.blazebit.persistence.spring.data.impl.query;

import com.blazebit.persistence.spring.data.base.query.AbstractCriteriaQueryParameterBinder;
import com.blazebit.persistence.spring.data.base.query.JpaParameters;
import com.blazebit.persistence.spring.data.base.query.ParameterMetadataProvider;

/**
 * Concrete version for Spring Data 2.x.
 * 
 * @author Christian Beikov
 * @since 1.3.0
 */
public class CriteriaQueryParameterBinder extends AbstractCriteriaQueryParameterBinder {

    public CriteriaQueryParameterBinder(JpaParameters parameters, Object[] values, Iterable<ParameterMetadataProvider.ParameterMetadata<?>> expressions) {
        super(parameters, values, expressions);
    }

    @Override
    protected int getOffset() {
        return (int) getPageable().getOffset();
    }
}
