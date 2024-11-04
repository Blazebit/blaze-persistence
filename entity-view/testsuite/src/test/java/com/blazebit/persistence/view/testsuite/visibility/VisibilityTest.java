/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.visibility;

import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.visibility.model1.IdHolderView;
import com.blazebit.persistence.view.testsuite.visibility.model2.DocumentView;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Christian Beikov
 * @since 1.4.0
 */
public class VisibilityTest extends AbstractEntityViewTest {

    @Test
    public void testVisibilityModifierAndDifferentPackages() {
        EntityViewManager evm = build(
                IdHolderView.class,
                DocumentView.class
        );

        // Requires a class in the package where IdHolderView resides that overrides with public modifiers
        // Should also print a warning that equals can't support user types
        DocumentView documentView = evm.getReference(DocumentView.class, 0L);
        Assert.assertEquals(Long.MIN_VALUE, documentView.id());
    }
}
