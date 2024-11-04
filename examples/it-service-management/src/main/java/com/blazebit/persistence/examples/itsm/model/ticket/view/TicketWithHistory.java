/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import java.util.Comparator;
import java.util.SortedSet;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.Ticket;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.EntityView;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(Ticket.class)
public interface TicketWithHistory extends TicketBase {

    @CollectionMapping(comparator = TicketHistoryComparator.class)
    SortedSet<TicketHistoryDetail> getHistory();

    class TicketHistoryComparator implements Comparator<TicketHistoryDetail> {

        @Override
        public int compare(TicketHistoryDetail o1, TicketHistoryDetail o2) {
            Comparator<TicketHistoryDetail> comparator = Comparator.comparing(
                    TicketHistoryDetail::getCreated, Comparator.naturalOrder());
            return comparator.compare(o1, o2);
        }

    }
}
