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

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.impl.EntityViewConfiguration;
import com.blazebit.persistence.view.impl.EntityViewManagerImpl;
import com.blazebit.persistence.view.metamodel.ManagedViewType;
import com.blazebit.persistence.view.metamodel.ViewType;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public final class SubviewCorrelator implements Correlator {

    private final ManagedViewType<?> managedViewType;
    private final EntityViewManagerImpl evm;
    private final String viewName;

    public SubviewCorrelator(ManagedViewType<?> managedViewType, EntityViewManagerImpl evm, String viewName) {
        this.managedViewType = managedViewType;
        this.evm = evm;
        this.viewName = viewName;
    }

    @Override
    public void finish(CriteriaBuilder<?> criteriaBuilder, EntityViewConfiguration entityViewConfiguration, int batchSize, String correlationRoot) {
        // We have the correlation key on the first position if we do batching
        int offset = batchSize > 1 ? 1 : 0;
        evm.applyObjectBuilder((ViewType<?>) managedViewType, null, viewName, correlationRoot, criteriaBuilder, entityViewConfiguration, offset, false);
    }
}
