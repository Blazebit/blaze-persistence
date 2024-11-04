/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrder;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(LegacyOrder.class)
public interface LegacyOrderIdView extends IdHolderView<Long> {
}
