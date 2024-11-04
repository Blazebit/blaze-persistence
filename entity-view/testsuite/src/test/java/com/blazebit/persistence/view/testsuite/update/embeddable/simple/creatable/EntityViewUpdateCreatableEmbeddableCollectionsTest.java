/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.embeddable.simple.creatable;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.update.embeddable.simple.creatable.model.UpdatableDocumentEmbeddableWithCollectionsView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: No Datanucleus support yet
@Category({ NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateCreatableEmbeddableCollectionsTest extends AbstractEntityViewTest {

    @Test
    public void testValidateInvalidConfiguration() {
        try {
            evm = build(UpdatableDocumentEmbeddableWithCollectionsView.class);
            fail("Expected failure because of invalid attribute definition!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Cascading configuration for basic, embeddable or flat view type attributes is not allowed"));
        }
    }
}
