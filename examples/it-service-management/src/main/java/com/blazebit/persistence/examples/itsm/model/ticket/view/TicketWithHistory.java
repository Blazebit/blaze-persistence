/*
 * Copyright 2014 - 2024 Blazebit.
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
