/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@Entity
@DiscriminatorValue("customer")
public class Customer extends AbstractCustomer {

    boolean top;

    public boolean isTop() {
        return this.top;
    }

}
