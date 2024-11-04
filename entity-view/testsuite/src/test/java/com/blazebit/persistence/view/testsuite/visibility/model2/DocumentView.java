/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.visibility.model2;

import com.blazebit.persistence.testsuite.entity.Document;
import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.visibility.model1.IdHolderView;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(Document.class)
public abstract class DocumentView extends IdHolderView<Long> {

    private static final long serialVersionUID = 1L;

    public long id() {
        return getId();
    }

}
