/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.correlated.creatable.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.MappingCorrelatedSimple;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

import java.util.Date;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public abstract class UpdatableDocumentView {
    
    @IdMapping
    public abstract Long getId();

    public abstract Long getVersion();

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Date getLastModified();
    
    public abstract void setLastModified(Date date);

    @UpdatableMapping(cascade = { CascadeType.PERSIST })
    @MappingCorrelatedSimple(correlated = Person.class, correlationBasis = "responsiblePerson.id", correlationExpression = "id IN correlationKey")
    public abstract UpdatablePersonView getResponsiblePerson();
    
    public abstract void setResponsiblePerson(UpdatablePersonView responsiblePerson);

}
