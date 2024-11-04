/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.simple.model;

import com.blazebit.persistence.testsuite.entity.Person;
import com.blazebit.persistence.view.CascadeType;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.MappingInverse;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

import java.util.Set;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(Person.class)
public interface UpdatablePersonView extends PersonIdView {

    String getName();
    void setName(String name);

    @MappingInverse(removeStrategy = InverseRemoveStrategy.REMOVE)
    @UpdatableMapping(cascade = { CascadeType.UPDATE, CascadeType.PERSIST })
    Set<DocumentIdView> getOwnedDocuments2();
    void setOwnedDocuments2(Set<DocumentIdView> ownedDocuments2);
}
