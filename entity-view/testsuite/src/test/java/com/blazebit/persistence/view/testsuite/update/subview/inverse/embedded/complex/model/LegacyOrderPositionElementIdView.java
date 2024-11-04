/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrder;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionElement;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@EntityView(LegacyOrderPositionElement.class)
public interface LegacyOrderPositionElementIdView extends IdHolderView<Long> {
}
