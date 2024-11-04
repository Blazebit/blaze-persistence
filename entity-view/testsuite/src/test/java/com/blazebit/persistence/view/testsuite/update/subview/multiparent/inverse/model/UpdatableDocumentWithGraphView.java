/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.multiparent.inverse.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentWithGraphView {
    
    @IdMapping
    public Long getId();

    public Long getVersion();

    public String getName();

    public void setName(String name);

    public PersonView getOwner();

    public void setOwner(PersonView owner);

    public PersonView getResponsiblePerson();

    public void setResponsiblePerson(PersonView responsiblePerson);

    public Set<UpdatablePersonView> getPartners();

    public void setPartners(Set<UpdatablePersonView> partners);

}
