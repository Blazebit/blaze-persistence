/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.view;

import com.blazebit.persistence.examples.itsm.model.customer.entity.AbstractCustomer;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewInheritance;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(AbstractCustomer.class)
@EntityViewInheritance({ CustomerDetail.class, ShippingAddressDetail.class })
public interface AbstractCustomerDetail extends AbstractCustomerBase {

    ServiceDetailView getServiceDetail();

}
