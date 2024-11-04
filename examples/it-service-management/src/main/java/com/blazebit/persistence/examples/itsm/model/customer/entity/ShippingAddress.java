/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@DiscriminatorValue("address")
public class ShippingAddress extends AbstractCustomer {

    @ManyToOne
    private Customer customer;

    public Customer getCustomer() {
        return this.customer;
    }

}
