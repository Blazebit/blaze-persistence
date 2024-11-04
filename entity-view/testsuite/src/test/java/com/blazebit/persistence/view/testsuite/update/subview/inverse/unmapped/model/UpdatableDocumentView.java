/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.unmapped.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.AllowUpdatableEntityViews;
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
 * @since 1.2.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentView extends DocumentIdView {

    public String getName();
    public void setName(String name);

    @UpdatableMapping(cascade = { CascadeType.UPDATE, CascadeType.PERSIST })
    @AllowUpdatableEntityViews
    PersonIdView getOwner();
    void setOwner(PersonIdView owner);

    @MappingInverse(removeStrategy = InverseRemoveStrategy.REMOVE)
    @UpdatableMapping(cascade = { CascadeType.UPDATE, CascadeType.PERSIST })
    Set<VersionIdView> getVersions();
    void setVersions(Set<VersionIdView> versions);

    @MappingInverse(removeStrategy = InverseRemoveStrategy.SET_NULL)
    @UpdatableMapping
    Set<UpdatablePersonView> getPartners();
    void setPartners(Set<UpdatablePersonView> partners);
}
