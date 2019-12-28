/*
 * Copyright 2014 - 2019 Blazebit.
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

package com.blazebit.persistence.criteria.impl;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.SelectCTECriteriaBuilder;
import com.blazebit.persistence.SelectRecursiveCTECriteriaBuilder;
import com.blazebit.persistence.criteria.BlazeSelectCTECriteria;
import com.blazebit.persistence.criteria.BlazeSelectRecursiveCTECriteria;

public class BlazeSelectRecursiveCTECriteriaImpl<T>
        extends AbstractBlazeSelectBaseCTECriteria<T> // TODO: Introduce abstract class
        implements BlazeSelectRecursiveCTECriteria<T> {

    private final BlazeSelectCTECriteriaImpl<T> recursivePart;
    private boolean unionAll;

    public BlazeSelectRecursiveCTECriteriaImpl(BlazeCriteriaBuilderImpl criteriaBuilder, Class<T> returnType) {
        super(criteriaBuilder, returnType);
        recursivePart = new BlazeSelectCTECriteriaImpl<>(criteriaBuilder, returnType);
    }

    @Override
    public BlazeSelectCTECriteria<T> union() {
        return recursivePart;
    }

    @Override
    public BlazeSelectCTECriteria<T> unionAll() {
        unionAll = true;
        return recursivePart;
    }

    @Override
    public <X> CriteriaBuilder<X> render(CriteriaBuilder<X> cbs) {
        SelectRecursiveCTECriteriaBuilder<CriteriaBuilder<X>> criteriaBuilder = cbs.withRecursive(returnType);
        RenderContextImpl context = new RenderContextImpl();
        render(criteriaBuilder, context);

        SelectCTECriteriaBuilder<CriteriaBuilder<X>> criteriaBuilderSelectCTECriteriaBuilder =
                unionAll ? criteriaBuilder.unionAll() : criteriaBuilder.union();

        recursivePart.render(criteriaBuilderSelectCTECriteriaBuilder, context);
        return criteriaBuilderSelectCTECriteriaBuilder.end();
    }
}
