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
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewType;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public abstract class AbstractCorrelatedSubviewSubqueryTupleTransformerFactory<T> extends AbstractCorrelatedSubqueryTupleTransformerFactory<T> {

    private final ManagedViewType<T> managedViewType;
    private final EntityViewManagerImpl evm;
    private final ExpressionFactory ef;
    private final String viewName;

    public AbstractCorrelatedSubviewSubqueryTupleTransformerFactory(ManagedViewType<T> managedViewType, ManagedViewType<?> viewRootType, String correlationResult, CorrelationProviderFactory correlationProviderFactory, int tupleIndex, Class<?> correlationBasisEntity, EntityViewManagerImpl evm, ExpressionFactory ef, String viewName) {
        super(managedViewType.getEntityClass(), viewRootType, correlationResult, correlationProviderFactory, tupleIndex, correlationBasisEntity);
        this.managedViewType = managedViewType;
        this.evm = evm;
        this.ef = ef;
        this.viewName = viewName;
    }

    @Override
    protected void finishCriteriaBuilder(CriteriaBuilder<?> criteriaBuilder, Map<String, Object> optionalParameters, String correlationRoot) {
        evm.applyObjectBuilder((ViewType<T>) managedViewType, null, viewName, correlationRoot, criteriaBuilder, optionalParameters, false);
    }

}
