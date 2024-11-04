/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.convert.view.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Document.class)
@UpdatableEntityView
@CreatableEntityView
public interface DocumentCloneUpdateView extends DocumentCloneView {

    public void setName(String name);

    public void setAge(long age);

    public void setOwner(PersonView owner);

    public void setPeople(List<PersonView> people);

    public void setPartners(Set<PersonView> partners);

    public void setContacts(Map<Integer, Person> contacts);
}
