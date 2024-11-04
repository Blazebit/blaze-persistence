/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.inheritance.subview.simple.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(Document.class)
public interface DocumentView1 extends SimpleDocumentView {

    public PersonBaseView1 getOwner();
}
