/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.listener.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@UpdatableEntityView
@CreatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentWithCollectionsView {
    
    @IdMapping
    public Long getId();

    public Long getVersion();

    public String getName();

    public void setName(String name);

    public PersonView getOwner();

    public void setOwner(PersonView owner);

    public List<UpdatablePersonView> getPeople();

    public void setPeople(List<UpdatablePersonView> people);

}
