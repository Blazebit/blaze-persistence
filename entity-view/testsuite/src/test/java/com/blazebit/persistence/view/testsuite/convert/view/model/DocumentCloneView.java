/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.convert.view.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingParameter;
import com.blazebit.persistence.view.PostConvert;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentCloneView extends Serializable {

    @IdMapping
    public Long getId();

    public String getName();

    public long getAge();

    public PersonView getOwner();

    public List<PersonView> getPeople();

    public Set<PersonView> getPartners();

    public Map<Integer, Person> getContacts();

    @MappingParameter("source")
    public Object getSource();

    public void setSource(Object source);

    @PostConvert
    default void postConvert(Object source) {
        setSource(source);
    }
}
