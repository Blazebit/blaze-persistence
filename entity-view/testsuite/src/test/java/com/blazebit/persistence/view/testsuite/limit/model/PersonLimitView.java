/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.limit.model;

import com.blazebit.persistence.view.testsuite.basic.model.IdHolderView;

import java.util.List;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public interface PersonLimitView extends IdHolderView<Long> {

    public String getName();

    public List<DocumentLimitView> getOwnedDocuments();

}
