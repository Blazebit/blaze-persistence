/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.pagination.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;
import com.blazebit.persistence.testsuite.entity.Document;

/**
 *
 * @author Moritz Becker
 * @since 1.0.0
 */
@EntityView(Document.class)
public interface DocumentViewInterface extends IdHolderView<Long> {

    public String getName();

}
