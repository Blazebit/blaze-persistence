/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.onetoone.entity.model;

import com.blazebit.persistence.testsuite.entity.DocumentForSimpleOneToOne;
import com.blazebit.persistence.testsuite.entity.DocumentInfoSimple;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.InverseRemoveStrategy;
import com.blazebit.persistence.view.MappingInverse;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.UpdatableMapping;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(DocumentForSimpleOneToOne.class)
public interface UpdatableDocumentForOneToOneView extends DocumentForOneToOneIdView {

    String getName();
    void setName(String name);

    @MappingInverse(removeStrategy = InverseRemoveStrategy.SET_NULL)
    @UpdatableMapping
    DocumentInfoSimple getDocumentInfo();
    void setDocumentInfo(DocumentInfoSimple documentInfo);
}
