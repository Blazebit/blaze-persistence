/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.subtype.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.CreatableEntityView;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@CreatableEntityView
@EntityView(Document.class)
public interface CreatableDocumentView extends DocumentView {

    void setOwner(PersonIdView owner);
    void setResponsiblePerson(PersonIdView responsiblePerson);
}
