/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.view;

import com.blazebit.persistence.examples.itsm.model.customer.entity.ShippingAddress;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(ShippingAddress.class)
@UpdatableEntityView
public interface ShippingAddressDetail extends AbstractCustomerDetail {

    @Mapping("customer.top")
    boolean isTop();

    CustomerDetailReadonly getCustomer();

}
