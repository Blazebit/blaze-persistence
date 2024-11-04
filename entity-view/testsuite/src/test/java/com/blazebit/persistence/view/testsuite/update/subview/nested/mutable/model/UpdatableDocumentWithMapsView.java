/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.nested.mutable.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import com.blazebit.persistence.view.UpdatableEntityView;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentWithMapsView {
    
    @IdMapping
    public Long getId();

    public Long getVersion();

    public String getName();

    public void setName(String name);

    public Map<Integer, UpdatableResponsiblePersonView> getContacts();

    public void setContacts(Map<Integer, UpdatableResponsiblePersonView> contacts);

}
