/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.correlation.batchsubselect.model;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrder;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionElement;

/**
 *
 * @author Christian Beikov
 * @since 1.6.9
 */
@EntityView(LegacyOrderPositionElement.class)
public interface LegacyOrderPositionElementView extends IdHolderView<Long> {
    public String getText();

    @MappingSubquery(MySubqueryProvider.class)
    public Long getSubquery();

    class MySubqueryProvider implements SubqueryProvider {
        @Override
        public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator) {
            return subqueryInitiator.from(LegacyOrder.class, "lo")
                .where("lo.id").eqExpression("EMBEDDING_VIEW(orderId)")
                .select("id")
                .end();
        }
    }
}
