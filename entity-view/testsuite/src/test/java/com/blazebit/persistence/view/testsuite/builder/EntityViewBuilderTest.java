/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.testsuite.builder;

import com.blazebit.persistence.view.EntityViewBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViews;
import com.blazebit.persistence.view.spi.EntityViewConfiguration;
import com.blazebit.persistence.view.testsuite.AbstractEntityViewTest;
import com.blazebit.persistence.view.testsuite.builder.model.DocumentBuilderView;
import com.blazebit.persistence.view.testsuite.builder.model.PersonView;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 *
 * @author Christian Beikov
 * @since 1.5.0
 */
public class EntityViewBuilderTest extends AbstractEntityViewTest {

    @Test
    public void testBuilder() {
        Object global = new Object();
        EntityViewConfiguration cfg = EntityViews.createDefaultConfiguration();
        cfg.setOptionalParameter("test", global);
        EntityViewManager evm = build(cfg, DocumentBuilderView.class, PersonView.class);
        EntityViewBuilder<DocumentBuilderView> builder = evm.createBuilder(DocumentBuilderView.class);
        DocumentBuilderView view = builder.with("id", 10L)
            .with("name", "Test")
            .withSingularBuilder("owner")
                .with("id", 100L)
                .with("name", "Owner")
            .build()
            .withMapBuilder("contacts", 1)
                .with("id", 100L)
                .with("name", "Owner")
            .build()
            .withListBuilder("people", 1)
                .with("id", 100L)
                .with("name", "Owner")
            .build()
            .withCollectionBuilder("peopleListBag")
                .with("id", 100L)
                .with("name", "Owner")
            .build()
            .withCollectionBuilder("partners")
                .with("id", 100L)
                .with("name", "Owner")
            .build()
            .withElement("strings", "Test")
        .build();
        assertView(view);
        Assert.assertEquals(global, view.getTest());
        Object overridden = new Object();
        DocumentBuilderView view2 = evm.createBuilder(view, Collections.singletonMap("test", overridden))
            .withListBuilder("people", 0)
                .with("id", 100L)
                .with("name", "Owner")
            .build()
        .build();
        assertView(view2);
        Assert.assertEquals(overridden, view2.getTest());
        Assert.assertNull(view.getPeople().get(0));
        assertOwner(view2.getPeople().get(0));
        DocumentBuilderView view3 = evm.createBuilder(view).with(0, "Test").build();
        Assert.assertEquals("Test", view3.getTest());
    }

    private void assertView(DocumentBuilderView view) {
        Assert.assertEquals(10L, view.getId().longValue());
        Assert.assertEquals("Test", view.getName());
        assertOwner(view.getOwner());
        assertOwner(view.getContacts().get(1));
        assertOwner(view.getPeople().get(1));
        assertOwner(view.getPeopleListBag().get(0));
        assertOwner(view.getPartners().iterator().next());
        Assert.assertEquals("Test", view.getStrings().iterator().next());
    }

    private void assertOwner(PersonView p) {
        Assert.assertEquals(100L, p.getId().longValue());
        Assert.assertEquals("Owner", p.getName());
    }
}
