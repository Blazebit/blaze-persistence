/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.subview.inverse.embedded.entityid.model;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefault;
import com.blazebit.persistence.view.testsuite.entity.LegacyOrderPositionDefaultId;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
@EntityView(LegacyOrderPositionDefault.class)
public interface LegacyOrderPositionDefaultIdView extends IdHolderView<LegacyOrderPositionDefaultId> {

}
