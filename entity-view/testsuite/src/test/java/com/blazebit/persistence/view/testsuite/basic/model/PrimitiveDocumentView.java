/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */
package com.blazebit.persistence.view.testsuite.basic.model;

import com.blazebit.persistence.testsuite.entity.PrimitiveDocument;
import com.blazebit.persistence.testsuite.entity.PrimitivePerson;
import com.blazebit.persistence.view.*;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(PrimitiveDocument.class)
public interface PrimitiveDocumentView extends PrimitiveSimpleDocumentView {

    public void setId(long id);

    public void setName(String name);

    public PrimitivePersonView getOwner();

    @Mapping(value = "owner", fetch = FetchStrategy.SELECT)
    public PrimitivePersonView getCorrelatedOwner();

    @Mapping("contacts[1]")
    public PrimitivePerson getFirstContactPerson();

    public List<PrimitivePersonView> getPartners();

    public Map<Integer, PrimitivePersonView> getContacts();

    public List<PrimitivePersonView> getPeople();

    // TODO: Report that selecting bags in datanucleus leads to an exception
//    public List<PrimitivePersonView> getPeopleListBag();

//    public List<PrimitivePersonView> getPeopleCollectionBag();

    public PrimitiveSimpleDocumentView getParent();

}
