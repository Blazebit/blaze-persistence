/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.update.flatview.nested.updatableonly;

import com.blazebit.persistence.testsuite.base.jpa.category.NoDatanucleus;
import com.blazebit.persistence.testsuite.base.jpa.category.NoEclipselink;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate42;
import com.blazebit.persistence.testsuite.base.jpa.category.NoHibernate43;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.updatableonly.model.UpdatableDocumentWithCollectionsView;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.updatableonly.model.UpdatableNameObjectContainerView;
import com.blazebit.persistence.view.testsuite.update.flatview.nested.updatableonly.model.UpdatableNameObjectView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
// NOTE: Apparently Hibernate 4 does not support a list of embeddables containing embeddables
// NOTE: No Datanucleus support yet
@Category({ NoHibernate42.class, NoHibernate43.class, NoDatanucleus.class, NoEclipselink.class})
public class EntityViewUpdateNestedUpdatableOnlyFlatViewCollectionsTest extends AbstractEntityViewTest {

    @Test
    public void testValidateInvalidConfiguration() {
        try {
            evm = build(
                    UpdatableDocumentWithCollectionsView.class,
                    UpdatableNameObjectContainerView.class,
                    UpdatableNameObjectView.class
            );
            fail("Expected failure because of invalid attribute definition!");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Cascading configuration for basic, embeddable or flat view type attributes is not allowed"));
        }
    }
}
