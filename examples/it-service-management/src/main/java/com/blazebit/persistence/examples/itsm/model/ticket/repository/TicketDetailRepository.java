/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blazebit.persistence.spring.data.repository.EntityViewSpecificationExecutor;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.examples.itsm.model.ticket.view.TicketDetailUpdatable;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
public interface TicketDetailRepository
        extends JpaRepository<TicketDetailUpdatable, Long>,
        EntityViewSpecificationExecutor<TicketDetailUpdatable, Ticket> {

}
