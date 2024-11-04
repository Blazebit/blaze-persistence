/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.convert.view.model;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.Self;
import com.blazebit.persistence.view.testsuite.convert.view.model.sub.DocumentCloneParentView;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Document.class)
public abstract class DocumentCloneView2 extends DocumentCloneParentView {
    public DocumentCloneView2(@Self DocumentCloneParentView self) {
        super(self);
    }
}
