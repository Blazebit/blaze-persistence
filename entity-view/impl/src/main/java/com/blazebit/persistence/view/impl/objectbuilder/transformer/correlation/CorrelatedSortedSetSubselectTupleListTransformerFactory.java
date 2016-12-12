/*
 * Copyright 2014 - 2016 Blazebit.
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

import com.blazebit.persistence.FullQueryBuilder;
import com.blazebit.persistence.view.impl.CorrelationProviderFactory;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.objectbuilder.transformer.TupleListTransformer;
import com.blazebit.persistence.view.metamodel.ManagedViewType;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class CorrelatedSortedSetSubselectTupleListTransformerFactory extends AbstractCorrelatedSubselectTupleListTransformerFactory {

    private final Comparator<?> comparator;

    public CorrelatedSortedSetSubselectTupleListTransformerFactory(Correlator correlator, Class<?> criteriaBuilderRoot, ManagedViewType<?> viewRootType, String viewRootAlias, String correlationResult, String correlationKeyExpression, CorrelationProviderFactory correlationProviderFactory, int tupleIndex, Class<?> correlationBasisType, Class<?> correlationBasisEntity, Comparator<?> comparator) {
        super(correlator, criteriaBuilderRoot, viewRootType, viewRootAlias, correlationResult, correlationKeyExpression, correlationProviderFactory, tupleIndex, correlationBasisType, correlationBasisEntity);
        this.comparator = comparator;
    }

    @Override
    public TupleListTransformer create(FullQueryBuilder<?, ?> queryBuilder, Map<String, Object> optionalParameters, EntityViewConfiguration entityViewConfiguration) {
        return new CorrelatedSortedSetSubselectTupleListTransformer(correlator, criteriaBuilderRoot, viewRootType, viewRootAlias, correlationResult, correlationKeyExpression, correlationProviderFactory, tupleIndex, correlationBasisType, correlationBasisEntity, entityViewConfiguration, comparator);
    }

}
