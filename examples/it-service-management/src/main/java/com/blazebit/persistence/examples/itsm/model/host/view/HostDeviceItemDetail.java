/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.host.view;

import com.blazebit.persistence.examples.itsm.model.host.entity.HostDeviceItem;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(HostDeviceItem.class)
@EntityViewInheritance
public interface HostDeviceItemDetail {

    @IdMapping
    Long getId();

}
