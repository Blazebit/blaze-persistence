/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blazebit.persistence.examples.itsm.model.customer.view.AbstractCustomerSummary;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface CustomerSummaryRepository
        extends JpaRepository<AbstractCustomerSummary, Long> {

}
