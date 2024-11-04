/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.complex.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.UpdatableEntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionElement;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
@UpdatableEntityView
@EntityView(LegacyOrderPositionElement.class)
public interface UpdatableLegacyOrderPositionElementView extends LegacyOrderPositionElementIdView {

    public String getText();

    public void setText(String text);
}
