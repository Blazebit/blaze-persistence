/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.simple.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@CreatableEntityView
@UpdatableEntityView
@EntityView(Document.class)
public interface UpdatableDocumentView extends DocumentIdView {

    String getName();
    void setName(String name);

    PersonIdView getOwner();
    void setOwner(PersonIdView owner);
}
