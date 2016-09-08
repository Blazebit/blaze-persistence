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
package com.blazebit.persistence.view.impl.objectbuilder.transformer;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.impl.macro.CorrelatedSubqueryViewRootJpqlMacro;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedTupleTransformer implements TupleTransformer {

    private final CriteriaBuilder<?> criteriaBuilder;
    private final CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro;
    private final String correlationParamName;
    private final int tupleIndex;

    public AbstractCorrelatedTupleTransformer(CriteriaBuilder<?> criteriaBuilder, CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro, String correlationParamName, int tupleIndex) {
        this.criteriaBuilder = criteriaBuilder;
        this.viewRootJpqlMacro = viewRootJpqlMacro;
        this.correlationParamName = correlationParamName;
        this.tupleIndex = tupleIndex;
    }

    @Override
    public Object[] transform(Object[] tuple) {
        criteriaBuilder.setParameter(correlationParamName, tuple[tupleIndex]);
        // The id of the view root is always on position 0
        viewRootJpqlMacro.setParameters(tuple[0]);
        tuple[tupleIndex] = transform(criteriaBuilder);
        return tuple;
    }

    protected abstract Object transform(CriteriaBuilder<?> criteriaBuilder);

}
