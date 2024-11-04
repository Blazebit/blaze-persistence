/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.entity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
