/*
 * Copyright 2014 - 2019 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
