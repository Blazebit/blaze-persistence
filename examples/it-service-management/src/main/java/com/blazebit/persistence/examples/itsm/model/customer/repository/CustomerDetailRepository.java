/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blazebit.persistence.examples.itsm.model.customer.view.AbstractCustomerDetail;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface CustomerDetailRepository
        extends JpaRepository<AbstractCustomerDetail, Long> {

    Optional<AbstractCustomerDetail> findByName(String name);

}
