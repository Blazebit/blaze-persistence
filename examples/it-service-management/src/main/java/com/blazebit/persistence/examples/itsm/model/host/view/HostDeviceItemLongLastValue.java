/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.host.view;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.examples.itsm.model.host.entity.HostDeviceItemLong;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(HostDeviceItemLong.class)
public interface HostDeviceItemLongLastValue extends HostDeviceItemDetail {

    @MappingSubquery(LastValueProvider.class)
    Long getLastValue();

    class LastValueProvider implements SubqueryProvider {

        @Override
        public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator) {
            // @formatter:off
            return subqueryInitiator
                    .from("embedding_view(values)", "v")
                    .select("v")
                    .orderByDesc("key(v)")
                    .setMaxResults(1)
                    .end();
            // @formatter:on
        }

    }

}
