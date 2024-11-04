/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.correlated.mutable.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public abstract class UpdatableDocumentWithCollectionsView {
    
    @IdMapping
    public abstract Long getId();

    public abstract Long getVersion();

    public abstract String getName();

    public abstract void setName(String name);

    @MappingCorrelatedSimple(correlated = Person.class, correlationBasis = "id", correlationExpression = "partnerDocument.id IN correlationKey")
    public abstract List<UpdatablePersonView> getPartners();

    public abstract void setPartners(List<UpdatablePersonView> partners);

    public void addPerson(UpdatablePersonView person) {
        getPartners().add(person);
        person.setPartnerDocument(evm().getReference(DocumentIdView.class, getId()));
    }

    abstract EntityViewManager evm();

}
