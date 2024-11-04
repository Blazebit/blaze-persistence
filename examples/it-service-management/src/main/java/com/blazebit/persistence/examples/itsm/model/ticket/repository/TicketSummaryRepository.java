/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.repository;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.examples.itsm.model.ticket.view.TicketSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.blazebit.persistence.spring.data.annotation.OptionalParam;
import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;

import com.blazebit.persistence.examples.itsm.model.common.entity.User;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface TicketSummaryRepository
        extends JpaRepository<TicketSummary, Long>,
        EntityViewSpecificationExecutor<TicketSummary, Ticket> {

    @Transactional(readOnly = true)
    List<TicketSummary> findAll(@OptionalParam("observer") User observer,
            @OptionalParam("locale") Locale locale,
            @OptionalParam("defaultLocale") Locale defaultLocale);

    @Transactional(readOnly = true)
    List<TicketSummary> findAll(Specification<Ticket> spec,
            @OptionalParam("observer") User observer);

    @Transactional(readOnly = true)
    List<TicketSummary> findAll(Specification<Ticket> spec,
            @OptionalParam("observer") User observer, Pageable pageable);

    @Transactional(readOnly = true)
    Optional<TicketSummary> findByNumber(long number,
            @OptionalParam("observer") User observer);

}
