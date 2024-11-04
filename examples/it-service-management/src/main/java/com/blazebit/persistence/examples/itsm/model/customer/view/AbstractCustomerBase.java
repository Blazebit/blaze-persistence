/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.view;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.blazebit.persistence.examples.itsm.model.customer.entity.AbstractCustomer;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(AbstractCustomer.class)
public interface AbstractCustomerBase {

    @IdMapping
    Long getId();

    String getName();

    String getEmailAddress();

    String getTelephoneNumber();

    String getFaxNumber();

    String getNumber();

    String getStreet();

    String getDistrict();

    String getPostalCode();

    String getCity();

    String getProvince();

    String getRegion();

    String getCountry();

    default String getFullAddress() {
        return Stream
                .of(this.getNumber(), this.getStreet(), this.getDistrict(),
                        this.getPostalCode(), this.getCity(),
                        this.getProvince(), this.getRegion(), this.getCountry())
                .filter(Objects::nonNull).collect(Collectors.joining(", "));
    }

}
