/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.host.view;

import java.util.Set;

import com.blazebit.persistence.examples.itsm.model.host.entity.HostDevice;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(HostDevice.class)
public interface HostDeviceWithItems {

    @IdMapping
    Long getId();

    Set<HostDeviceItemDetail> getItems();

}
