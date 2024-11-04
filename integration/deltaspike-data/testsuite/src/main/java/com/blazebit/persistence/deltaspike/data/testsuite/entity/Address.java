/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.deltaspike.data.testsuite.entity;

import javax.persistence.Embeddable;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
@Embeddable
public class Address {
    private String street;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
