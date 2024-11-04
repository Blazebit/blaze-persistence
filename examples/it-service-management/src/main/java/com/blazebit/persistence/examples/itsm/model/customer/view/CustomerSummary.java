/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.view;

import com.blazebit.persistence.examples.itsm.model.customer.entity.Customer;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Customer.class)
public interface CustomerSummary extends AbstractCustomerSummary {

}
