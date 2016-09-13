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
package com.blazebit.persistence.view.impl.objectbuilder.transformer.correlation;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;

import java.util.*;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedListTupleListTransformer extends AbstractCorrelatedCollectionTupleListTransformer {

    public CorrelatedListTupleListTransformer(CriteriaBuilder<?> criteriaBuilder, CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro, String correlationParamName, int tupleIndex, int batchSize, Class<?> correlationBasisEntity) {
        super(criteriaBuilder, viewRootJpqlMacro, correlationParamName, tupleIndex, batchSize, correlationBasisEntity);
    }

    @Override
    protected Collection<Object> createCollection(List<? extends Object> list) {
        return (Collection<Object>) list;
    }

    @Override
    protected Collection<Object> createCollection(int size) {
        if (size < 1) {
            return new ArrayList<Object>();
        }
        return new ArrayList<Object>(size);
    }
}
