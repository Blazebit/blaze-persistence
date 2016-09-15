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
