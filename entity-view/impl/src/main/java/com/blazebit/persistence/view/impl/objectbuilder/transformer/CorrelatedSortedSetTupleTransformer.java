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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSortedSetTupleTransformer extends AbstractCorrelatedTupleTransformer {

    private final Comparator<?> comparator;

    public CorrelatedSortedSetTupleTransformer(CriteriaBuilder<?> criteriaBuilder, CorrelatedSubqueryViewRootJpqlMacro viewRootJpqlMacro, String correlationParamName, int tupleIndex, Comparator<?> comparator) {
        super(criteriaBuilder, viewRootJpqlMacro, correlationParamName, tupleIndex);
        this.comparator = comparator;
    }

    @Override
    public Object transform(CriteriaBuilder<?> cb) {
        Set<Object> s = new TreeSet<Object>((Comparator<? super Object>) comparator);
        s.addAll(cb.getResultList());
        return s;
    }

}
