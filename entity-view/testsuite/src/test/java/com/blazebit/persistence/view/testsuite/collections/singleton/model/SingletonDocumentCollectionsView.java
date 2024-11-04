/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.collections.singleton.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.Mapping;
import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.testsuite.entity.Person;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface SingletonDocumentCollectionsView {
    
    @IdMapping
    public Long getId();

    public String getName();

    @Mapping("owner.id")
    public Long getOwnerEntityId();

    @Mapping("owner")
    public Person getOwnerEntity();

    @Mapping("owner")
    public SingletonPersonView getOwnerEntityView();

    @Mapping("owner.id")
    public List<Long> getOwnerEntityIdList();

    @Mapping("owner")
    public List<Person> getOwnerEntityList();

    @Mapping("owner")
    public List<SingletonPersonView> getOwnerEntityViewList();
}
