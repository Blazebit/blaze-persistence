/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.examples.itsm.model.ticket.view;

import java.util.Comparator;
import java.util.SortedSet;

import com.blazebit.persistence.examples.itsm.model.ticket.entity.TicketStatus;
import com.blazebit.persistence.view.CollectionMapping;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 * @author Giovanni Lovato
 * @since 1.4.0
 */
@EntityView(TicketStatus.class)
@UpdatableEntityView
public interface StatusWithNext extends StatusId {

    @UpdatableMapping
    @CollectionMapping(comparator = TicketStatusComparator.class)
    SortedSet<StatusBase> getNext();

    class TicketStatusComparator implements Comparator<StatusBase> {

        @Override
        public int compare(StatusBase o1, StatusBase o2) {
            Comparator<StatusBase> comparator = Comparator.comparing(StatusBase::getName,
                    Comparator.naturalOrder());
            return comparator.compare(o1, o2);
        }

    }

}
