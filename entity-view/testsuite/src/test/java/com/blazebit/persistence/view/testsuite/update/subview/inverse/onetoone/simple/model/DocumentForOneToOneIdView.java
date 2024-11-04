/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.onetoone.simple.model;

import com.blazebit.persistence.testsuite.entity.DocumentForSimpleOneToOne;
import com.blazebit.persistence.view.EntityView;

/**
 *
 * @author Christian Beikov
 * @since 1.3.0
 */
@EntityView(DocumentForSimpleOneToOne.class)
public interface DocumentForOneToOneIdView extends IdHolderView<Long> {
}
