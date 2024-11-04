/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.view.AttributeFilter;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.Limit;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.view.MappingSubquery;
import com.blazebit.persistence.view.SubqueryProvider;
import com.blazebit.persistence.view.filter.NullFilter;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
@EntityView(Document.class)
public interface DocumentWithEntityView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @AttributeFilter(NullFilter.class)
    public Person getOwner();

    @Limit(limit = "1", order = "KEY(this)")
    @Mapping("contacts")
    public PersonView getFirstContact();

    @MappingSubquery(ContactCountProvider.class)
    public long getContactCount();

    class ContactCountProvider implements SubqueryProvider {
        @Override
        public <T> T createSubquery(SubqueryInitiator<T> subqueryInitiator) {
            return subqueryInitiator.from("EMBEDDING_VIEW(contacts)", "c")
                    .select("COUNT(*)")
                    .end();
        }
    }
}
